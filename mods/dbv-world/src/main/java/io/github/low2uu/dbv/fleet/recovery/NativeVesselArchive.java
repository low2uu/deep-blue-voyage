package io.github.low2uu.dbv.fleet.recovery;

import static io.github.low2uu.dbv.fleet.recovery.RecoveryMessages.text;
import com.enxv.aeronauticsstructuretool.PlacementSnapMode;
import com.enxv.aeronauticsstructuretool.RuntimeContraptionBlueprint;
import com.enxv.aeronauticsstructuretool.blueprint.capture.ConnectedSubLevelCollector;
import com.enxv.aeronauticsstructuretool.blueprint.capture.NativeBlueprintCaptureService;
import com.enxv.aeronauticsstructuretool.blueprint.codec.BlueprintArchiveCodec;
import com.enxv.aeronauticsstructuretool.blueprint.codec.NativeBlueprintReader;
import com.enxv.aeronauticsstructuretool.blueprint.geometry.PlotBlockDataReader;
import com.enxv.aeronauticsstructuretool.blueprint.geometry.PlotBlockTransform;
import com.enxv.aeronauticsstructuretool.blueprint.model.NativeBlueprintPlacementResult;
import com.enxv.aeronauticsstructuretool.blueprint.model.SavedSubLevelBlueprint;
import com.enxv.aeronauticsstructuretool.blueprint.placement.BlueprintVerticalPlacement;
import com.enxv.aeronauticsstructuretool.blueprint.placement.NativeBlueprintPlacementService;
import com.enxv.aeronauticsstructuretool.blueprint.runtime.BlueprintPlacementObserver;
import com.enxv.aeronauticsstructuretool.blueprint.security.MissingRegistryContentSanitizer;
import com.enxv.aeronauticsstructuretool.server.ServerServices;
import com.enxv.aeronauticsstructuretool.server.SubLevelCollisionToggleManager;
import com.google.gson.JsonObject;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import dev.ryanhcode.sable.sublevel.storage.holding.GlobalSavedSubLevelPointer;
import dev.ryanhcode.sable.sublevel.storage.holding.SubLevelHoldingChunk;
import dev.ryanhcode.sable.sublevel.storage.holding.SubLevelHoldingChunkMap;
import dev.ryanhcode.sable.sublevel.storage.region.SubLevelRegionFile;
import dev.ryanhcode.sable.sublevel.storage.region.SubLevelStorageFile;
import dev.ryanhcode.sable.sublevel.storage.serialization.SubLevelSerializer;
import dev.ryanhcode.sable.sublevel.storage.serialization.SubLevelStorage;
import io.github.low2uu.dbv.fleet.storage.BlockEntityJson;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.util.regex.Pattern;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.entity.BlockEntity;

/** Native ship import plus an independent, strict post-import content audit.
 * The upstream completion callback also fires when a contraption was skipped: it is not a receipt.
 */
public final class NativeVesselArchive {
    private static final String RECOVERY_SOURCE = "DbvRecoverySourceBlueprint";
    private static final String RECOVERY_ROOT = "DbvRecoveryCandidateRoot";
    private NativeVesselArchive() {}

    public record Verification(int blocks, int blockEntities, int containers, int runtimeContraptions) {}

    /** These are the actual source UUIDs, including child bodies whose root may already be destroyed. */
    public static List<UUID> sourceBodyIds(CompoundTag archive) throws IOException {
        return NativeBlueprintReader.read(archive).sublevels().stream()
                .map(SavedSubLevelBlueprint::originalId).distinct().toList();
    }

    /** Stable damage baseline. Blueprint UUIDs change on every capture; source body UUIDs do not.
     * Includes bearing-mounted blocks, which are absent from the root's ordinary plot block list.
     */
    public static JsonObject topology(CompoundTag archive) throws IOException {
        var result = new JsonObject();
        for (var saved : NativeBlueprintReader.read(archive).sublevels()) {
            String prefix = "body/" + saved.originalId();
            result.addProperty(prefix + "/@body", "body");
            for (var block : PlotBlockDataReader.read(saved.plotTag(), saved.sourceMinBuildHeight()))
                result.addProperty(prefix + "/block/" + block.blockPos().toShortString(),
                        BuiltInRegistries.BLOCK.getKey(block.state().getBlock()).toString());
            for (var moving : saved.runtimeContraptions()) {
                String assembly = prefix + "/runtime/" + moving.kind() + "/" + moving.controllerLocalPos().toShortString();
                result.addProperty(assembly + "/@assembly", moving.entityClassName() + ":" + moving.contraptionTag().getString("Type"));
                appendContraptionTopology(result, assembly, moving.contraptionTag());
            }
        }
        return result;
    }

