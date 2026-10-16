package io.github.low2uu.dbv.fleet.dock;

import io.github.low2uu.dbv.world.WorldHeights;

import com.maxenonyme.createsubmarine.CreateSubmarine;
import com.maxenonyme.createsubmarine.submarine.block.entity.ElectrolyzerBlockEntity;
import com.maxenonyme.createsubmarine.submarine.block.entity.OxygeneDiffuserBlockEntity;
import dev.ryanhcode.sable.api.SubLevelAssemblyHelper;
import dev.ryanhcode.sable.api.physics.constraint.FixedConstraintHandle;
import dev.ryanhcode.sable.api.physics.constraint.FixedConstraintConfiguration;
import dev.ryanhcode.sable.companion.math.BoundingBox3i;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import dev.ryanhcode.sable.sublevel.system.SubLevelPhysicsSystem;
import io.github.low2uu.dbv.fleet.FleetModule;
import io.github.low2uu.dbv.world.ConstructionMode;
import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.storage.LevelResource;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction;
import org.joml.*;

/** Construction-stage starter vessel. Shore service is explicit and is never an ocean survival mechanic. */
public final class StarterSubmarine {
    private static final dev.ryanhcode.sable.api.sublevel.ticket.SubLevelLoadingTicketType<net.minecraft.util.Unit> INITIAL_VALIDATION =
            dev.ryanhcode.sable.api.sublevel.ticket.SubLevelLoadingTicketType.create(ResourceLocation.parse("dbv_world:starter_validation"),
                    com.mojang.serialization.Codec.unit(net.minecraft.util.Unit.INSTANCE));
    public static void initializeTickets() { /* Register the ticket codec before world ticket deserialization. */ }
    private final MinecraftServer server;
    private final Path directory;
    private FixedConstraintHandle mooring;
    private java.util.UUID mooredBody;
    private ServerSubLevel mooredInstance;
    private boolean failed;
    private CompoundTag vesselData;
    private Boolean departed;
    public boolean departed(FleetModule module) throws IOException {
        if(module.registry().flagship().isEmpty())return false;
        if(departed==null){
            var navigation=module.storage().store(module).read().getAsJsonObject("modules").getAsJsonObject("navigation");
            departed=navigation!=null&&navigation.get("departed").getAsBoolean();
        }
        return departed;
    }
    public String departureProblem(net.minecraft.server.level.ServerPlayer player,FleetModule module){
        try{
            if(departed(module))return "已出航，船坞固定与岸供已断开";
            if(module.refit().active())return "请先完成或取消改装";
            return module.refit().accessProblem(player,module);
        }catch(Exception error){return "无法读取出航档案，请检查独立数据目录";}
    }
    public String depart(net.minecraft.server.level.ServerPlayer player,FleetModule module) throws IOException {
        String problem=departureProblem(player,module);if(problem!=null)throw new IllegalStateException(problem);
        var state=new com.google.gson.JsonObject();state.addProperty("schemaVersion",1);state.addProperty("departed",true);
        module.storage().store(module).putModule("navigation",state);
        departed=true;releaseMooring();
        return "已出航：船坞固定与岸供已断开，潜艇进入自由物理状态。请使用艇上的驾驶设备控制。";
    }
    public String mooringProblem(net.minecraft.server.level.ServerPlayer player,FleetModule module){
        try{
            if(!ConstructionMode.ENABLED||!player.isCreative()||!player.hasPermissions(2))return "首版停泊仅限建设模式创造管理员";
            if(failed)return "船坞服务异常，请保存退出后重新进入世界，再检查日志";
            if(!departed(module))return "主艇已经停泊";
            if(module.refit().active())return "请先结束改装";
            var body=module.refit().currentBody(module);
            if(body==null||body.isRemoved())return "主艇未加载";
            if(player.serverLevel()!=body.getLevel()||!inDock(body))return "请将整艘潜艇驶入初始船坞范围后停泊";
            var local=body.logicalPose().transformPositionInverse(player.position());var box=body.getPlot().getBoundingBox();
            if(local.x<box.minX()-12||local.x>box.maxX()+12||local.y<box.minY()-12||local.y>box.maxY()+12||local.z<box.minZ()-12||local.z>box.maxZ()+12)return "请靠近主艇操作停泊";
            for(var holder:body.getPlot().getLoadedChunks()){
                if(holder.getChunk()==null)return "船体区块未完整加载";
                for(var be:holder.getChunk().getBlockEntities().values())
                    if(be instanceof com.simibubi.create.content.kinetics.base.KineticBlockEntity prop
                            &&BuiltInRegistries.BLOCK.getKey(be.getBlockState().getBlock()).equals(ResourceLocation.parse("create_submarine:submarine_propeller"))
                            &&java.lang.Math.abs(prop.getSpeed())>.01)return "请先断开两侧推进离合器，停止螺旋桨";
            }
            var helper=com.maxenonyme.createsubmarine.submarine.util.SablePhysicsHelper.getHandle(body);
            var velocity=com.maxenonyme.createsubmarine.submarine.util.SablePhysicsHelper.getVelocity(helper);
            var angular=com.maxenonyme.createsubmarine.submarine.util.SablePhysicsHelper.getAngularVelocity(helper);
            if(velocity==null||angular==null||!Double.isFinite(velocity.length())||!Double.isFinite(angular.length()))return "暂时无法确认船体速度";
            if(velocity.length()>1||angular.length()>.15)return "船体仍在移动，请减速并等待稳定后停泊";
            return null;
        }catch(Exception error){return "无法读取停泊状态，请检查独立数据档案";}
    }
    public String moor(net.minecraft.server.level.ServerPlayer player,FleetModule module) throws IOException {
        String problem=mooringProblem(player,module);if(problem!=null)throw new IllegalStateException(problem);
        var body=module.refit().currentBody(module);
        module.storage().backup(module);
        // Capture the current world anchor and attitude before enabling persistent mooring.
        adoptRefitBody(body);
        var state=new com.google.gson.JsonObject();state.addProperty("schemaVersion",1);state.addProperty("departed",false);
        module.storage().store(module).putModule("navigation",state);departed=false;
        try{
            tick(module);
            if(!moored(body))throw new IOException("停泊约束未建立，请保留备份并检查船坞状态");
        }catch(IOException|RuntimeException error){
            releaseMooring();departed=true;state.addProperty("departed",true);
            try{module.storage().store(module).putModule("navigation",state);}catch(IOException rollback){error.addSuppressed(rollback);}
            throw error;
        }
        return "已停泊：保持当前船坞位置与朝向，船体固定、岸供恢复；现在可以改装，或再次出航。";
    }
    public StarterSubmarine(MinecraftServer server) {
        this.server = server;
        directory = server.getWorldPath(LevelResource.ROOT).resolve("dbv/fleet/dock");
    }
    public static void requestNewWorld(MinecraftServer server) throws IOException {
        var directory = server.getWorldPath(LevelResource.ROOT).resolve("dbv/fleet/dock");
        DockFiles.create(directory.resolve("starter-request.nbt"), new CompoundTag());
    }
    public void tick(FleetModule module) throws IOException {
        if (failed || !ConstructionMode.ENABLED) return;
        try {
            Path vessel = directory.resolve("starter.nbt");
            if (!Files.exists(vessel)) {
                if (!Files.exists(directory.resolve("starter-request.nbt"))) return;
                if (Files.exists(directory.resolve("starter-building.nbt"))) throw new IOException("初始艇生成曾中断，不重复生成，请保留船坞档案");
                create(module);
            }
            if (vesselData == null) vesselData = DockFiles.read(vessel);
            var tag = vesselData;
            var body = module.adapter().find(server.overworld(), tag.getUUID("body"));
            if (body == null) return; // Unloaded is not destroyed; never create a replacement.
            if(departed(module)){
                releaseMooring();
                dev.ryanhcode.sable.api.sublevel.SubLevelContainer.getContainer(body.getLevel())
                        .removeForceLoadTicket(body,INITIAL_VALIDATION,net.minecraft.util.Unit.INSTANCE);
                return;
            }
            if (!inDock(body)) return;
            if (mooring == null || !mooring.isValid() || body != mooredInstance) {
                if (mooring != null && mooring.isValid()) mooring.remove();
                var p = new Vector3d(tag.getInt("x") + .5, tag.getInt("y") + .5, tag.getInt("z") + .5);
                var w = new Vector3d(tag.getDouble("worldX"), tag.getDouble("worldY"), tag.getDouble("worldZ"));
                mooring = SubLevelPhysicsSystem.require(body.getLevel()).getPipeline().addConstraint(body, null,
                        new FixedConstraintConfiguration(p, w, tag.contains("qw")
                                ?new Quaterniond(tag.getDouble("qx"),tag.getDouble("qy"),tag.getDouble("qz"),tag.getDouble("qw")):new Quaterniond()));
                mooredBody = body.getUniqueId();
                mooredInstance = body;
            }
            // A dock utility, not fake production: the generator still consumes FE/water and produces oxygen.
            for (var holder : body.getPlot().getLoadedChunks()) {
                if (holder.getChunk() == null) continue;
                for (var be : holder.getChunk().getBlockEntities().values()) {
                    if (be instanceof ElectrolyzerBlockEntity gen) {
                        gen.energyStorage.receiveEnergy(1000, false);
                        gen.waterTank.fill(new FluidStack(Fluids.WATER, 100), FluidAction.EXECUTE);
                    } else if (be instanceof OxygeneDiffuserBlockEntity diff) {
                        diff.oxygenTank.fill(new FluidStack(CreateSubmarine.OXYGEN.get(), 100), FluidAction.EXECUTE);
                    }
                }
            }
            if (server.getTickCount() % 20 == 0 && module.registry().flagship().isEmpty() && module.inspection().inspect(body).qualification().eligible()) {
                module.bind(server, body, "酒狐号·初始艇");
                server.getPlayerList().broadcastSystemMessage(net.minecraft.network.chat.Component.literal(
                        "酒狐号已通过三项检查并登记。船坞岸供已连接（建设模式）；潜行右键船载电脑打开改装菜单。"), false);
            }
            if (module.registry().flagship().isPresent()&&!module.refit().active()) {
                dev.ryanhcode.sable.api.sublevel.SubLevelContainer.getContainer(body.getLevel())
                        .removeForceLoadTicket(body, INITIAL_VALIDATION, net.minecraft.util.Unit.INSTANCE);
            }
        } catch (IOException | RuntimeException error) { failed = true; throw error; }
    }
    public static boolean inDock(ServerSubLevel body) {
        var box = body.boundingBox();
        return body.getLevel().dimension() == net.minecraft.world.level.Level.OVERWORLD
                && box.minX() >= 0 && box.maxX() <= 128 && box.minZ() >= 112 && box.maxZ() <= 240
                && box.minY() >= WorldHeights.fromLegacy(13) && box.maxY() <= WorldHeights.fromLegacy(100);
    }
    public boolean moored(ServerSubLevel body) { return body.getUniqueId().equals(mooredBody) && mooring != null && mooring.isValid(); }
    public void releaseMooring(){
        if(mooring!=null&&mooring.isValid())mooring.remove();
        mooring=null;mooredBody=null;mooredInstance=null;
    }
    public void adoptRefitBody(ServerSubLevel body) throws IOException {
        releaseMooring();
        var b=body.getPlot().getBoundingBox();var p=new Vector3d(b.minX()+.5,b.minY()+.5,b.minZ()+.5);
        var w=body.logicalPose().transformPosition(p,new Vector3d());
        var tag=new CompoundTag();tag.putUUID("body",body.getUniqueId());
        tag.putInt("x",b.minX());tag.putInt("y",b.minY());tag.putInt("z",b.minZ());
        tag.putDouble("worldX",w.x);tag.putDouble("worldY",w.y);tag.putDouble("worldZ",w.z);
        var orientation=body.logicalPose().orientation().conjugate(new Quaterniond());
        tag.putDouble("qx",orientation.x);tag.putDouble("qy",orientation.y);tag.putDouble("qz",orientation.z);tag.putDouble("qw",orientation.w);
        DockFiles.replace(directory.resolve("starter.nbt"),tag);vesselData=tag;
        dev.ryanhcode.sable.api.sublevel.SubLevelContainer.getContainer(body.getLevel()).addForceLoadTicket(body,INITIAL_VALIDATION,net.minecraft.util.Unit.INSTANCE);
    }
    private void create(FleetModule module) throws IOException {
        if (module.registry().flagship().isPresent() || !module.adapter().loaded(server.overworld()).isEmpty())
            throw new IOException("世界已有船体或主艇，拒绝自动生成");
        var level = server.overworld();
        var plan = AnglerSubmarine.plan();
        for (var p : BlockPos.betweenClosed(AnglerSubmarine.MIN, AnglerSubmarine.MAX)) {
            if (!level.getBlockState(p).isAir()) throw new IOException("初始艇泊位被占用：" + p.toShortString());
        }
        DockFiles.create(directory.resolve("starter-building.nbt"), new CompoundTag());
        plan.forEach((p,state)->level.setBlock(p,state,2));
        plan.forEach((p,state)->level.updateNeighborsAt(p,state.getBlock()));
        AnglerSubmarine.provision(level);
        var positions = plan.entrySet().stream().filter(e->!e.getValue().isAir()).map(java.util.Map.Entry::getKey).toList();
        var body = SubLevelAssemblyHelper.assembleBlocks(level,AnglerSubmarine.MIN,positions,new BoundingBox3i(60,WorldHeights.fromLegacy(68),150,74,WorldHeights.fromLegacy(78),173));
        if (body==null) throw new IOException("Sable装配失败，未生成第二艘船");
        dev.ryanhcode.sable.api.sublevel.SubLevelContainer.getContainer(level)
                .addForceLoadTicket(body, INITIAL_VALIDATION, net.minecraft.util.Unit.INSTANCE);
        var box = body.getPlot().getBoundingBox();
        var p = new Vector3d(box.minX()+.5,box.minY()+.5,box.minZ()+.5);
        var w = body.logicalPose().transformPosition(p,new Vector3d());
        var tag = new CompoundTag(); tag.putUUID("body",body.getUniqueId());
        tag.putInt("x",box.minX());tag.putInt("y",box.minY());tag.putInt("z",box.minZ());
        tag.putDouble("worldX",w.x);tag.putDouble("worldY",w.y);tag.putDouble("worldZ",w.z);
        if (!server.saveEverything(false,true,true)) throw new IOException("初始艇存盘失败");
        DockFiles.create(directory.resolve("starter.nbt"),tag);
    }
    private static void place(net.minecraft.server.level.ServerLevel level, ArrayList<BlockPos> positions, BlockPos p, String id) {
        level.setBlock(p,BuiltInRegistries.BLOCK.get(ResourceLocation.parse(id)).defaultBlockState(),3); positions.add(p);
    }
}
