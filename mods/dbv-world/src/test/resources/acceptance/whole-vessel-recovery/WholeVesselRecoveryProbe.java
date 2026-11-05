package dbv.acceptance;

import com.enxv.aeronauticsstructuretool.blueprint.lifecycle.SubLevelRemovalCoordinator;
import com.google.gson.*;
import com.maxenonyme.createsubmarine.CreateSubmarine;
import com.maxenonyme.createsubmarine.submarine.block.entity.ElectrolyzerBlockEntity;
import com.maxenonyme.createsubmarine.submarine.block.entity.OxygeneDiffuserBlockEntity;
import com.simibubi.create.content.fluids.tank.FluidTankBlockEntity;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.ryanhcode.sable.api.sublevel.ticket.SubLevelLoadingTicketType;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import io.github.low2uu.dbv.fleet.FleetModule;
import io.github.low2uu.dbv.fleet.dock.StarterSubmarine;
import io.github.low2uu.dbv.fleet.persistence.VesselDataStore;
import io.github.low2uu.dbv.fleet.recovery.NativeVesselArchive;
import io.github.low2uu.dbv.fleet.recovery.RecoveryJournal;
import java.nio.file.*;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.*;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Unit;
import net.minecraft.world.Container;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.storage.LevelResource;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.fluids.FluidStack;

/** Destructive acceptance fixture. It is a separate test-only mod and refuses every non-test world. */
@Mod("dbv_recovery_probe")
public final class WholeVesselRecoveryProbe {
    private static final Gson JSON=new GsonBuilder().setPrettyPrinting().create();
    private static final SubLevelLoadingTicketType<Unit> TICKET=SubLevelLoadingTicketType.create(
            ResourceLocation.parse("dbv_recovery_probe:acceptance"),com.mojang.serialization.Codec.unit(Unit.INSTANCE));
    private MinecraftServer server;
    private Path runDirectory,reports;
    private String phase,state="starting";
    private int ticks,startedAt;
    private boolean finished,fixtures;
    private UUID vesselId,oldBodyId;
    private JsonObject expected,splitEvidence;
    private long splitGeneration;
    private String splitCheckpoint;
    private Set<UUID> independentBodies=Set.of(),initialCandidates=Set.of();
    private Set<Path> previousTransactions=Set.of();