    static void appendContraptionTopology(JsonObject result, String prefix, CompoundTag contraption) throws IOException {
        if (!contraption.contains("Blocks")) return;
        var blocks = contraption.getCompound("Blocks");
        if (!blocks.contains("Palette", Tag.TAG_LIST) || !blocks.contains("BlockList", Tag.TAG_LIST))
            throw new IOException(text("native.blueprint_invalid"));
        var palette = blocks.getList("Palette", Tag.TAG_COMPOUND);
        for (var value : blocks.getList("BlockList", Tag.TAG_COMPOUND)) {
            var block = (CompoundTag) value;
            var position = readPosition(block.get("Pos"));
            int state = block.getInt("State");
            if (position == null || !block.contains("State", Tag.TAG_INT) || state < 0 || state >= palette.size()
                    || !palette.getCompound(state).contains("Name", Tag.TAG_STRING))
                throw new IOException(text("native.blueprint_invalid"));
            result.addProperty(prefix + "/block/" + position.toShortString(), palette.getCompound(state).getString("Name"));
        }
    }

    /** Find and load a body even when its far-away holding chunk has never loaded after server startup.
     * A missing in-memory holding entry is not evidence of destruction. Read the complete native index;
     * corrupt or unreadable storage is an error, never permission to create a duplicate ship.
     * Must run on the server thread. No terrain chunks are generated by this lookup.
     */
    public static ServerSubLevel findPersistedBody(ServerLevel level, UUID id) throws IOException {
        if (!level.getServer().isSameThread()) throw new IOException(text("native.server_thread"));
        var container = SubLevelContainer.getContainer(level);
        var live = container.getSubLevel(id);
        if (live instanceof ServerSubLevel serverBody && !live.isRemoved()) return serverBody;
        var holding = container.getHoldingChunkMap();
        var memory = holding.getHoldingSubLevel(id);
        if (memory != null) {
            if (memory.pointer() == null) holding.loadHoldingSubLevel(memory);
            else holding.snatchAndLoad(memory.pointer(), id);
            return requireLoaded(level, id);
        }
        var storage = holding.getStorage();
        var loaded = holdingChunks(holding);
        var deleted = queuedDeletions(holding);
        // The live holding index may be newer than its disk version (moves/deletions this tick).
        for (var entry : new ArrayList<>(loaded.values())) {
            var found = findInHoldingChunk(storage, entry, deleted, id);
            if (found != null) {
                holding.snatchAndLoad(found, id);
                return requireLoaded(level, id);
            }
        }
        if (Files.notExists(storage.getFolder())) return null;
        if (!Files.isDirectory(storage.getFolder())) throw new IOException(text("native.storage_folder"));
        var regionPattern = Pattern.compile("r\\.(-?\\d+)\\.(-?\\d+)\\.slvlr");
        try (var files = Files.list(storage.getFolder())) {
            for (var path : files.sorted().toList()) {
                var match = regionPattern.matcher(path.getFileName().toString());
                if (!match.matches()) continue;
                int regionX = Integer.parseInt(match.group(1)), regionZ = Integer.parseInt(match.group(2));
                var first = new ChunkPos(Math.multiplyExact(regionX, 32), Math.multiplyExact(regionZ, 32));
                long bytes = Files.size(path);
                if (bytes > 0 && bytes < 4096) throw new IOException(text("native.index_truncated", path.getFileName()));
                var region = (SubLevelRegionFile) storageCall(storage, "getRegionFile",
                        new Class<?>[]{ChunkPos.class}, first);
                for (int z = 0; z < 32; z++) for (int x = 0; x < 32; x++) {
                    var chunk = new ChunkPos(first.x + x, first.z + z);
                    if (loaded.containsKey(chunk.toLong())) continue;
                    // Use the throwing read(int), not attemptLoadHoldingChunk which suppresses IO errors.
                    var raw = strictRead(region, region.getIndex(x, z));
                    if (raw == null) continue;
                    if (!raw.contains("pointers", Tag.TAG_INT_ARRAY)) throw new IOException(text("native.pointer_corrupt", chunk));
                    var entry = SubLevelHoldingChunk.from(chunk, raw);
                    if (entry == null) throw new IOException(text("native.index_corrupt", chunk));
                    var found = findInHoldingChunk(storage, entry, deleted, id);
                    if (found != null) {
                        holding.snatchAndLoad(found, id);
                        return requireLoaded(level, id);
                    }
                }
            }
        } catch (RuntimeException e) { throw new IOException(text("native.index_scan"), e); }
        return null;
    }

    private static GlobalSavedSubLevelPointer findInHoldingChunk(SubLevelStorage storage, SubLevelHoldingChunk entry,
                                                                Set<GlobalSavedSubLevelPointer> deleted, UUID id) throws IOException {
        for (var pointer : entry.getSubLevelPointers()) {
            var global = new GlobalSavedSubLevelPointer(entry.getChunkPos(), pointer.storageIndex(), pointer.subLevelIndex());
            if (deleted.contains(global)) continue;
            var dataFile = (SubLevelStorageFile) storageCall(storage, "getRegionStorageFile",
                    new Class<?>[]{ChunkPos.class, int.class}, entry.getChunkPos(), (int) pointer.storageIndex());
            var raw = strictRead(dataFile, pointer.subLevelIndex());
            if (raw == null) throw new IOException(text("native.data_missing", global));
            var data = SubLevelSerializer.fromData(raw);
            if (data == null) throw new IOException(text("native.data_corrupt", global));
            if (id.equals(data.uuid())) return global;
        }
        return null;
    }

