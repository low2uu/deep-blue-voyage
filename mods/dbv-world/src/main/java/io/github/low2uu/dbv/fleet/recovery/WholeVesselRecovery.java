package io.github.low2uu.dbv.fleet.recovery;

import static io.github.low2uu.dbv.fleet.recovery.RecoveryMessages.text;
import com.enxv.aeronauticsstructuretool.blueprint.capture.ConnectedSubLevelCollector;
import com.enxv.aeronauticsstructuretool.blueprint.lifecycle.SubLevelRemovalCoordinator;
import com.enxv.aeronauticsstructuretool.blueprint.runtime.BlueprintPlacementObserver;
import com.google.gson.*;
import com.mojang.logging.LogUtils;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import io.github.low2uu.dbv.fleet.FleetModule;
import io.github.low2uu.dbv.fleet.dock.StarterSubmarine;
import io.github.low2uu.dbv.fleet.persistence.VesselDataStore;
import io.github.low2uu.dbv.world.WorldHeights;
import java.io.IOException;
import java.util.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

/** Copy, verify, commit identity, then retire the old carrier. Every durable stage is restartable. */
public final class WholeVesselRecovery {
    private static final dev.ryanhcode.sable.api.sublevel.ticket.SubLevelLoadingTicketType<net.minecraft.util.Unit> RECOVERY_TICKET=
            dev.ryanhcode.sable.api.sublevel.ticket.SubLevelLoadingTicketType.create(net.minecraft.resources.ResourceLocation.parse("dbv_world:recovery"),
                    com.mojang.serialization.Codec.unit(net.minecraft.util.Unit.INSTANCE));
    public static void initializeTickets(){ /* Register before Sable deserializes saved recovery tickets. */ }
    private final MinecraftServer server;
    private RecoveryJournal disk;
    private JsonObject transaction;
    private CompoundTag archive;
    private boolean initialized, callbackComplete, resuming, blocked, allocating;
    private String failure, lastStatus="";
    private long deadline;