    public WholeVesselRecoveryProbe(){
        if(!Boolean.getBoolean("dbv.recoveryProbe"))throw new IllegalStateException("The recovery probe must never be installed in a normal server");
        NeoForge.EVENT_BUS.addListener(this::started);
        NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST,this::tick);
        NeoForge.EVENT_BUS.addListener(this::beforeTick);
    }
    private void started(ServerStartedEvent event){
        server=event.getServer();
        try{
            runDirectory=Path.of(System.getProperty("dbv.recoveryProbe.directory")).toAbsolutePath().normalize();
            Path actual=server.getWorldPath(LevelResource.ROOT).toAbsolutePath().normalize();
            if(!Files.isRegularFile(runDirectory.resolve(".dbv-recovery-probe"))||!actual.equals(runDirectory.resolve("server/world")))
                throw new IllegalStateException("Probe world is not the isolated acceptance world: "+actual);
            reports=runDirectory.resolve("reports");Files.createDirectories(reports);
            phase=System.getProperty("dbv.recoveryProbe.phase","recover");
            if(!Set.of("recover","restart","resume","splitrollback").contains(phase))throw new IllegalArgumentException("Unknown probe phase");
            server.overworld().getGameRules().getRule(GameRules.RULE_DOMOBSPAWNING).set(false,server);
            server.overworld().getGameRules().getRule(GameRules.RULE_DOFIRETICK).set(false,server);
            server.overworld().getGameRules().getRule(GameRules.RULE_RANDOMTICKING).set(0,server);
            for(int x=2;x<=6;x++)for(int z=8;z<=14;z++)server.overworld().setChunkForced(x,z,true);
            if(phase.equals("recover")){
                if(FleetModule.get(server).registry().flagship().isPresent())throw new IllegalStateException("Fresh recovery phase requires an empty test world");
                StarterSubmarine.requestNewWorld(server);state="wait-registration";
            }else{
                expected=JsonParser.parseString(Files.readString(reports.resolve("expected.json"))).getAsJsonObject();
                vesselId=UUID.fromString(expected.get("vesselId").getAsString());fixtures=!phase.equals("restart");state=phase.equals("restart")?"verify-restart":phase.equals("splitrollback")?"prepare-split":"resume-rollback";
            }
        }catch(Throwable failure){fail(failure);}
    }
    private void beforeTick(ServerTickEvent.Pre event){
        if(!fixtures||finished||event.getServer()!=server)return;
        try{
            // Test fixture only: stop shore refilling while comparing exact stored amounts. No production defaults change.
            var field=StarterSubmarine.class.getDeclaredField("failed");field.setAccessible(true);field.setBoolean(FleetModule.get(server).starter(),true);
        }catch(Throwable failure){fail(failure);}
    }
    private void tick(ServerTickEvent.Post event){
        if(server==null||event.getServer()!=server||finished)return;
        try{
            if(++ticks>2400)throw new IllegalStateException("Acceptance timed out in "+state);
            var m=FleetModule.get(server);
            for(var body:m.adapter().loaded(server.overworld()))SubLevelContainer.getContainer(server.overworld()).addForceLoadTicket(body,TICKET,Unit.INSTANCE);
            switch(state){
                case "wait-registration"->{
                    if(m.registry().flagship().isEmpty())return;
                    var body=m.refit().currentBody(m);if(body==null)return;
                    prepare(m,body);state="wait-fixtures";startedAt=ticks;
                }
                case "wait-fixtures"->{
                    if(ticks-startedAt<60)return; // Let the Sable topology, bounds and tank connectivity settle.
                    var body=m.refit().currentBody(m);
                    var loaded=m.adapter().loaded(server.overworld()).stream().filter(b->!b.isRemoved()).toList();
                    if(body==null||loaded.size()!=1||!loaded.getFirst().getUniqueId().equals(oldBodyId))
                        throw new IllegalStateException("Fixture must remain attached to the single source hull; loaded="+loaded.stream().map(b->b.getUniqueId().toString()).toList());
                    var handle=com.maxenonyme.createsubmarine.submarine.util.SablePhysicsHelper.getHandle(body);
                    var velocity=com.maxenonyme.createsubmarine.submarine.util.SablePhysicsHelper.getVelocity(handle);
                    var angular=com.maxenonyme.createsubmarine.submarine.util.SablePhysicsHelper.getAngularVelocity(handle);
                    if(!m.starter().moored(body)||velocity==null||angular==null||!Double.isFinite(velocity.length())||!Double.isFinite(angular.length()))throw new IllegalStateException("Fixture hull has no valid mooring/physics state");
                    if(velocity.length()>1||angular.length()>.15)return;
                    resetFixtureAmounts(m,body);
                    checkpoint(m,body);destroyAndRestore(m,body);state="wait-recovered";startedAt=ticks;
                }
                case "wait-recovered","wait-resumed"->{
                    if(m.recovery().active())return;
                    if(ticks<=startedAt)return;
                    verify(m,true);
                    if(state.equals("wait-recovered")){
                        expected.addProperty("restoredBodyId",m.registry().flagship().orElseThrow().bodyId().toString());
                        Files.writeString(reports.resolve("expected.json"),JSON.toJson(expected));
                    }
                    succeed(m,false);
                }
                case "verify-restart"->{
                    if(ticks<5)return;
                    verify(m,false);
                    var body=m.refit().currentBody(m);oldBodyId=body.getUniqueId();
                    fixtures=true;recordFixtureAmounts(body);checkpoint(m,body);
                    expected.addProperty("interruptedOriginalBodyId",oldBodyId.toString());Files.writeString(reports.resolve("expected.json"),JSON.toJson(expected));
                    destroyAndRestore(m,body);
                    if(!m.recovery().active())throw new IllegalStateException("No recovery transaction to interrupt");
                    succeed(m,true); // Stop before the next tick can commit the allocated replacement.
                }
                case "resume-rollback"->{
                    if(ticks<5||m.recovery().active())return;
                    oldBodyId=UUID.fromString(expected.get("interruptedOriginalBodyId").getAsString());
                    if(!m.registry().flagship().orElseThrow().bodyId().equals(oldBodyId))throw new IllegalStateException("Interrupted pre-commit import changed identity");
                    m.recovery().restoreAtDock(m);state="wait-resumed";startedAt=ticks;
                }
                case "prepare-split"->{
                    if(ticks<5||m.recovery().active())return;
                    var identity=m.registry().flagship().orElseThrow();
                    var body=NativeVesselArchive.findPersistedBody(server.overworld(),identity.bodyId());if(body==null)return;
                    prepareSplitRollback(m,body);state="wait-split-rollback";startedAt=ticks;
                }
                case "wait-split-rollback"->{
                    if(m.recovery().active()||ticks<=startedAt)return;
                    verifySplitRollback(m);succeed(m,false);
                }
                default->throw new IllegalStateException("Unknown probe state "+state);
            }
        }catch(Throwable failure){fail(failure);}
    }
    private void prepare(FleetModule m,ServerSubLevel body) throws Exception {
        fixtures=true;vesselId=m.registry().flagship().orElseThrow().vesselId();oldBodyId=body.getUniqueId();
        var level=server.overworld();var box=body.getPlot().getBoundingBox();
        var origin=new BlockPos(box.minX(),box.minY(),box.minZ());
        // Separate fluid circuits, but one physically connected hull. Floating samples are split into independent Sable bodies.
        BlockPos barrel=origin.offset(3,box.maxY()-box.minY()+2,3),gen=barrel.offset(0,0,3),diff=barrel.offset(0,0,6),tank=barrel.offset(0,0,9);
        attachFixtureRack(body,barrel,tank);
        level.setBlock(barrel,Blocks.BARREL.defaultBlockState(),3);
        level.setBlock(gen,block("create_submarine:electrolyzer").defaultBlockState(),3);
        level.setBlock(diff,block("create_submarine:oxygene_diffuser").defaultBlockState(),3);
        level.setBlock(tank,block("create:fluid_tank").defaultBlockState(),3);
        var inventory=(Container)Objects.requireNonNull(level.getBlockEntity(barrel));
        var diamonds=new ItemStack(Items.DIAMOND,17);diamonds.set(DataComponents.CUSTOM_NAME,Component.literal("整艇恢复·带组件物品"));
        var data=new CompoundTag();data.putString("purpose","whole-vessel-recovery");data.putLong("serial",9007199254740993L);data.putIntArray("values",new int[]{-3,7,123456});
        diamonds.set(DataComponents.CUSTOM_DATA,CustomData.of(data));inventory.setItem(0,diamonds);
        var pick=new ItemStack(Items.IRON_PICKAXE);pick.setDamageValue(37);inventory.setItem(8,pick);inventory.setChanged();
        var generator=(ElectrolyzerBlockEntity)Objects.requireNonNull(level.getBlockEntity(gen));
        generator.waterTank.setFluid(new FluidStack(Fluids.WATER,1379));generator.oxygenTank.setFluid(new FluidStack(CreateSubmarine.OXYGEN.get(),211));
        generator.energyStorage.deserializeNBT(level.registryAccess(),IntTag.valueOf(1234));
        var enabled=ElectrolyzerBlockEntity.class.getDeclaredField("isEnabled");enabled.setAccessible(true);enabled.setBoolean(generator,false);generator.setChanged();
        var diffuser=(OxygeneDiffuserBlockEntity)Objects.requireNonNull(level.getBlockEntity(diff));diffuser.oxygenTank.setFluid(new FluidStack(CreateSubmarine.OXYGEN.get(),733));diffuser.setChanged();
        var reservoir=(FluidTankBlockEntity)Objects.requireNonNull(level.getBlockEntity(tank));reservoir.getTankInventory().setFluid(new FluidStack(BuiltInRegistries.FLUID.get(ResourceLocation.parse("createdieselgenerators:diesel")),2487));reservoir.setChanged();
        expected=new JsonObject();expected.addProperty("vesselId",vesselId.toString());expected.addProperty("originalBodyId",oldBodyId.toString());
        for(var entry:Map.of("barrel",barrel,"generator",gen,"diffuser",diff,"dieselTank",tank).entrySet())expected.add(entry.getKey(),position(entry.getValue().subtract(origin)));
        expected.addProperty("water",1379);expected.addProperty("generatorOxygen",211);expected.addProperty("energy",1234);expected.addProperty("diffuserOxygen",733);expected.addProperty("diesel",2487);
    }
    private void attachFixtureRack(ServerSubLevel body,BlockPos first,BlockPos last){
        var level=server.overworld();var b=body.getPlot().getBoundingBox();BlockPos anchor=null;double distance=Double.POSITIVE_INFINITY;
        // Choose a real hull block that is the topmost block in its column. The post cannot cross existing devices.
        for(int x=b.minX();x<=b.maxX();x++)for(int z=b.minZ();z<=b.maxZ();z++)for(int y=b.maxY();y>=b.minY();y--){
            var candidate=new BlockPos(x,y,z);var state=level.getBlockState(candidate);if(state.isAir())continue;
            if(!state.hasBlockEntity()&&state.getFluidState().isEmpty()&&state.isSolidRender(level,candidate)){
                double d=candidate.distSqr(first);if(d<distance){distance=d;anchor=candidate;}
            }
            break;
        }
        if(anchor==null)throw new IllegalStateException("No exposed solid hull anchor for connected fixtures");
        int deck=first.getY()-1;var rack=new LinkedHashSet<BlockPos>();
        for(int y=anchor.getY()+1;y<=deck;y++)rack.add(new BlockPos(anchor.getX(),y,anchor.getZ()));
        for(int x=Math.min(anchor.getX(),first.getX());x<=Math.max(anchor.getX(),first.getX());x++)rack.add(new BlockPos(x,deck,anchor.getZ()));
        for(int z=Math.min(anchor.getZ(),first.getZ());z<=Math.max(anchor.getZ(),first.getZ());z++)rack.add(new BlockPos(first.getX(),deck,z));
        for(int z=first.getZ();z<=last.getZ();z++)rack.add(new BlockPos(first.getX(),deck,z));
        for(var pos:rack)if(!level.getBlockState(pos).isAir())throw new IllegalStateException("Fixture rack would overwrite an existing block: "+pos);
        for(var pos:List.of(first,first.offset(0,0,3),first.offset(0,0,6),last))if(!level.getBlockState(pos).isAir())throw new IllegalStateException("Fixture device would overwrite an existing block: "+pos);
        for(var pos:rack)level.setBlock(pos,Blocks.IRON_BLOCK.defaultBlockState(),3);
    }
    private void resetFixtureAmounts(FleetModule m,ServerSubLevel body){
        var origin=origin(body);var level=server.overworld();
        var gen=(ElectrolyzerBlockEntity)Objects.requireNonNull(level.getBlockEntity(pos("generator",origin)));
        gen.waterTank.setFluid(new FluidStack(Fluids.WATER,expected.get("water").getAsInt()));gen.oxygenTank.setFluid(new FluidStack(CreateSubmarine.OXYGEN.get(),expected.get("generatorOxygen").getAsInt()));
        gen.energyStorage.deserializeNBT(level.registryAccess(),IntTag.valueOf(expected.get("energy").getAsInt()));gen.setChanged();
        var diff=(OxygeneDiffuserBlockEntity)Objects.requireNonNull(level.getBlockEntity(pos("diffuser",origin)));diff.oxygenTank.setFluid(new FluidStack(CreateSubmarine.OXYGEN.get(),expected.get("diffuserOxygen").getAsInt()));diff.setChanged();
        var tank=(FluidTankBlockEntity)Objects.requireNonNull(level.getBlockEntity(pos("dieselTank",origin)));tank.getTankInventory().setFluid(new FluidStack(BuiltInRegistries.FLUID.get(ResourceLocation.parse("createdieselgenerators:diesel")),expected.get("diesel").getAsInt()));tank.setChanged();
    }
    private void recordFixtureAmounts(ServerSubLevel body){
        var origin=origin(body);var level=server.overworld();
        var gen=(ElectrolyzerBlockEntity)Objects.requireNonNull(level.getBlockEntity(pos("generator",origin)));
        expected.addProperty("water",gen.waterTank.getFluidAmount());expected.addProperty("generatorOxygen",gen.oxygenTank.getFluidAmount());expected.addProperty("energy",gen.energyStorage.getEnergyStored());
        var diff=(OxygeneDiffuserBlockEntity)Objects.requireNonNull(level.getBlockEntity(pos("diffuser",origin)));expected.addProperty("diffuserOxygen",diff.oxygenTank.getFluidAmount());
    }
    private static BlockPos origin(ServerSubLevel body){var b=body.getPlot().getBoundingBox();return new BlockPos(b.minX(),b.minY(),b.minZ());}
    private void checkpoint(FleetModule m,ServerSubLevel body) throws Exception {
        m.storage().capture(m);var store=m.storage().store(m);store.checkpoint(store.read(),false);
        expected.addProperty("contraptions",body.getPlot().getContraptions().size());
        expected.addProperty("blocks",m.storage().structure(m).getAsJsonArray("blocks").size());
        Files.writeString(reports.resolve("expected.json"),JSON.toJson(expected));save();
    }
    private static net.minecraft.world.level.block.Block block(String name){return BuiltInRegistries.BLOCK.get(ResourceLocation.parse(name));}
    private static JsonArray position(BlockPos p){var value=new JsonArray();value.add(p.getX());value.add(p.getY());value.add(p.getZ());return value;}
    private BlockPos pos(String key,BlockPos origin){var a=expected.getAsJsonArray(key);return origin.offset(a.get(0).getAsInt(),a.get(1).getAsInt(),a.get(2).getAsInt());}
    private void destroyAndRestore(FleetModule m,ServerSubLevel body) throws Exception {
        oldBodyId=body.getUniqueId();m.starter().releaseMooring();
        SubLevelRemovalCoordinator.remove(server.overworld(),SubLevelContainer.getContainer(server.overworld()),List.of(body));save();
        if(m.adapter().find(server.overworld(),oldBodyId)!=null)throw new IllegalStateException("Destruction fixture failed to remove the old hull");
        m.recovery().restoreAtDock(m);
    }
    private void verify(FleetModule m,boolean replacement) throws Exception {
        var identity=m.registry().flagship().orElseThrow();if(!identity.vesselId().equals(vesselId))throw new IllegalStateException("Permanent identity changed");
        var body=m.refit().currentBody(m);if(body==null)throw new IllegalStateException("Recovered hull missing; recovery status: "+m.recovery().status(m));
        if(replacement&&body.getUniqueId().equals(oldBodyId))throw new IllegalStateException("No replacement carrier was created");
        if(!replacement&&!identity.bodyId().toString().equals(expected.get("restoredBodyId").getAsString()))throw new IllegalStateException("Restart changed the carrier identity");
        var origin=origin(body);var level=server.overworld();
        var inventory=(Container)Objects.requireNonNull(level.getBlockEntity(pos("barrel",origin)),"Restored barrel missing");
        var diamonds=inventory.getItem(0);var custom=diamonds.get(DataComponents.CUSTOM_DATA);
        if(!diamonds.is(Items.DIAMOND)||diamonds.getCount()!=17||!diamonds.getHoverName().getString().equals("整艇恢复·带组件物品")||custom==null||custom.copyTag().getLong("serial")!=9007199254740993L||!Arrays.equals(custom.copyTag().getIntArray("values"),new int[]{-3,7,123456}))throw new IllegalStateException("Item components or counts changed");
        if(!inventory.getItem(8).is(Items.IRON_PICKAXE)||inventory.getItem(8).getDamageValue()!=37)throw new IllegalStateException("Item durability changed");
        var generator=(ElectrolyzerBlockEntity)Objects.requireNonNull(level.getBlockEntity(pos("generator",origin)));
        boolean shoreMayRun=phase.equals("restart")&&!replacement;
        if(!amount(generator.waterTank.getFluidAmount(),"water",shoreMayRun)||!amount(generator.oxygenTank.getFluidAmount(),"generatorOxygen",false)||!amount(generator.energyStorage.getEnergyStored(),"energy",shoreMayRun))
            throw new IllegalStateException("Electrolyzer state mismatch: water="+generator.waterTank.getFluidAmount()+", oxygen="+generator.oxygenTank.getFluidAmount()+", FE="+generator.energyStorage.getEnergyStored());
        var enabled=ElectrolyzerBlockEntity.class.getDeclaredField("isEnabled");enabled.setAccessible(true);if(enabled.getBoolean(generator))throw new IllegalStateException("Device disabled setting changed");
        var diffuser=(OxygeneDiffuserBlockEntity)Objects.requireNonNull(level.getBlockEntity(pos("diffuser",origin)));
        if(!amount(diffuser.oxygenTank.getFluidAmount(),"diffuserOxygen",shoreMayRun))throw new IllegalStateException("Oxygen diffuser contents changed: "+diffuser.oxygenTank.getFluidAmount());
        var tank=(FluidTankBlockEntity)Objects.requireNonNull(level.getBlockEntity(pos("dieselTank",origin)));
        if(!amount(tank.getTankInventory().getFluidAmount(),"diesel",false)||!BuiltInRegistries.FLUID.getKey(tank.getTankInventory().getFluid().getFluid()).toString().equals("createdieselgenerators:diesel"))throw new IllegalStateException("Stored diesel changed");
        if(m.storage().structure(m).getAsJsonArray("blocks").size()!=expected.get("blocks").getAsInt())throw new IllegalStateException("Hull block count changed");
        if(body.getPlot().getContraptions().size()!=expected.get("contraptions").getAsInt())throw new IllegalStateException("Bearing contraption count changed");
        if(!m.starter().moored(body))throw new IllegalStateException("Recovered vessel is not moored");
        try(var paths=Files.walk(server.getWorldPath(LevelResource.ROOT).resolve("dbv/fleet"))){
            if(paths.anyMatch(p->p.toString().endsWith(".nbt")&&!p.toString().contains("legacy-nbt")))throw new IllegalStateException("Active fleet data still writes NBT files");
        }
    }
    private void prepareSplitRollback(FleetModule m,ServerSubLevel body) throws Exception {
        var identity=m.registry().flagship().orElseThrow();
        if(!identity.vesselId().equals(vesselId))throw new IllegalStateException("Split fixture permanent identity changed");
        oldBodyId=identity.bodyId();splitGeneration=identity.generation();
        independentBodies=loadedBodyIds(m);independentBodies.remove(oldBodyId);
        previousTransactions=transactionFiles(m);
        var box=body.getPlot().getBoundingBox();BlockPos floating=null;
        // Keep the extra container inside the existing plot bounds, but genuinely detached on all six sides.
        search:for(int y=box.maxY()-1;y>box.minY();y--)for(int x=box.minX()+1;x<box.maxX();x++)for(int z=box.minZ()+1;z<box.maxZ();z++){
            var p=new BlockPos(x,y,z);if(!server.overworld().getBlockState(p).isAir())continue;
            boolean clear=true;for(var direction:Direction.values())if(!server.overworld().getBlockState(p.relative(direction)).isAir()){clear=false;break;}
            if(clear){floating=p;break search;}
        }
        if(floating==null)throw new IllegalStateException("No six-sided air cell inside source plot for split rollback regression");
        server.overworld().setBlock(floating,Blocks.BARREL.defaultBlockState(),3);
        var inventory=(Container)Objects.requireNonNull(server.overworld().getBlockEntity(floating));
        var diamonds=new ItemStack(Items.DIAMOND,17);diamonds.set(DataComponents.CUSTOM_NAME,Component.literal("分裂回滚·不可残留样本"));
        var marker=new CompoundTag();marker.putString("purpose","whole-vessel-split-rollback");
        diamonds.set(DataComponents.CUSTOM_DATA,CustomData.of(marker));inventory.setItem(0,diamonds);inventory.setChanged();
        // No intervening tick: capture the disconnected block before native topology moves it into a child carrier.
        m.storage().capture(m);var store=m.storage().store(m);store.checkpoint(store.read(),false);
        splitCheckpoint=VesselDataStore.fingerprint(Objects.requireNonNull(store.recoveryCheckpoint()));
        splitEvidence=new JsonObject();splitEvidence.add("floatingBarrel",position(floating.subtract(origin(body))));
        splitEvidence.addProperty("originalBodyId",oldBodyId.toString());splitEvidence.addProperty("generation",splitGeneration);
        splitEvidence.addProperty("checkpointSha256",splitCheckpoint);splitEvidence.add("independentBodies",uuidArray(independentBodies));
        Files.writeString(reports.resolve("splitrollback-expected.json"),JSON.toJson(splitEvidence));
        destroyAndRestore(m,body);
        var transaction=new RecoveryJournal(store.directory()).read();
        if(transaction==null||!m.recovery().active())throw new IllegalStateException("Split fixture did not create a pending recovery transaction; "+m.recovery().status(m));
        initialCandidates=uuidSet(transaction.getAsJsonArray("candidates"));
        splitEvidence.add("initialCandidates",uuidArray(initialCandidates));
        Files.writeString(reports.resolve("splitrollback-expected.json"),JSON.toJson(splitEvidence));
    }
    private void verifySplitRollback(FleetModule m) throws Exception {
        var identity=m.registry().flagship().orElseThrow();
        if(m.recovery().active())throw new IllegalStateException("Split rollback transaction is still active");
        if(!identity.vesselId().equals(vesselId)||!identity.bodyId().equals(oldBodyId)||identity.generation()!=splitGeneration)
            throw new IllegalStateException("Rejected split import changed registered identity or generation");
        var newTransactions=transactionFiles(m);newTransactions.removeAll(previousTransactions);
        if(newTransactions.size()!=1)throw new IllegalStateException("Expected one archived split transaction; found "+newTransactions);
        var receipt=newTransactions.iterator().next();
        if(!receipt.getFileName().toString().endsWith("-rolled-back.json"))throw new IllegalStateException("Split import was not rolled back: "+receipt);
        var envelope=JsonParser.parseString(Files.readString(receipt)).getAsJsonObject();var transaction=envelope.getAsJsonObject("payload");
        if(!VesselDataStore.fingerprint(transaction).equals(envelope.get("sha256").getAsString())||!transaction.get("originalBody").getAsString().equals(oldBodyId.toString()))
            throw new IllegalStateException("Split transaction receipt does not match the failed operation");
        var candidates=uuidSet(transaction.getAsJsonArray("candidates"));var descendants=new HashSet<>(candidates);descendants.removeAll(initialCandidates);
        if(descendants.isEmpty())throw new IllegalStateException("Fixture did not exercise native split tracking; candidates="+candidates);
        for(var id:candidates)for(var level:server.getAllLevels())if(NativeVesselArchive.findPersistedBody(level,id)!=null)
            throw new IllegalStateException("Rejected candidate or split descendant remains in native storage: "+id);
        if(!loadedBodyIds(m).equals(independentBodies))throw new IllegalStateException("Split rollback left unrelated or duplicate physical bodies: "+loadedBodyIds(m));
        for(var level:server.getAllLevels())for(var entity:level.getAllEntities())if(entity instanceof net.minecraft.world.entity.item.ItemEntity dropped){
            var custom=dropped.getItem().get(DataComponents.CUSTOM_DATA);
            if(custom!=null&&custom.copyTag().getString("purpose").equals("whole-vessel-split-rollback"))throw new IllegalStateException("Split rollback duplicated the fixture cargo as a dropped item");
        }
        var store=m.storage().store(m);var checkpoint=store.recoveryCheckpoint();
        if(checkpoint==null||!splitCheckpoint.equals(VesselDataStore.fingerprint(checkpoint)))throw new IllegalStateException("Rejected split import changed the recovery checkpoint");
        if(new RecoveryJournal(store.directory()).read()!=null)throw new IllegalStateException("Split rollback left an active disk transaction");
        splitEvidence.add("allCandidatesRemoved",uuidArray(candidates));splitEvidence.add("splitDescendantsRemoved",uuidArray(descendants));
        splitEvidence.addProperty("checkpointUnchanged",true);splitEvidence.addProperty("identityUnchanged",true);
        splitEvidence.addProperty("cargoCopiesRemaining",0);splitEvidence.addProperty("receipt",receipt.toString());
    }
    private Set<UUID> loadedBodyIds(FleetModule m){
        var ids=new HashSet<UUID>();for(var level:server.getAllLevels())for(var body:m.adapter().loaded(level))if(!body.isRemoved())ids.add(body.getUniqueId());return ids;
    }
    private Set<Path> transactionFiles(FleetModule m) throws Exception {
        var directory=m.storage().store(m).directory().resolve("recovery-history");
        if(!Files.isDirectory(directory))return new HashSet<>();
        try(var paths=Files.list(directory)){return new HashSet<>(paths.filter(p->p.getFileName().toString().endsWith(".json")).toList());}
    }
    private static Set<UUID> uuidSet(JsonArray values){var ids=new HashSet<UUID>();for(var value:values)ids.add(UUID.fromString(value.getAsString()));return ids;}
    private static JsonArray uuidArray(Set<UUID> ids){var values=new JsonArray();ids.stream().map(UUID::toString).sorted().forEach(values::add);return values;}
    private boolean amount(int actual,String key,boolean mayIncrease){int saved=expected.get(key).getAsInt();return mayIncrease?actual>=saved:actual==saved;}
    private void save() throws Exception {if(!server.saveEverything(false,true,true))throw new IllegalStateException("Test world save failed");}
    private void succeed(FleetModule m,boolean interrupted) throws Exception {
        var report=new JsonObject();report.addProperty("passed",true);report.addProperty("phase",phase);report.addProperty("ticks",ticks);
        report.addProperty("interruptedBeforeCommit",interrupted);report.addProperty("vesselId",vesselId.toString());report.addProperty("bodyId",m.registry().flagship().orElseThrow().bodyId().toString());
        if(splitEvidence!=null)report.add("splitRollback",splitEvidence.deepCopy());
        report.addProperty("status",m.recovery().status(m));report.addProperty("world",server.getWorldPath(LevelResource.ROOT).toString());
        Files.writeString(reports.resolve(phase+".json"),JSON.toJson(report));save();finished=true;server.halt(false);
    }
    private void fail(Throwable failure){
        finished=true;failure.printStackTrace();
        try{
            if(reports!=null){var report=new JsonObject();report.addProperty("passed",false);report.addProperty("phase",phase);report.addProperty("state",state);report.addProperty("ticks",ticks);
                if(splitEvidence!=null)report.add("splitRollback",splitEvidence.deepCopy());
                if(server!=null)try{var m=FleetModule.get(server);report.addProperty("recoveryStatus",m.recovery().status(m));report.addProperty("recoveryActive",m.recovery().active());}catch(Exception diagnostic){report.addProperty("recoveryDiagnosticFailure",diagnostic.toString());}
                var stack=new java.io.StringWriter();failure.printStackTrace(new java.io.PrintWriter(stack));report.addProperty("failure",stack.toString());Files.writeString(reports.resolve((phase==null?"startup":phase)+".json"),JSON.toJson(report));}
        }catch(Exception ignored){}
        if(server!=null)server.halt(false);
    }
}