    private static ServerSubLevel requireLoaded(ServerLevel level, UUID id) throws IOException {
        var body = SubLevelContainer.getContainer(level).getSubLevel(id);
        if (!(body instanceof ServerSubLevel serverBody) || body.isRemoved())
            throw new IOException(text("native.load_failed", id));
        return serverBody;
    }

    private static CompoundTag strictRead(SubLevelStorageFile file, int index) throws IOException {
        try {
            var field = SubLevelStorageFile.class.getDeclaredField("sectorSpans");
            field.setAccessible(true);
            int span = ((java.nio.IntBuffer) field.get(file)).get(index);
            if (span == 0) return null;
            var raw = file.read(index);
            if (raw == null) throw new IOException(text("native.sector_corrupt", file.getPath().getFileName(), index));
            return raw;
        } catch (ReflectiveOperationException | RuntimeException e) {
            throw new IOException(text("native.sector_verify"), e);
        }
    }

    /** Sable only exposes exception-suppressing storage lookups; reuse its own file cache so there
     * are never two independently writable handles to a region. Version drift fails closed.
     */
    private static Object storageCall(SubLevelStorage storage, String name, Class<?>[] types, Object... arguments) throws IOException {
        try {
            var method = SubLevelStorage.class.getDeclaredMethod(name, types);
            method.setAccessible(true);
            return method.invoke(storage, arguments);
        } catch (InvocationTargetException e) {
            if (e.getCause() instanceof IOException io) throw io;
            throw new IOException(text("native.storage_read", name), e.getCause());
        } catch (ReflectiveOperationException | RuntimeException e) {
            throw new IOException(text("native.storage_api", name), e);
        }
    }

    @SuppressWarnings("unchecked")
    private static Map<Long, SubLevelHoldingChunk> holdingChunks(SubLevelHoldingChunkMap holding) throws IOException {
        return (Map<Long, SubLevelHoldingChunk>) holdingField(holding, "loadedHoldingChunks");
    }

    @SuppressWarnings("unchecked")
    private static Set<GlobalSavedSubLevelPointer> queuedDeletions(SubLevelHoldingChunkMap holding) throws IOException {
        return new HashSet<>((Set<GlobalSavedSubLevelPointer>) holdingField(holding, "queuedDeletion"));
    }

    private static Object holdingField(SubLevelHoldingChunkMap holding, String name) throws IOException {
        try {
            var field = SubLevelHoldingChunkMap.class.getDeclaredField(name);
            field.setAccessible(true);
            return field.get(holding);
        } catch (ReflectiveOperationException | RuntimeException e) {
            throw new IOException(text("native.index_api", name), e);
        }
    }

    /** Read our typed JSON, fail before allocation if the required registry content is unavailable. */
    public static CompoundTag decodeAndValidate(JsonObject structure, ServerLevel level) throws IOException {
        try {
            if (structure == null || !structure.has("nativeBlueprint"))
                throw new IOException(text("native.no_blueprint"));
            Tag decoded = BlockEntityJson.decode(structure.getAsJsonObject("nativeBlueprint"));
            if (!(decoded instanceof CompoundTag archive)) throw new IOException(text("native.blueprint_format"));
            var document = NativeBlueprintReader.read(archive);
            if (document.sublevels().isEmpty()) throw new IOException(text("native.blueprint_empty"));
            validateCaptureScope(structure, document.sublevels().size());
            var absent = MissingRegistryContentSanitizer.sanitizeNative(archive.copy(), level.registryAccess());
            if (!absent.isEmpty()) throw new IOException(text("native.dependencies", absent));
            for (var saved : document.sublevels()) {
                var blocks = PlotBlockDataReader.read(saved.plotTag(), saved.sourceMinBuildHeight());
                if (blocks.isEmpty())
                    throw new IOException(text("native.empty_body"));
                // Blueprint material summaries also use id/count but refer to blocks without items
                // (for example encased pipes). Only device/moving-assembly payloads contain cargo.
                for (var block : blocks)
                    if (block.blockEntityTag() != null) validateItemAndFluidIds(block.blockEntityTag(), level);
                for (var moving : saved.runtimeContraptions()) validateItemAndFluidIds(moving.contraptionTag(), level);
            }
            return archive;
        } catch (IOException e) { throw e; }
        catch (RuntimeException e) { throw new IOException(text("native.blueprint_invalid"), e); }
    }

    static void validateCaptureScope(JsonObject structure, int bodyCount) throws IOException {
        // Older captures used proximity=0 and may include an unrelated boat overlapping the berth.
        // Its UUID must not become an authorized deletion target during restoration.
        if (bodyCount > 1 && (!structure.has("captureScope")
                || !"connected-only-v1".equals(structure.get("captureScope").getAsString())))
            throw new IOException(text("native.legacy_scope_ambiguous"));
    }