    public WholeVesselRecovery(MinecraftServer server){this.server=server;}
    public boolean active(){return transaction!=null||blocked;}
    /** Loading a saved carrier never calls this fresh-allocation receipt. */
    public void recordAllocated(ServerSubLevel body){
        if(!allocating||transaction==null)return;
        var id=new JsonPrimitive(body.getUniqueId().toString());var ids=transaction.getAsJsonArray("candidates");
        if(ids.contains(id))return;ids.add(id);
        try{disk.write(transaction);}catch(IOException e){failure=e.getMessage();throw new java.io.UncheckedIOException(e);}
    }
    /** Track native split descendants so a rejected import cannot leave duplicate cargo behind. */
    public void recordSplit(ServerSubLevel source,ServerSubLevel child){
        if(transaction==null)return;
        var sourceId=new JsonPrimitive(source.getUniqueId().toString());var childId=new JsonPrimitive(child.getUniqueId().toString());
        var ids=transaction.getAsJsonArray("candidates");
        boolean candidate=ids.contains(sourceId);
        if(!candidate)ids=transaction.getAsJsonArray("originalBodies");
        if(!ids.contains(sourceId)||ids.contains(childId))return;
        ids.add(childId);if(candidate)SubLevelContainer.getContainer(child.getLevel()).addForceLoadTicket(child,RECOVERY_TICKET,net.minecraft.util.Unit.INSTANCE);
        try{disk.write(transaction);}catch(IOException e){failure=e.getMessage();LogUtils.getLogger().error("恢复候选拆分记录未保存",e);}
    }
    public static boolean nearDock(ServerPlayer player){
        var p=player.position();return player.serverLevel()==player.getServer().overworld()
                &&p.x>=-12&&p.x<=140&&p.z>=100&&p.z<=252
                &&p.y>=WorldHeights.fromLegacy(1)&&p.y<=WorldHeights.fromLegacy(112);
    }
    public boolean hasSnapshot(FleetModule m) throws IOException {
        if(m.registry().flagship().isEmpty())return false;
        return java.nio.file.Files.exists(m.storage().store(m).directory().resolve("recovery-checkpoint.json"))
                ||VesselDataStore.completeVessel(m.storage().store(m).read());
    }
    public String accessProblem(ServerPlayer player,FleetModule m){
        if(!player.isCreative()||!player.hasPermissions(2))return text("flow.permission");
        if(!nearDock(player))return text("flow.near_dock");
        if(active())return status(m);
        if(m.registry().flagship().isEmpty())return text("flow.unregistered");
        if(m.refit().active())return text("flow.finish_refit");
        try {if(!hasSnapshot(m))return text("flow.no_archive");}
        catch(IOException e){return text("flow.archive_read_failed", e.getMessage());}
        return null;
    }
    public String status(FleetModule m){
        if(blocked)return text("flow.blocked", lastStatus);
        if(transaction!=null)return text("flow.working");
        return lastStatus.isEmpty()?text("flow.ready"):lastStatus;
    }
    public String restoreLatest(ServerPlayer player,FleetModule m) throws IOException {
        String problem=accessProblem(player,m);if(problem!=null)throw new IllegalStateException(problem);
        return restoreAtDock(m);
    }
    /** Server-side operation shared by the UI, commands and integration acceptance. */
    public String restoreAtDock(FleetModule m) throws IOException {
        initialize(m);
        if(active()||m.refit().active())throw new IOException(text("flow.other_transaction"));
        var identity=m.registry().flagship().orElseThrow();var store=m.storage().store(m);
        var saved=store.recoveryCheckpoint();
        var source=saved==null?store.read():saved.getAsJsonObject("payload");
        if(!VesselDataStore.completeVessel(source))throw new IOException(text("flow.no_checkpoint"));
        archive=NativeVesselArchive.decodeAndValidate(source.getAsJsonObject("modules").getAsJsonObject("structure"),server.overworld());
        // Loading is distinct from destruction. If the original is unloaded, bring it back before replacement.
        var old=load(identity.bodyId());var oldBodies=new ArrayList<ServerSubLevel>();
        if(old!=null)oldBodies.addAll(ConnectedSubLevelCollector.collect(server.overworld(),old,-1));
        // The root can be destroyed while attached secondary bodies survive independently.
        for(var id:NativeVesselArchive.sourceBodyIds(archive)){
            var remaining=load(id);if(remaining!=null&&!oldBodies.contains(remaining))oldBodies.add(remaining);
        }
        var tx=new JsonObject();tx.addProperty("phase","PREPARED");tx.addProperty("vesselId",identity.vesselId().toString());
        tx.addProperty("originalBody",identity.bodyId().toString());tx.addProperty("startedAt",java.time.Instant.now().toString());
        tx.add("source",source.deepCopy());tx.add("before",store.recoveryBefore(source));
        var originals=new JsonArray();for(var body:oldBodies)originals.add(body.getUniqueId().toString());tx.add("originalBodies",originals);
        tx.add("candidates",new JsonArray());
        disk.write(tx);transaction=tx;resuming=false;callbackComplete=false;failure=null;deadline=server.overworld().getGameTime()+600;
        try {
            var anchor=StarterSubmarine.recoveryPosition();
            for(int x=anchor.getX()-64;x<=anchor.getX()+64;x+=16)for(int z=anchor.getZ()-64;z<=anchor.getZ()+64;z+=16)
                server.overworld().getChunk(x>>4,z>>4);
            allocating=true;
            var result=NativeVesselArchive.place(server.overworld(),archive,anchor,null,new BlueprintPlacementObserver(){
                public void onCompleted(){if(transaction==tx)callbackComplete=true;}
                public void onFailed(String reason){if(transaction==tx)failure=reason;}
            });
            allocating=false;
            transaction.addProperty("candidate",result.rootSubLevel().getUniqueId().toString());
            pinCandidates(m);transaction.addProperty("phase","PLACED");disk.write(transaction);
            NativeVesselArchive.applyStoredContents(server.overworld(),archive,result.rootSubLevel());
            // Upstream's misleading boolean means "queued", not necessarily "finished".
            saveWorld();
            lastStatus=text("flow.importing");
        } catch(Exception e){
            allocating=false;
            pinCandidates(m);disk.write(transaction);rollback(m,e);throw new IOException(text("flow.import_failed"),e);
        }
        return text("flow.started");
    }
    private void pinCandidates(FleetModule m){
        for(var id:transaction.getAsJsonArray("candidates")){
            var b=m.adapter().find(server.overworld(),UUID.fromString(id.getAsString()));
            if(b!=null)SubLevelContainer.getContainer(b.getLevel()).addForceLoadTicket(b,RECOVERY_TICKET,net.minecraft.util.Unit.INSTANCE);
        }
    }
    private void initialize(FleetModule m) throws IOException {
        if(initialized||m.registry().flagship().isEmpty())return;
        disk=new RecoveryJournal(m.storage().store(m).directory());
        try{
            var loaded=disk.read();
            if(loaded!=null&&!m.registry().flagship().orElseThrow().vesselId().toString().equals(loaded.get("vesselId").getAsString()))throw new IOException(text("flow.wrong_identity"));
            transaction=loaded;initialized=true;resuming=transaction!=null;
        }catch(IOException e){initialized=true;blocked=true;lastStatus=e.getMessage();throw e;}
    }
    public void tick(FleetModule m){
        try {
            initialize(m);if(transaction==null||blocked)return;
            UUID current=m.registry().flagship().orElseThrow().bodyId();
            UUID candidate=transaction.has("candidate")?UUID.fromString(transaction.get("candidate").getAsString()):null;
            if(candidate!=null&&current.equals(candidate)){finishCommitted(m);return;}
            if(resuming){
                // Before identity commit, an interrupted import is discarded; retry uses the same pinned source.
                rollback(m,new IOException(text("flow.interrupted")));return;
            }
            if(failure!=null)throw new IOException(text("flow.native_failed", failure));
            if(server.overworld().getGameTime()>deadline)throw new IOException(text("flow.runtime_timeout"));
            if(!callbackComplete)return;
            var body=load(candidate);if(body==null)throw new IOException(text("flow.candidate_unloaded"));
            NativeVesselArchive.applyStoredContents(server.overworld(),archive,body);
            var receipt=NativeVesselArchive.verify(server.overworld(),archive,body);
            transaction.addProperty("blocksVerified",receipt.blocks());transaction.addProperty("equipmentVerified",receipt.blockEntities());
            transaction.addProperty("contraptionsVerified",receipt.runtimeContraptions());
            saveWorld();transaction.addProperty("phase","VERIFIED");disk.write(transaction);
            m.rebindForRefit(UUID.fromString(transaction.get("originalBody").getAsString()),body);
            transaction.addProperty("phase","COMMITTED");disk.write(transaction);
            finishCommitted(m);
        } catch(Exception e){
            try {
                if(transaction!=null&&transaction.has("candidate")&&m.registry().flagship().orElseThrow().bodyId().toString().equals(transaction.get("candidate").getAsString())){
                    blocked=true;lastStatus=e.getMessage(); // Committed identity is never rolled back to a retired ship.
                }else rollback(m,e);
            }catch(Exception cleanup){blocked=true;lastStatus=e.getMessage()+"；"+cleanup.getMessage();e.addSuppressed(cleanup);}
            LogUtils.getLogger().error("整艇恢复未完成",e);
        }
    }
    private void finishCommitted(FleetModule m) throws IOException {
        var body=load(UUID.fromString(transaction.get("candidate").getAsString()));if(body==null)throw new IOException(text("flow.committed_unloaded"));
        var restored=transaction.getAsJsonObject("source").deepCopy();var modules=restored.getAsJsonObject("modules");
        var identity=m.registry().flagship().orElseThrow();var vessel=new JsonObject();vessel.addProperty("name",identity.name());
        m.adapter().mark(body,identity);
        vessel.addProperty("dimension",identity.dimension());vessel.addProperty("bodyId",identity.bodyId().toString());vessel.addProperty("generation",identity.generation());
        modules.add("vessel",vessel);var nav=new JsonObject();nav.addProperty("schemaVersion",1);nav.addProperty("departed",false);modules.add("navigation",nav);
        m.storage().store(m).replaceFromRecovery(restored);
        m.starter().adoptRecoveredBody(body,m);modules.add("berth",m.starter().berthSnapshot());modules.add("refit",m.refit().snapshot());
        m.storage().store(m).replaceFromRecovery(restored);
        // Retire every old connected carrier only after the identity and the replacement are durable.
        var retired=new ArrayList<ServerSubLevel>();
        for(var value:transaction.getAsJsonArray("originalBodies")){
            UUID id=UUID.fromString(value.getAsString());if(id.equals(body.getUniqueId()))throw new IOException(text("flow.carrier_conflict"));
            var old=load(id);if(old!=null)retired.add(old);
        }
        remove(retired);
        var original=NativeVesselArchive.decodeAndValidate(transaction.getAsJsonObject("source").getAsJsonObject("modules").getAsJsonObject("structure"),server.overworld());
        NativeVesselArchive.restoreCollision(server.overworld(),original,body);
        // Capture the new generation's plot coordinates, carrier IDs and restored collision settings.
        modules.add("structure",m.storage().structure(m));modules.add("storage",m.storage().scan(m));
        m.storage().store(m).replaceFromRecovery(restored);m.storage().store(m).checkpoint(restored,false);
        for(var value:transaction.getAsJsonArray("candidates")){
            var allocated=load(UUID.fromString(value.getAsString()));if(allocated!=null)
                SubLevelContainer.getContainer(allocated.getLevel()).removeForceLoadTicket(allocated,RECOVERY_TICKET,net.minecraft.util.Unit.INSTANCE);
        }
        saveWorld();disk.finish("committed");
        NativeVesselArchive.clearRecoveryTags(server.overworld(),body);
        lastStatus=text("flow.complete");
        transaction=null;archive=null;resuming=false;callbackComplete=false;
        server.getPlayerList().broadcastSystemMessage(net.minecraft.network.chat.Component.literal(lastStatus),false);
    }
    private void rollback(FleetModule m,Exception reason) throws IOException {
        if(transaction==null)return;
        m.reconcileRecoveryRegistry();
        if(transaction.has("candidate")&&m.registry().flagship().orElseThrow().bodyId().toString().equals(transaction.get("candidate").getAsString()))
            throw new IOException(text("flow.commit_pending_finish"));
        var candidates=new ArrayList<ServerSubLevel>();
        for(var id:transaction.getAsJsonArray("candidates")){
            var candidate=load(UUID.fromString(id.getAsString()));if(candidate!=null)candidates.add(candidate);
        }
        remove(candidates);saveWorld();disk.finish("rolled-back");
        transaction=null;archive=null;resuming=false;callbackComplete=false;failure=null;lastStatus=reason.getMessage();
    }
    private ServerSubLevel load(UUID id) throws IOException {
        if(id==null)return null;
        for(var level:server.getAllLevels()){
            var body=NativeVesselArchive.findPersistedBody(level,id);if(body!=null)return body;
        }
        return null;
    }
    private void remove(List<ServerSubLevel> bodies){
        var groups=new HashMap<ServerLevel,List<ServerSubLevel>>();for(var b:bodies)groups.computeIfAbsent(b.getLevel(),k->new ArrayList<>()).add(b);
        for(var e:groups.entrySet())SubLevelRemovalCoordinator.remove(e.getKey(),SubLevelContainer.getContainer(e.getKey()),e.getValue());
    }
    private void saveWorld() throws IOException {if(!server.saveEverything(false,true,true))throw new IOException(text("flow.world_save_failed"));}
}
