package io.github.low2uu.dbv.fleet.dock;

import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import io.github.low2uu.dbv.fleet.FleetModule;
import io.github.low2uu.dbv.world.ConstructionMode;
import java.io.IOException;
import java.nio.file.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.storage.LevelResource;

/** One durable body-only transaction. Interrupted sessions roll back before editing can resume. */
public final class RefitService {
    private final MinecraftServer server;
    private final Path active;
    private CompoundTag session;
    private boolean recoveryRequired;
    private boolean blocked;
    private long checkingUntil;
    public RefitService(MinecraftServer server) throws IOException {
        this.server=server;
        active=server.getWorldPath(LevelResource.ROOT).resolve("dbv/fleet/dock/active-refit.nbt");
        if (Files.exists(active.resolveSibling("active-refit.nbt.pending"))) throw new IOException("改装备份写入中断，禁止继续编辑");
        if (Files.exists(active)) { session=DockFiles.read(active); validate(session); recoveryRequired=true; }
    }
    public boolean active() { return session!=null; }
    public String status() { return blocked ? "恢复异常，已锁定；请保留船坞备份" : recoveryRequired ? "上次改装中断，等待原船加载后自动还原" : checkingUntil>0 ? "正在等待舱室扫描，编辑暂时锁定" : active() ? "改装中；范围限原船包围盒，提交失败会还原" : "未改装；建设模式/创造模式可开始"; }
    private ServerSubLevel body(FleetModule module) {
        var identity=module.registry().flagship().orElseThrow(()->new IllegalStateException("主艇尚未登记，请等待供氧和密闭扫描"));
        var body=module.adapter().find(server.overworld(),identity.bodyId());
        if(body==null) throw new IllegalStateException("原主艇未加载，不能用新船替代");
        if(session!=null && (!session.getUUID("body").equals(identity.bodyId()) || !session.getUUID("vessel").equals(identity.vesselId())))
            throw new IllegalStateException("备份与主艇身份不符");
        return body;
    }
    private ServerSubLevel authorize(ServerPlayer player,FleetModule module) {
        if(!ConstructionMode.ENABLED || !player.isCreative() || !player.hasPermissions(2)) throw new IllegalStateException("首版仅限建设模式下的创造模式管理员");
        if(blocked || recoveryRequired) throw new IllegalStateException(status());
        var body=body(module);
        if(module.sample(server).state()!=io.github.low2uu.dbv.fleet.telemetry.VesselTelemetry.State.ACTIVE)
            throw new IllegalStateException("主艇身份或载体状态异常，请先核对档案");
        if(player.serverLevel()!=server.overworld() || !StarterSubmarine.inDock(body) || !module.starter().moored(body))
            throw new IllegalStateException("请在初始船坞内对已系泊主艇操作");
        var local=body.logicalPose().transformPositionInverse(player.position());
        var b=body.getPlot().getBoundingBox();
        if(local.x<b.minX()-12 || local.x>b.maxX()+12 || local.y<b.minY()-12 || local.y>b.maxY()+12 || local.z<b.minZ()-12 || local.z>b.maxZ()+12)
            throw new IllegalStateException("请靠近主艇后操作");
        if(session!=null && !session.getUUID("owner").equals(player.getUUID())) throw new IllegalStateException("另一位开发者正在改装，不能接管");
        return body;
    }
    public String execute(String action,ServerPlayer player,FleetModule module) throws IOException {
        var body=authorize(player,module);
        if(action.equals("check")) {
            var report=module.inspection().inspect(body);
            io.github.low2uu.dbv.fleet.command.ControllerInteraction.display(report,player::sendSystemMessage,1);
            return status();
        }
        if(action.equals("begin")) {
            if(active()) throw new IllegalStateException("已有改装备份，请提交或取消");
            module.inspection().inspect(body).requireEligible();
            if(!body.getPlot().getContraptions().isEmpty()) throw new IllegalStateException("首版暂不备份运动机械结构，请先移除");
            var b=body.getPlot().getBoundingBox();
            long volume=(long)(b.maxX()-b.minX()+1)*(b.maxY()-b.minY()+1)*(b.maxZ()-b.minZ()+1);
            if(volume>4096 || volume<=0) throw new IllegalStateException("首版仅支持4096格以内的单船体");
            var data=new CompoundTag(); data.putInt("schema",1);data.putUUID("body",body.getUniqueId());
            data.putUUID("vessel",module.registry().flagship().orElseThrow().vesselId());data.putUUID("owner",player.getUUID());
            data.putIntArray("bounds",new int[]{b.minX(),b.minY(),b.minZ(),b.maxX(),b.maxY(),b.maxZ()});
            var entries=new ListTag();
            for(var p:BlockPos.betweenClosed(b.minX(),b.minY(),b.minZ(),b.maxX(),b.maxY(),b.maxZ())) {
                var state=body.getLevel().getBlockState(p);
                if(!supported(state)) throw new IllegalStateException("首版快照不支持该设备："+state.getBlock());
                var entry=new CompoundTag();entry.putLong("pos",p.asLong());entry.put("state",NbtUtils.writeBlockState(state));
                var be=body.getLevel().getBlockEntity(p);
                if(be!=null) entry.put("be",be.saveWithFullMetadata(body.getLevel().registryAccess()));
                entries.add(entry);
            }
            data.put("blocks",entries);
            DockFiles.create(active,data);session=data;
            return "备份已落盘，开始改装。可替换船壳、玻璃和三项核心设备；暂不支持扩容、容器、实体或子船。";
        }
        if(!active()) throw new IllegalStateException("请先开始改装");
        if(action.equals("commit")) {
            var report=module.inspection().inspect(body);
            if(report.qualification().pending()) {
                if(checkingUntil==0) checkingUntil=server.overworld().getGameTime()+200;
                return "验收中，等待结构扫描（最多10秒）；期间锁定编辑，完成后自动通知。";
            }
            if(!report.qualification().eligible()) {
                String reasons=String.join("；",report.qualification().reasons());restore(body);finish("rejected");
                return "验收失败，已还原改装前状态："+reasons;
            }
            finish("committed");return "验收通过，改装已保存；主艇身份保持不变，旧备份保留在history。";
        }
        if(action.equals("cancel")) { restore(body);finish("cancelled");return "已取消改装并还原船体与支持设备的数据。"; }
        throw new IllegalArgumentException("未知改装操作");
    }
    public void recover(FleetModule module) throws IOException {
        if(!recoveryRequired || blocked) return;
        var identity=module.registry().flagship();
        if(identity.isEmpty() || module.adapter().find(server.overworld(),identity.get().bodyId())==null) return;
        try { restore(body(module));finish("interrupted");recoveryRequired=false; }
        catch(IOException | RuntimeException error) { blocked=true;throw error; }
    }
    public void process(FleetModule module) throws IOException {
        if(checkingUntil==0 || blocked || recoveryRequired || !active() || server.getTickCount()%20!=0) return;
        try {
            var body=body(module);var report=module.inspection().inspect(body);
            if(report.qualification().pending() && server.overworld().getGameTime()<checkingUntil) return;
            var owner=server.getPlayerList().getPlayer(session.getUUID("owner"));
            boolean eligible=report.qualification().eligible();
            if(!eligible) restore(body);
            finish(eligible?"committed":"rejected");
            if(owner!=null) owner.sendSystemMessage(net.minecraft.network.chat.Component.literal(eligible?"验收完成，改装已保存。":"验收未通过，已自动还原："+String.join("；",report.qualification().reasons())));
        } catch(IOException | RuntimeException error) { blocked=true;throw error; }
    }
    private void restore(ServerSubLevel body) throws IOException {
        try { restoreBody(body); }
        catch(IOException | RuntimeException error) { blocked=true;throw error; }
    }
    private void restoreBody(ServerSubLevel body) throws IOException {
        validate(session);
        // Validate every state before touching the live body. Do not use a whole-plot load over live chunks.
        var entries=session.getList("blocks",Tag.TAG_COMPOUND);
        var registry=body.getLevel().holderLookup(Registries.BLOCK);
        var states=new java.util.ArrayList<net.minecraft.world.level.block.state.BlockState>();
        for(int i=0;i<entries.size();i++) {
            var state=NbtUtils.readBlockState(registry,entries.getCompound(i).getCompound("state"));
            if(!supported(state)) throw new IOException("快照含未支持设备，停止恢复");
            states.add(state);
        }
        var level=body.getLevel();
        for(int i=0;i<entries.size();i++) {
            var e=entries.getCompound(i);var p=BlockPos.of(e.getLong("pos"));
            level.removeBlockEntity(p);
            level.setBlock(p,states.get(i),3);
            if(e.contains("be")) {
                var be=BlockEntity.loadStatic(p,states.get(i),e.getCompound("be"),level.registryAccess());
                if(be==null) throw new IOException("方块实体恢复失败："+p);
                level.setBlockEntity(be);be.setChanged();
                level.sendBlockUpdated(p,states.get(i),states.get(i),3);
            }
        }
    }
    private void finish(String outcome) throws IOException {
        try { closeSnapshot(outcome); }
        catch(IOException | RuntimeException error) { blocked=true;throw error; }
    }
    private void closeSnapshot(String outcome) throws IOException {
        if(!server.saveEverything(false,true,true)) throw new IOException("世界存盘失败，保留活动备份");
        Path history=active.getParent().resolve("history");Files.createDirectories(history);
        Files.move(active,history.resolve(java.util.UUID.randomUUID()+"-"+outcome+".nbt"),StandardCopyOption.ATOMIC_MOVE);
        session=null;
        checkingUntil=0;
    }
    private static void validate(CompoundTag data) throws IOException {
        if(data.getInt("schema")!=1 || !data.hasUUID("body") || !data.hasUUID("vessel") || !data.hasUUID("owner")) throw new IOException("改装快照头损坏");
        var b=data.getIntArray("bounds");
        if(b.length!=6) throw new IOException("改装边界损坏");
        long volume=(long)(b[3]-b[0]+1)*(b[4]-b[1]+1)*(b[5]-b[2]+1);
        var entries=data.getList("blocks",Tag.TAG_COMPOUND);
        if(b[3]<b[0] || b[4]<b[1] || b[5]<b[2] || volume<=0 || volume>4096 || entries.size()!=volume) throw new IOException("改装快照范围不合法");
        var seen=new java.util.HashSet<Long>();
        for(int i=0;i<entries.size();i++) {
            var e=entries.getCompound(i);var p=BlockPos.of(e.getLong("pos"));
            if(!e.contains("state",Tag.TAG_COMPOUND) || !seen.add(p.asLong()) || p.getX()<b[0] || p.getX()>b[3] || p.getY()<b[1] || p.getY()>b[4] || p.getZ()<b[2] || p.getZ()>b[5]) throw new IOException("改装快照条目损坏");
        }
    }
    public boolean mayEdit(ServerPlayer player,BlockPos p,net.minecraft.world.level.block.state.BlockState state) {
        if(!active() || blocked || recoveryRequired || checkingUntil>0 || !ConstructionMode.ENABLED || !player.isCreative() || !player.hasPermissions(2) || !session.getUUID("owner").equals(player.getUUID()) || !supported(state)) return false;
        var b=session.getIntArray("bounds");
        // Retain the mooring anchor block and keep the first implementation inside the original envelope.
        return p.getX()>=b[0] && p.getX()<=b[3] && p.getY()>=b[1] && p.getY()<=b[4] && p.getZ()>=b[2] && p.getZ()<=b[5]
                && !(p.getX()==b[0] && p.getY()==b[1] && p.getZ()==b[2]);
    }
    public static boolean supported(net.minecraft.world.level.block.state.BlockState state) {
        var id=net.minecraft.core.registries.BuiltInRegistries.BLOCK.getKey(state.getBlock()).toString();
        return java.util.Set.of("minecraft:air", "minecraft:iron_block", "minecraft:glass", "minecraft:deepslate_tiles",
                "minecraft:oak_trapdoor", "minecraft:redstone_block", "minecraft:sea_lantern", "minecraft:copper_block",
                "minecraft:cut_copper", "minecraft:stone", "minecraft:stone_bricks", "minecraft:polished_deepslate",
                "minecraft:white_concrete", "minecraft:gray_concrete", "minecraft:light_gray_concrete").contains(id)
                || id.equals("create_submarine:command_sub") || id.equals("create_submarine:electrolyzer") || id.equals("create_submarine:oxygene_diffuser");
    }
}