    /** Caller owns the transaction and must record all newly allocated bodies before committing.
     * In the pinned toolgun release runtimeRestoredImmediately actually contains the queued flag;
     * wait for the observer and our audit instead of trusting that misleading accessor.
     */
    public static NativeBlueprintPlacementResult place(ServerLevel level, CompoundTag archive, BlockPos anchor,
                                                       UUID playerId, BlueprintPlacementObserver observer) throws IOException {
        String prefix = "DBV-recovery-" + UUID.randomUUID() + "/";
        var staged = stageArchive(archive, prefix);
        var placed = NativeBlueprintPlacementService.place(level, anchor, Direction.UP, text("native.restore_label"),
                BlueprintArchiveCodec.encode(staged), anchor.getX(), anchor.getY(), anchor.getZ(),
                0, 100, 0, 0, 0, PlacementSnapMode.HIT, null,
                BlueprintVerticalPlacement.alignMinimumCenter(anchor.getY() + .5, Double.NaN), playerId, observer);
        identifyPlacedBodies(level, archive, placed.rootSubLevel(), prefix);
        applyStoredContents(level, archive, placed.rootSubLevel());
        return placed;
    }

    /** Native allocation preserves names but does not expose its UUID map or copy arbitrary userData.
     * Use unique temporary names only across this synchronous call; immediately restore real names and
     * retain an adapter marker through the transaction. Identical left/right child hulls must not swap cargo.
     */
    static CompoundTag stageArchive(CompoundTag archive, String prefix) {
        var staged = archive.copy();
        for (var value : staged.getList("sublevels", Tag.TAG_COMPOUND)) {
            var body = (CompoundTag) value;
            body.putBoolean("AstDisableStructureCollision", true);
            body.putString("name", prefix + body.getUUID("sublevel_id"));
        }
        return staged;
    }

    private static void identifyPlacedBodies(ServerLevel level, CompoundTag archive, ServerSubLevel root,
                                             String prefix) throws IOException {
        var expected = new HashMap<String, SavedSubLevelBlueprint>();
        for (var saved : NativeBlueprintReader.read(archive).sublevels())
            expected.put(prefix + saved.blueprintId(), saved);
        for (var body : SubLevelContainer.getContainer(level).getAllSubLevels()) {
            var saved = expected.remove(body.getName());
            if (saved == null) continue;
            var previous = body.getUserDataTag();
            var data = previous == null ? new CompoundTag() : previous.copy();
            data.putUUID(RECOVERY_SOURCE, saved.blueprintId());
            data.putUUID(RECOVERY_ROOT, root.getUniqueId());
            body.setUserDataTag(data);
            body.setName(saved.name());
        }
        if (!expected.isEmpty()) throw new IOException(text("native.connected_match"));
    }

    /** Only call after the durable recovery journal is finished. A crash before that point must
     * leave the exact source-to-candidate mapping available to finish the committed transaction.
     * These markers are adapter bookkeeping, not authoritative vessel/storage data.
     */
    public static void clearRecoveryTags(ServerLevel level, ServerSubLevel root) {
        for (var body : SubLevelContainer.getContainer(level).getAllSubLevels()) {
            var data = body.getUserDataTag();
            if (data == null || !data.hasUUID(RECOVERY_ROOT) || !data.getUUID(RECOVERY_ROOT).equals(root.getUniqueId())) continue;
            var clean = data.copy(); clean.remove(RECOVERY_SOURCE); clean.remove(RECOVERY_ROOT);
            body.setUserDataTag(clean);
        }
    }

    /** Call only after old bodies have retired. Restore each body's saved collision mode without
     * replacing userData, which also contains the new primary-vessel identity and placement flags.
     */
    public static void restoreCollision(ServerLevel level, CompoundTag archive, ServerSubLevel candidate) throws IOException {
        for (var pair : matchBodies(level, archive, candidate)) {
            if (SubLevelCollisionToggleManager.isCollisionDisabled(pair.body()) != pair.saved().collisionDisabled())
                ServerServices.SUBLEVEL_COLLISION.toggle(level, pair.body());
            if (SubLevelCollisionToggleManager.isCollisionDisabled(pair.body()) != pair.saved().collisionDisabled())
                throw new IOException(text("native.collision_restore"));
        }
    }

