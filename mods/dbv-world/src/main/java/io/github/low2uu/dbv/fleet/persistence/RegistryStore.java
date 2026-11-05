package io.github.low2uu.dbv.fleet.persistence;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.github.low2uu.dbv.fleet.domain.FlagshipRegistry;
import io.github.low2uu.dbv.fleet.domain.VesselIdentity;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.Optional;
import java.util.UUID;

/** World-local registry. Invalid existing files fail closed instead of creating a second ship. */
public final class RegistryStore {
    private final Path file;

    public RegistryStore(Path worldRoot) { file = worldRoot.resolve("dbv/fleet/registry.json"); }

    /** Only a verified whole-vessel transaction may settle an interrupted carrier swap. */
    public void recoverWholeVesselRebind() throws IOException {
        Path pending=file.resolveSibling("registry.refit.pending.json");
        if(!Files.exists(pending)||!Files.exists(file))return;
        try {
            var current=JsonParser.parseString(Files.readString(file)).getAsJsonObject();
            var ship=current.getAsJsonObject("flagship");UUID vessel=UUID.fromString(string(ship,"vesselId"));
            Path directory=file.getParent().resolve("vessels").resolve(vessel.toString());
            var journal=new io.github.low2uu.dbv.fleet.recovery.RecoveryJournal(directory).read();
            if(journal==null||!java.util.Set.of("VERIFIED","COMMITTED").contains(string(journal,"phase")))return;
            if(!string(journal,"originalBody").equals(string(ship,"bodyId"))||!string(journal,"vesselId").equals(vessel.toString()))return;
            boolean valid=false;
            try {
                var next=JsonParser.parseString(Files.readString(pending)).getAsJsonObject();var replacement=next.getAsJsonObject("flagship");
                var expected=current.deepCopy();var expectedShip=expected.getAsJsonObject("flagship");
                expectedShip.addProperty("bodyId",string(journal,"candidate"));expectedShip.addProperty("generation",Math.addExact(integer(ship,"generation"),1));
                valid=next.equals(expected)&&string(replacement,"vesselId").equals(vessel.toString());
            }catch(RuntimeException ignored){ /* A partial write has not committed; retain it as evidence. */ }
            if(valid)Files.move(pending,file,StandardCopyOption.ATOMIC_MOVE,StandardCopyOption.REPLACE_EXISTING);
            else {
                Path history=directory.resolve("recovery-history");Files.createDirectories(history);
                Files.move(pending,history.resolve(UUID.randomUUID()+"-registry-interrupted.json"),StandardCopyOption.ATOMIC_MOVE);
            }
        }catch(RuntimeException e){throw new IOException("整艇恢复登记无法核对，保留原文件",e);}
    }

    public FlagshipRegistry load() throws IOException {
        if(Files.exists(file.resolveSibling("registry.refit.pending.json")))throw new IOException("主艇换装登记中断，请保留船坞档案");
        if (!Files.exists(file)) {
            if (Files.exists(file.resolveSibling("registration.lock")) || Files.exists(file.resolveSibling("registry.pending.json"))) {
                throw new IOException("Unfinished flagship registration; preserve pending files for inspection");
            }
            return FlagshipRegistry.empty();
        }
        if (Files.size(file) > 16_384) { throw new IOException("Flagship registry exceeds the metadata size limit"); }
        try {
            JsonObject root = JsonParser.parseString(Files.readString(file)).getAsJsonObject();
            if (integer(root, "schemaVersion") != 1) { throw new IllegalArgumentException("Unknown schema"); }
            JsonObject vessel = root.getAsJsonObject("flagship");
            return new FlagshipRegistry(Optional.of(new VesselIdentity(
                    UUID.fromString(string(vessel, "vesselId")),
                    UUID.fromString(string(vessel, "bodyId")),
                    string(vessel, "dimension"), string(vessel, "name"), integer(vessel, "generation"))));
        } catch (RuntimeException exception) {
            throw new IOException("Invalid flagship registry; registration remains locked", exception);
        }
    }

    /** Compare-and-replace the physics carrier while retaining the campaign identity. */
    public FlagshipRegistry rebind(UUID expectedBody,UUID nextBody) throws IOException {
        var current=load().flagship().orElseThrow();
        if(!current.bodyId().equals(expectedBody))throw new IOException("主艇载体已变化，拒绝覆盖登记");
        var next=new VesselIdentity(current.vesselId(),nextBody,current.dimension(),current.name(),Math.addExact(current.generation(),1));
        var root=JsonParser.parseString(Files.readString(file)).getAsJsonObject();
        var ship=root.getAsJsonObject("flagship");ship.addProperty("bodyId",nextBody.toString());ship.addProperty("generation",next.generation());
        Path pending=file.resolveSibling("registry.refit.pending.json");
        try(var channel=FileChannel.open(pending,StandardOpenOption.CREATE_NEW,StandardOpenOption.WRITE)){
            var bytes=StandardCharsets.UTF_8.encode(root.toString());while(bytes.hasRemaining())channel.write(bytes);channel.force(true);
        }
        Files.move(pending,file,StandardCopyOption.ATOMIC_MOVE,StandardCopyOption.REPLACE_EXISTING);
        return new FlagshipRegistry(Optional.of(next));
    }

    private static String string(JsonObject object, String key) {
        var value = object.getAsJsonPrimitive(key);
        if (!value.isString()) { throw new IllegalArgumentException("Expected a string: " + key); }
        return value.getAsString();
    }

    private static long integer(JsonObject object, String key) {
        var value = object.getAsJsonPrimitive(key);
        if (!value.isNumber()) { throw new IllegalArgumentException("Expected a number: " + key); }
        return value.getAsBigDecimal().longValueExact();
    }

    /** Initial registration only. No replacement of an existing registry is permitted. */
    public void create(FlagshipRegistry registry) throws IOException {
        VesselIdentity vessel = registry.flagship().orElseThrow();
        Files.createDirectories(file.getParent());
        Path lock = file.resolveSibling("registration.lock");
        // A leftover lock is deliberately retained after a failed transaction for manual investigation.
        try (FileChannel ignored = FileChannel.open(lock, StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE)) {
            if (Files.exists(file)) { throw new IOException("A flagship registry already exists"); }
            JsonObject root = new JsonObject();
            root.addProperty("schemaVersion", 1);
            JsonObject ship = new JsonObject();
            ship.addProperty("vesselId", vessel.vesselId().toString());
            ship.addProperty("bodyId", vessel.bodyId().toString());
            ship.addProperty("dimension", vessel.dimension());
            ship.addProperty("name", vessel.name());
            ship.addProperty("generation", vessel.generation());
            root.add("flagship", ship);
            Path temporary = file.resolveSibling("registry.pending.json");
            try (FileChannel channel = FileChannel.open(temporary, StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE)) {
                ByteBuffer bytes = StandardCharsets.UTF_8.encode(root.toString());
                while (bytes.hasRemaining()) { channel.write(bytes); }
                channel.force(true);
            }
            // Refuse unsupported atomic moves; never fall back to a partially-written authoritative file.
            Files.move(temporary, file, StandardCopyOption.ATOMIC_MOVE);
        }
        Files.delete(lock);
    }
}