    /** Reapply snapshot resources after upstream's delayed machinery import, before the final audit.
     * Only the new candidate is touched. Its remapped links and rebuilt kinetic network are preserved.
     */
    public static void applyStoredContents(ServerLevel level, CompoundTag archive, ServerSubLevel restored) throws IOException {
        var pairs = matchBodies(level, archive, restored);
        for (var pair : pairs) {
            var target = PlotBlockTransform.capture(pair.body());
            var sourceOrigin = sourceOrigin(level, pair.saved());
            for (var block : PlotBlockDataReader.read(pair.saved().plotTag(), pair.saved().sourceMinBuildHeight())) {
                var expected = block.blockEntityTag();
                if (expected == null || expected.isEmpty()) continue;
                BlockPos global = target.toGlobalBlockPos(block.blockPos());
                BlockEntity entity = level.getBlockEntity(global);
                if (entity == null) throw new IOException(text("native.equipment_missing_detail",
                        block.blockPos().toShortString(), expected.getString("id"), global.toShortString(),
                        level.getBlockState(global).toString(), level.hasChunkAt(global), pair.body().getUniqueId()));
                var current = entity.saveWithFullMetadata(level.registryAccess());
                var patch = current.copy();
                copyResourceFields(expected, patch);
                relocateKnownReferences(expected, patch, sourceOrigin, target);
                if (!patch.equals(current)) {
                    entity.loadWithComponents(patch, level.registryAccess());
                    if (entity instanceof com.maxenonyme.createsubmarine.submarine.block.entity.ElectrolyzerBlockEntity electrolyzer)
                        restoreExactEnergy(electrolyzer.energyStorage, level.registryAccess(), expected);
                    entity.setChanged();
                    level.sendBlockUpdated(global, entity.getBlockState(), entity.getBlockState(), 3);
                }
            }
            for (var saved : pair.saved().runtimeContraptions()) {
                var controller = level.getBlockEntity(target.toGlobalBlockPos(saved.controllerLocalPos()));
                if (controller instanceof com.simibubi.create.content.contraptions.bearing.MechanicalBearingBlockEntity bearing) {
                    var moving = bearing.getMovedContraption();
                    // First call can precede upstream's queued entity import; verify() later rejects a missing entity.
                    if (moving == null || !moving.isAlive() || moving.getContraption() == null) continue;
                    var contraption = moving.getContraption();
                    // Older toolgun imports kept the source Anchor although the bearing itself had moved.
                    // A controlled bearing's anchor is defined by its controller and facing, not that stale cache.
                    var localAnchor = controlledAnchor(saved);
                    if (localAnchor != null) contraption.anchor = target.toGlobalBlockPos(localAnchor);
                    contraption.getStorage().read(saved.contraptionTag().copy(), level.registryAccess(), false, contraption);
                }
            }
        }
    }

    /** The pinned DeepSeas read() uses rate-limited extract/receive calls, so loading 1234 FE into an
     * empty device restores only 1000 FE. Deserialization restores persisted energy without transfer limits.
     */
    static void restoreExactEnergy(net.neoforged.neoforge.energy.EnergyStorage storage,
                                   net.minecraft.core.HolderLookup.Provider registries, CompoundTag snapshot) {
        if (snapshot.contains("Energy", Tag.TAG_ANY_NUMERIC))
            storage.deserializeNBT(registries, IntTag.valueOf(snapshot.getInt("Energy")));
    }

    /** Verify geometry, equipment settings, all persisted resource subtrees, and moving assemblies.
     * A mismatch throws; callers must never label a partial import as a complete restore.
     */
    public static Verification verify(ServerLevel level, CompoundTag archive, ServerSubLevel restored) throws IOException {
        var pairs = matchBodies(level, archive, restored);
        var issues = new ArrayList<String>();
        int blocks = 0, entities = 0, inventories = 0, contraptions = 0;
        for (var pair : pairs) {
            var transform = PlotBlockTransform.capture(pair.body());
            var expected = PlotBlockDataReader.read(pair.saved().plotTag(), pair.saved().sourceMinBuildHeight());
            var captured = BlueprintArchiveCodec.decode(NativeBlueprintCaptureService.captureById(
                    level, pair.body().getUniqueId(), text("native.audit_label"), -1).fileContents());
            var actualDocument = NativeBlueprintReader.read(captured);
            var actualRoot = actualDocument.sublevels().stream()
                    .filter(s -> s.blueprintId().equals(actualDocument.rootBlueprintId())).findFirst().orElseThrow();
            var actualBlocks = PlotBlockDataReader.read(actualRoot.plotTag(), actualRoot.sourceMinBuildHeight());
            var index = PlotBlockDataReader.indexStates(actualBlocks);
            if (index.size() != expected.size()) issues.add(text("native.block_count") + " " + expected.size() + " -> " + index.size());
            for (var block : expected) {
                blocks++;
                var state = index.get(block.blockPos());
                if (state == null || !sameTransferredState(block.state(), state))
                    issues.add(text("native.block_state", block.blockPos().toShortString(), block.state(), state));
                var metadata = block.blockEntityTag();
                if (metadata == null || metadata.isEmpty()) continue;
                entities++;
                var entity = level.getBlockEntity(transform.toGlobalBlockPos(block.blockPos()));
                if (entity == null) {
                    issues.add(text("native.equipment_missing_after", block.blockPos().toShortString())); continue;
                }
                var actual = entity.saveWithFullMetadata(level.registryAccess());
                var normalizedExpected = normalizeEquipment(metadata, sourceOrigin(level, pair.saved()));
                var normalizedActual = normalizeEquipment(actual, transform.toGlobalBlockPos(BlockPos.ZERO));
                if (!normalizedExpected.equals(normalizedActual)) {
                    var differing = differingValues(normalizedExpected, normalizedActual);
                    issues.add(text("native.equipment_state", metadata.getString("id"), block.blockPos().toShortString(), differing));
                }
                if (containsItems(metadata)) inventories++;
            }
            try {
                var expectedRuntime = runtimeIndex(pair.saved().runtimeContraptions(), sourceOrigin(level, pair.saved()), false);
                var actualRuntime = runtimeIndex(RuntimeContraptionBlueprint.capture(level, transform), transform.toGlobalBlockPos(BlockPos.ZERO), true);
                var keys = new TreeSet<>(expectedRuntime.keySet()); keys.addAll(actualRuntime.keySet());
                for (String key : keys) {
                    var wanted = expectedRuntime.get(key); var actual = actualRuntime.get(key);
                    if (!Objects.equals(wanted, actual)) issues.add(text("native.runtime_content") + " " + key + " "
                            + (wanted == null || actual == null ? "present " + (wanted != null) + " -> " + (actual != null)
                            : differingValues(wanted, actual)));
                }
                contraptions += expectedRuntime.size();
            } catch (IOException e) { issues.add(e.getMessage()); }
        }
        if (!issues.isEmpty()) {
            com.mojang.logging.LogUtils.getLogger().error("DBV recovery content audit: {} differences\n{}", issues.size(), String.join("\n", issues));
            String first = issues.getFirst();
            if (first.length() > 480) first = first.substring(0, 480) + "...";
            throw new IOException(text("native.audit_failed", first, issues.size()));
        }
        return new Verification(blocks, entities, inventories, contraptions);
    }

    private record BodyPair(SavedSubLevelBlueprint saved, ServerSubLevel body) {}

    /** Match root directly and children by the allocation receipt. Shape is not identity: identical
     * child bodies can have different cargo, fuel, settings, or collision modes.
     */
    private static List<BodyPair> matchBodies(ServerLevel level, CompoundTag archive, ServerSubLevel root) throws IOException {
        if (root == null || root.isRemoved()) throw new IOException(text("native.candidate_removed"));
        var document = NativeBlueprintReader.read(archive);
        // Zero also collects unrelated overlapping neighbors in upstream. Negative disables proximity.
        var bodies = new ArrayList<>(ConnectedSubLevelCollector.collect(level, root, -1));
        if (bodies.size() != document.sublevels().size()) throw new IOException(text("native.connected_count",
                document.sublevels().size(), bodies.size(), bodies.stream().map(b -> b.getUniqueId() + ":" + b.getName()
                        + ":marked=" + (b.getUserDataTag() != null && b.getUserDataTag().hasUUID(RECOVERY_SOURCE))).toList()));
        var pairs = new ArrayList<BodyPair>();
        for (var saved : document.sublevels()) {
            ServerSubLevel match = null;
            if (saved.blueprintId().equals(document.rootBlueprintId())) match = root;
            else for (var body : bodies) {
                if (body == root) continue;
                var data = body.getUserDataTag();
                if (data != null && data.hasUUID(RECOVERY_SOURCE) && data.getUUID(RECOVERY_SOURCE).equals(saved.blueprintId())
                        && data.hasUUID(RECOVERY_ROOT) && data.getUUID(RECOVERY_ROOT).equals(root.getUniqueId())) {
                    if (match != null) throw new IOException(text("native.connected_match"));
                    match = body;
                }
            }
            if (match == null || !bodies.remove(match)) throw new IOException(text("native.connected_match"));
            pairs.add(new BodyPair(saved, match));
        }
        return pairs;
    }

    private static boolean sameTransferredState(net.minecraft.world.level.block.state.BlockState expected,
                                                 net.minecraft.world.level.block.state.BlockState actual) {
        if (expected.getBlock() != actual.getBlock()) return false;
        // Same narrowly scoped derived-state exceptions as the existing block-refit transfer audit.
        Set<String> derived = switch (BuiltInRegistries.BLOCK.getKey(expected.getBlock()).toString()) {
            case "create:fluid_pipe" -> Set.of("north", "south", "east", "west", "up", "down");
            case "create:clutch", "create:gearshift" -> Set.of("powered");
            case "create:fluid_tank" -> Set.of("top", "bottom", "shape");
            // DeepSeas sets POWERED every tick from whether oxygen was produced, not the user's
            // enabled switch. Enabled, module contents, fuel, oxygen and energy remain audited below.
            case "create_submarine:electrolyzer" -> Set.of("powered");
            default -> Set.of();
        };
        return expected.getValues().entrySet().stream().allMatch(e -> derived.contains(e.getKey().getName())
                || Objects.equals(e.getValue(), actual.getValues().get(e.getKey())));
    }

    private static BlockPos sourceOrigin(ServerLevel level, SavedSubLevelBlueprint saved) {
        var plot = saved.plotTag();
        var origin = SubLevelContainer.getContainer(level).getOrigin();
        int bits = plot.getInt("log_size") + 4;
        return new BlockPos((plot.getInt("plot_x") + origin.x) << bits, 0,
                (plot.getInt("plot_z") + origin.y) << bits);
    }

    /** Known reference fields omitted by the toolgun's relocation adapters. */
    private static void relocateKnownReferences(CompoundTag source, CompoundTag destination, BlockPos origin,
                                                PlotBlockTransform transform) {
        String id = source.getString("id");
        for (String field : positionFields(id)) {
            Tag value = source.get(field);
            BlockPos position = readPosition(value);
            if (position != null) {
                var local = position.subtract(origin);
                writePosition(destination, field, value, transform.toGlobalBlockPos(local));
            }
        }
    }

    private static Set<String> positionFields(String id) {
        if (id.equals("create:fluid_tank") || id.equals("create:item_vault") || id.startsWith("createdieselgenerators:"))
            return Set.of("Controller", "LastKnownPos");
        if (id.equals("create_submarine:pump_controller")) return Set.of("Computer");
        if (id.equals("create:redstone_link")) return Set.of("LastKnownPosition");
        return Set.of();
    }

    static BlockPos readPosition(Tag tag) {
        if (tag instanceof IntArrayTag a && a.size() == 3) {
            int[] v = a.getAsIntArray(); return new BlockPos(v[0], v[1], v[2]);
        }
        if (tag instanceof LongTag l) return BlockPos.of(l.getAsLong());
        if (tag instanceof CompoundTag c && c.contains("X") && c.contains("Y") && c.contains("Z"))
            return new BlockPos(c.getInt("X"), c.getInt("Y"), c.getInt("Z"));
        return null;
    }

    private static void writePosition(CompoundTag target, String key, Tag old, BlockPos value) {
        if (old instanceof LongTag) target.putLong(key, value.asLong());
        else if (old instanceof IntArrayTag) target.putIntArray(key, new int[]{value.getX(), value.getY(), value.getZ()});
        else { var c = new CompoundTag(); c.putInt("X", value.getX()); c.putInt("Y", value.getY()); c.putInt("Z", value.getZ()); target.put(key, c); }
    }

    /** Never globally remove UUID/position/resource keys: item custom data must remain exact. */
    static CompoundTag normalizeEquipment(CompoundTag source, BlockPos origin) {
        var tag = source.copy();
        String id = tag.getString("id");
        for (String key : List.of("x", "y", "z", "keepPacked", "Network", "Source", "Speed", "NeedsSpeedUpdate", "network", "source", "speed")) tag.remove(key);
        for (String key : positionFields(id)) {
            Tag value = tag.get(key); var position = readPosition(value);
            if (position != null) writePosition(tag, key, value, position.subtract(origin));
        }
        if (id.equals("create:redstone_link")) remove(tag, "LastKnownPosition", "Transmit", "Receive", "ReceivedChanged");
        if (id.equals("create:fluid_tank") || id.equals("create:item_vault") || id.startsWith("createdieselgenerators:")) remove(tag, "LastKnownPos");
        if (id.equals("createdieselgenerators:powered_engine_shaft_block_entity")) remove(tag, "Engines", "GeneratedSpeed", "Direction", "Warmup");
        if (id.equals("create_submarine:command_sub")) remove(tag, "Depth", "Weakest", "Surface", "Fill", "Pumps", "Status", "AutoTarget");
        if (id.equals("create_submarine:oxygene_diffuser")) remove(tag, "ActiveTicks");
        if (id.equals("create_submarine:submarine_propeller")) remove(tag, "RotationSpeed");
        if (id.equals("simulated:velocity_sensor")) remove(tag, "AdjustedVelocity", "SignedRedstoneStrength");
        if (id.equals("create:mechanical_bearing") || id.equals("create:windmill_bearing")) remove(tag, "Angle");
        if (id.startsWith("aeronautics_utility_objects:")) {
            remove(tag, "LinkedSubLevel"); // New bodies get new UUIDs; the local link endpoint remains checked.
            Tag linked = tag.get("LinkedPos");
            BlockPos position = readPosition(linked);
            if (position != null && Math.abs(position.getX()) > 1_000_000)
                writePosition(tag, "LinkedPos", linked, position.subtract(origin));
        }
        if (id.equals("create:fluid_pipe") || id.equals("create:encased_fluid_pipe") || id.equals("create:mechanical_pump") || id.equals("create_submarine:pump_controller"))
            remove(tag, "north", "south", "east", "west", "up", "down");
        return tag;
    }

    private static void remove(CompoundTag tag, String... keys) { for (String key : keys) tag.remove(key); }

    /** Copy complete owner subtrees containing items/fluids, plus explicitly supported energy fields. */
    static void copyResourceFields(CompoundTag source, CompoundTag target) {
        for (String key : source.getAllKeys()) {
            Tag value = source.get(key);
            if (resourceField(key) || containsResource(value)) target.put(key, value.copy());
        }
        for (String key : new ArrayList<>(target.getAllKeys()))
            if (!source.contains(key) && resourceField(key)) target.remove(key);
    }

    private static boolean resourceField(String key) {
        return Set.of("Items", "items", "Inventory", "inventory", "container", "Tank", "WaterTank", "OxygenTank",
                "FluidTank", "fluidTank", "Energy", "energy", "EnergyStored", "energyStored", "Module", "Fuel", "fuel").contains(key);
    }

    private static boolean containsResource(Tag tag) {
        if (tag instanceof CompoundTag c) {
            if (c.contains("id", Tag.TAG_STRING) && (c.contains("count") || c.contains("amount"))) return true;
            for (String key : c.getAllKeys()) if (containsResource(c.get(key))) return true;
        } else if (tag instanceof ListTag list) for (Tag value : list) if (containsResource(value)) return true;
        return false;
    }

    private static boolean containsItems(Tag tag) {
        if (tag instanceof CompoundTag c) {
            if (c.contains("id", Tag.TAG_STRING) && c.contains("count")) return true;
            for (String key : c.getAllKeys()) if (containsItems(c.get(key))) return true;
        } else if (tag instanceof ListTag list) for (Tag value : list) if (containsItems(value)) return true;
        return false;
    }

    private static void validateItemAndFluidIds(Tag tag, ServerLevel level) throws IOException {
        if (tag instanceof CompoundTag c) {
            if (c.contains("id", Tag.TAG_STRING)) {
                var id = ResourceLocation.tryParse(c.getString("id"));
                if (c.contains("count") && (id == null || !BuiltInRegistries.ITEM.containsKey(id)))
                    throw new IOException(text("native.item_missing", c.getString("id")));
                if (c.contains("amount") && (id == null || !BuiltInRegistries.FLUID.containsKey(id)))
                    throw new IOException(text("native.fluid_missing", c.getString("id")));
                var ops = level.registryAccess().createSerializationContext(NbtOps.INSTANCE);
                if (c.contains("count")) {
                    var parsed = net.minecraft.world.item.ItemStack.CODEC.parse(ops, c);
                    if (parsed.error().isPresent()) throw new IOException(text("native.item_decode", c.getString("id"), parsed.error().get().message()));
                }
                if (c.contains("amount")) {
                    var parsed = net.neoforged.neoforge.fluids.FluidStack.CODEC.parse(ops, c);
                    if (parsed.error().isPresent()) throw new IOException(text("native.fluid_decode", c.getString("id"), parsed.error().get().message()));
                }
            }
            for (String key : c.getAllKeys()) validateItemAndFluidIds(c.get(key), level);
        } else if (tag instanceof ListTag list) for (Tag value : list) validateItemAndFluidIds(value, level);
    }

    private static Map<String, CompoundTag> runtimeIndex(List<RuntimeContraptionBlueprint> runtime, BlockPos origin, boolean actual) throws IOException {
        var result = new TreeMap<String, CompoundTag>();
        for (var c : runtime) {
            String key = c.kind() + ":" + c.controllerLocalPos().toShortString() + ":" + c.entityClassName();
            var content = normalizeContraption(c.contraptionTag(), origin);
            var expectedAnchor = controlledAnchor(c);
            if (expectedAnchor != null) {
                var raw = content.get("Anchor");
                if (actual && !expectedAnchor.equals(readPosition(raw)))
                    throw new IOException(text("native.runtime_anchor"));
                if (raw != null) writePosition(content, "Anchor", raw, expectedAnchor);
            }
            if (result.put(key, content) != null) throw new IOException(text("native.runtime_duplicate"));
        }
        return result;
    }

    private static BlockPos controlledAnchor(RuntimeContraptionBlueprint c) {
        return c.kind().equals("create_controlled") && c.contraptionTag().contains("Facing")
                ? c.controllerLocalPos().relative(Direction.from3DDataValue(c.contraptionTag().getInt("Facing"))) : null;
    }

    static CompoundTag normalizeContraption(CompoundTag source, BlockPos origin) {
        var content = source.copy();
        // Rotation advances while upstream stabilizes the imported entity; block/inventory data must not change.
        remove(content, "UUID", "Motion", "Pos", "Rotation", "Stalled");
        Tag anchor = content.get("Anchor"); var position = readPosition(anchor);
        if (position != null) writePosition(content, "Anchor", anchor, position.subtract(origin));
        return content;
    }

    private static Map<String, String> differingValues(CompoundTag a, CompoundTag b) {
        var keys = new TreeSet<>(a.getAllKeys()); keys.addAll(b.getAllKeys());
        keys.removeIf(k -> Objects.equals(a.get(k), b.get(k)));
        var result = new TreeMap<String, String>();
        for (String key : keys) result.put(key, shortValue(a.get(key)) + " -> " + shortValue(b.get(key)));
        return result;
    }

    private static String shortValue(Tag value) {
        String text = String.valueOf(value);
        return text.length() > 400 ? text.substring(0, 400) + "..." : text;
    }
}
