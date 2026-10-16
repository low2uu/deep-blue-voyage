package io.github.low2uu.dbv.fleet.dock;

import io.github.low2uu.dbv.world.WorldHeights;

import dev.ryanhcode.sable.api.SubLevelAssemblyHelper;
import dev.ryanhcode.sable.companion.math.BoundingBox3i;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import dev.ryanhcode.sable.sublevel.plot.ServerLevelPlot;
import io.github.low2uu.dbv.fleet.FleetModule;
import io.github.low2uu.dbv.fleet.compat.VesselInspection;
import io.github.low2uu.dbv.fleet.domain.Qualification;
import io.github.low2uu.dbv.world.ConstructionMode;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.LevelResource;

/** Durable physical -> world blocks -> validated physical transaction. Unknown transitions fail closed. */
public final class RefitService {
    private static final BlockPos PAD=new BlockPos(60,WorldHeights.fromLegacy(68),150);
    private final MinecraftServer server;
    private final Path file;
    private final LegacyRefitService legacy;
    private CompoundTag journal;
    private boolean recovering,blocked;
    private long deadline;
    public RefitService(MinecraftServer server) throws IOException {
        this.server=server;legacy=new LegacyRefitService(server);
        file=server.getWorldPath(LevelResource.ROOT).resolve("dbv/fleet/dock/block-refit.nbt");
        AtomicSnapshotFile.requireComplete(file);
        if(Files.exists(file)){
            journal=DockFiles.read(file);validate();recovering=true;
            if(legacy.active())throw new IOException("检测到两份活动改装记录，停止自动操作");
            if(!phase().equals("STATIC"))blocked=true; // A half-converted world must never be guessed at.
        }
    }
    public boolean active(){return journal!=null||legacy.active();}
    public boolean checking(){return journal!=null?phase().equals("VALIDATING"):legacy.checking();}
    public boolean blockMode(){return journal!=null&&!phase().equals("VALIDATING");}
    private String phase(){return journal==null?"":journal.getString("phase");}
    public String status(){
        if(journal==null)return legacy.active()?legacy.status():"物理船体；可开始拆解改装，三项条件只在完成时验收。";
        if(blocked)return "转换中断或恢复异常，已锁定；保留船坞档案，禁止重复装配。";
        if(recovering)return "正在恢复上次未完成的方块改装。";
        return checking()?"完成改装验收中：临时装配并等待密闭与供氧检测，暂不可编辑。":"方块改装中：船体已拆为世界方块；关闭窗口施工，完成后提交验收。";
    }
    public ServerSubLevel currentBody(FleetModule m){
        UUID id=journal!=null&&journal.hasUUID("candidate")?journal.getUUID("candidate"):m.registry().flagship().map(i->i.bodyId()).orElse(null);
        return id==null?null:m.adapter().find(server.overworld(),id);
    }
    private static net.minecraft.world.phys.AABB worldBox(ServerSubLevel body){var b=body.boundingBox();return new net.minecraft.world.phys.AABB(b.minX(),b.minY(),b.minZ(),b.maxX(),b.maxY(),b.maxZ());}
    private BoundingBox3i bounds(){var a=journal.getIntArray("worldBounds");return new BoundingBox3i(a[0],a[1],a[2],a[3],a[4],a[5]);}
    public boolean contains(BlockPos p){
        if(journal==null)return false;var b=bounds();
        return p.getX()>=Math.max(0,b.minX()-16)&&p.getX()<=Math.min(127,b.maxX()+16)
            &&p.getY()>=Math.max(WorldHeights.fromLegacy(65),b.minY()-16)&&p.getY()<=Math.min(WorldHeights.fromLegacy(100),b.maxY()+16)
            &&p.getZ()>=Math.max(112,b.minZ()-16)&&p.getZ()<=Math.min(239,b.maxZ()+16);
    }
    public boolean near(ServerPlayer player){return journal!=null&&player.serverLevel()==server.overworld()&&bounds().toAABB().inflate(12).contains(player.position());}
    public String accessProblem(ServerPlayer p,FleetModule m){
        if(journal==null&&legacy.active())return legacy.accessProblem(p,m);
        try{authorize(p,m);return null;}catch(Exception e){return e.getMessage()==null?"船坞状态无法确认":e.getMessage();}
    }
    private void authorize(ServerPlayer p,FleetModule m){
        if(!ConstructionMode.ENABLED||!p.isCreative()||!p.hasPermissions(2))throw new IllegalStateException("首版仅限建设模式下的创造模式管理员");
        if(blocked||recovering)throw new IllegalStateException(status());
        if(journal!=null){
            verifyIdentity(m);
            if(!journal.getUUID("owner").equals(p.getUUID()))throw new IllegalStateException("另一位开发者正在改装，不能接管");
            if(!near(p))throw new IllegalStateException("请靠近船坞改装区域");
        }else{
            var body=currentBody(m);
            if(body==null||body.isRemoved())throw new IllegalStateException("原主艇未加载，不能用新船替代");
            if(m.sample(server).state()!=io.github.low2uu.dbv.fleet.telemetry.VesselTelemetry.State.ACTIVE)throw new IllegalStateException("主艇身份异常，请核对档案");
            if(p.serverLevel()!=server.overworld()||!StarterSubmarine.inDock(body)||!m.starter().moored(body))throw new IllegalStateException("请在初始船坞内对已系泊主艇操作");
            if(!worldBox(body).inflate(12).contains(p.position()))throw new IllegalStateException("请靠近主艇后操作");
        }
    }
    public String execute(String action,ServerPlayer p,FleetModule m) throws IOException {
        if(legacy.active())return legacy.execute(action,p,m);
        authorize(p,m);
        if(action.equals("angler"))return installAngler(p,m);
        if(action.equals("check"))return status();
        if(action.equals("begin")){if(active())throw new IllegalStateException("已有改装，请完成或取消");return begin(p,m);}
        if(journal==null)throw new IllegalStateException("请先开始改装");
        if(checking())throw new IllegalStateException("正在验收，请等待结果");
        if(action.equals("cancel")){rollback(m,"cancelled");return "已取消：恢复改装前船体，主艇永久身份不变。";}
        if(action.equals("commit")){
            rememberConnected(); // Refuse an oversized/shore-connected scan before converting anything.
            try{
                // Verify that every block entity can be saved before handing it to Sable.
                capture(selectedBlocks(),PAD);
                assemble(m,false);
                deadline=server.overworld().getGameTime()+400;
                return "正在完成改装：已临时装配，等待三项检测（最多20秒）；通过后交付，失败还原。";
            }catch(IllegalArgumentException e){rollback(m,"rejected");return "验收失败，已恢复改装前船体："+e.getMessage();}
        }
        throw new IllegalArgumentException("未知改装操作");
    }

    /** Explicit template replacement, protected by the same original-vessel transaction backup. */
    private String installAngler(ServerPlayer p,FleetModule m) throws IOException {
        if(active())throw new IllegalStateException("请先完成或取消当前改装，再升级灯笼鱼型");
        var plan=AnglerSubmarine.plan(); // Resolve every dependency before dismantling the old vessel.
        var body=currentBody(m);if(body==null)throw new IllegalStateException("主艇未加载");
        var carried=new ArrayList<net.minecraft.world.item.ItemStack>();
        for(var holder:body.getPlot().getLoadedChunks()){
            if(holder.getChunk()==null)throw new IllegalStateException("船体区块未加载，不能迁移存储");
            for(var be:holder.getChunk().getBlockEntities().values()){
                if(be instanceof net.minecraft.world.Container container){
                    for(int i=0;i<container.getContainerSize();i++)if(!container.getItem(i).isEmpty())carried.add(container.getItem(i).copy());
                }else{
                    var items=server.overworld().getCapability(net.neoforged.neoforge.capabilities.Capabilities.ItemHandler.BLOCK,be.getBlockPos(),null);
                    if(items!=null)for(int i=0;i<items.getSlots();i++)if(!items.getStackInSlot(i).isEmpty())carried.add(items.getStackInSlot(i).copy());
                }
            }
        }
        if(carried.size()>54)throw new IllegalStateException("旧艇物品超过新艇54个存储槽，请先整理；未开始替换");
        var old=body.getPlot().getBoundingBox();
        var oldTarget=new BoundingBox3i(PAD.getX(),PAD.getY(),PAD.getZ(),PAD.getX()+old.maxX()-old.minX(),PAD.getY()+old.maxY()-old.minY(),PAD.getZ()+old.maxZ()-old.minZ());
        for(var pos:BlockPos.betweenClosed(AnglerSubmarine.MIN,AnglerSubmarine.MAX))
            if(!server.overworld().getBlockState(pos).isAir())throw new IllegalStateException("蓝图泊位被占用："+pos.toShortString());
        if(oldTarget.maxX()>AnglerSubmarine.MAX.getX()||oldTarget.maxY()>AnglerSubmarine.MAX.getY()||oldTarget.maxZ()>AnglerSubmarine.MAX.getZ())
            throw new IllegalStateException("旧艇大于灯笼鱼模板，请手工改装，避免删除扩建");
        begin(p,m);
        // Record the whole replacement envelope before changing blocks, including disconnected devices.
        journal.putLongArray("assembledBlocks",BlockPos.betweenClosedStream(AnglerSubmarine.MIN,AnglerSubmarine.MAX).mapToLong(BlockPos::asLong).toArray());
        DockFiles.replace(file,journal);
        for(var pos:positions(bounds()))server.overworld().setBlock(pos,Blocks.AIR.defaultBlockState(),2);
        plan.forEach((pos,state)->server.overworld().setBlock(pos,state,2));
        plan.forEach((pos,state)->server.overworld().updateNeighborsAt(pos,state.getBlock()));
        AnglerSubmarine.provision(server.overworld());
        int itemIndex=0;
        for(var chestPos:List.of(new BlockPos(69,WorldHeights.fromLegacy(72),159),new BlockPos(69,WorldHeights.fromLegacy(72),160))){
            var container=(net.minecraft.world.Container)server.overworld().getBlockEntity(chestPos);
            for(int slot=0;slot<container.getContainerSize()&&itemIndex<carried.size();slot++)container.setItem(slot,carried.get(itemIndex++));
            container.setChanged();
        }
        saveWorld();
        return "灯笼鱼型已放入改装泊位，原艇与存储已备份，原物品已转入新艇箱子与木桶。检查布局后提交改装；取消可恢复原艇。";
    }
    private String begin(ServerPlayer p,FleetModule m) throws IOException {
        var body=currentBody(m);var b=body.getPlot().getBoundingBox();
        if(!body.getPlot().getContraptions().isEmpty())throw new IllegalStateException("首版暂不支持运动机械与子船快照");
        long volume=(long)(b.maxX()-b.minX()+1)*(b.maxY()-b.minY()+1)*(b.maxZ()-b.minZ()+1);
        if(volume<=0||volume>4096)throw new IllegalStateException("首版支持4096格以内的船体");
        for(int x=b.minX()>>4;x<=b.maxX()>>4;x++)for(int z=b.minZ()>>4;z<=b.maxZ()>>4;z++)
            if(body.getPlot().getChunk(body.getPlot().toLocal(new net.minecraft.world.level.ChunkPos(x,z)))==null)
                throw new IllegalStateException("船体区块尚未加载，无法完整备份，请稍候再开始");
        var target=new BoundingBox3i(PAD.getX(),PAD.getY(),PAD.getZ(),PAD.getX()+b.maxX()-b.minX(),PAD.getY()+b.maxY()-b.minY(),PAD.getZ()+b.maxZ()-b.minZ());
        if(target.maxX()>128||target.maxY()>WorldHeights.fromLegacy(100)||target.maxZ()>240)throw new IllegalStateException("船体超出改装泊位");
        for(var pos:positions(target))if(!server.overworld().getBlockState(pos).isAir())throw new IllegalStateException("改装泊位被占用："+pos.toShortString());
        for(var other:m.adapter().loaded(server.overworld()))if(other!=body&&!other.isRemoved()&&worldBox(other).intersects(target.toAABB()))throw new IllegalStateException("改装泊位有其他物理船体");
        var original=capture(new BoundingBox3i(b));
        var data=new CompoundTag();data.putInt("schema",2);data.putString("phase","PREPARING");
        data.putUUID("owner",p.getUUID());data.putUUID("vessel",m.registry().flagship().orElseThrow().vesselId());data.putUUID("originalBody",body.getUniqueId());
        data.putIntArray("worldBounds",new int[]{target.minX(),target.minY(),target.minZ(),target.maxX(),target.maxY(),target.maxZ()});data.put("original",original);
        data.putUUID("businessBackup",m.storage().backup(m));
        DockFiles.create(file,data);journal=data;
        try{
            m.starter().releaseMooring();
            disassemble(body,new BlockPos(b.minX(),b.minY(),b.minZ()));
            saveWorld();setPhase("STATIC");
            return "备份已保存，物理船体已拆为船坞世界方块。无需预先密闭或供氧；完成时才检查三项条件。";
        }catch(IOException|RuntimeException e){blocked=true;throw e;}
    }
    private List<BlockPos> positions(BoundingBox3i b){return BlockPos.betweenClosedStream(b.minX(),b.minY(),b.minZ(),b.maxX(),b.maxY(),b.maxZ()).map(BlockPos::immutable).toList();}
    private boolean solidPart(BlockPos p){
        var level=server.overworld();if(!level.hasChunkAt(p))throw new IllegalArgumentException("连接方块所在区块未加载："+p.toShortString());
        var state=level.getBlockState(p);return !state.isAir()&&!(state.getBlock() instanceof net.minecraft.world.level.block.LiquidBlock);
    }
    /** Six-face connectivity, seeded by the original envelope; free fluids and detached objects stay in the world. */
    private List<BlockPos> connectedBlocks(){
        var found=new LinkedHashSet<BlockPos>();var queue=new ArrayDeque<BlockPos>();
        for(var p:positions(bounds()))if(solidPart(p)){found.add(p);queue.add(p);}
        var b=bounds();int minX=b.minX(),minY=b.minY(),minZ=b.minZ(),maxX=b.maxX(),maxY=b.maxY(),maxZ=b.maxZ();
        while(!queue.isEmpty()){
            var p=queue.remove();
            for(var direction:net.minecraft.core.Direction.values()){
                var next=p.relative(direction);if(found.contains(next)||!solidPart(next))continue;
                if(!contains(next))throw new IllegalArgumentException("连接结构超出船坞改装范围，请断开与岸边建筑的连接："+next.toShortString());
                minX=Math.min(minX,next.getX());minY=Math.min(minY,next.getY());minZ=Math.min(minZ,next.getZ());
                maxX=Math.max(maxX,next.getX());maxY=Math.max(maxY,next.getY());maxZ=Math.max(maxZ,next.getZ());
                if((long)(maxX-minX+1)*(maxY-minY+1)*(maxZ-minZ+1)>4096)throw new IllegalArgumentException("连接后船体包围盒超过4096格，请缩小扩建或断开岸边结构");
                found.add(next);queue.add(next);
            }
        }
        return List.copyOf(found);
    }
    private void rememberConnected() throws IOException {
        var blocks=connectedBlocks();journal.putLongArray("assembledBlocks",blocks.stream().mapToLong(BlockPos::asLong).toArray());DockFiles.replace(file,journal);
    }
    private List<BlockPos> selectedBlocks(){return journal.contains("assembledBlocks")?java.util.Arrays.stream(journal.getLongArray("assembledBlocks")).mapToObj(BlockPos::of).toList():positions(bounds()).stream().filter(this::solidPart).toList();}
    private BoundingBox3i assemblyBounds(List<BlockPos> blocks){
        var b=bounds();int x=b.minX(),y=b.minY(),z=b.minZ(),xx=b.maxX(),yy=b.maxY(),zz=b.maxZ();
        for(var p:blocks){x=Math.min(x,p.getX());y=Math.min(y,p.getY());z=Math.min(z,p.getZ());xx=Math.max(xx,p.getX());yy=Math.max(yy,p.getY());zz=Math.max(zz,p.getZ());}
        return new BoundingBox3i(x,y,z,xx,yy,zz);
    }
    public List<BlockPos> storagePositions(FleetModule m){
        if(blockMode())return connectedBlocks();
        var body=currentBody(m);if(body==null||body.isRemoved())throw new IllegalStateException("主艇未加载，保留已有存储档案");
        var b=body.getPlot().getBoundingBox();
        if((long)(b.maxX()-b.minX()+1)*(b.maxY()-b.minY()+1)*(b.maxZ()-b.minZ()+1)>4096)throw new IllegalStateException("主艇扫描范围超过当前上限");
        return positions(new BoundingBox3i(b));
    }
    public BlockPos storageOrigin(FleetModule m){
        if(blockMode())return PAD;
        var body=currentBody(m);if(body==null)throw new IllegalStateException("主艇未加载");
        var b=body.getPlot().getBoundingBox();return new BlockPos(b.minX(),b.minY(),b.minZ());
    }
    /** Snapshot positions are relative, so a new Sable plot can receive the original data. */
    private ListTag capture(BoundingBox3i b){
        return capture(positions(b),new BlockPos(b.minX(),b.minY(),b.minZ()));
    }
    private ListTag capture(List<BlockPos> blocks,BlockPos origin){
        var entries=new ListTag();var level=server.overworld();
        for(var p:blocks){
            var state=level.getBlockState(p);
            var e=new CompoundTag();e.putLong("offset",p.subtract(origin).asLong());e.put("state",NbtUtils.writeBlockState(state));
            var be=level.getBlockEntity(p);
            if(state.hasBlockEntity()&&be==null)throw new IllegalArgumentException("设备数据未加载，无法完整备份："+p.toShortString());
            if(be!=null){
                var tag=be.saveWithFullMetadata(level.registryAccess());
                if(BlockEntity.loadStatic(p,state,tag.copy(),level.registryAccess())==null)
                    throw new IllegalArgumentException("设备数据无法恢复："+state.getBlock()+"，位置 "+p.toShortString());
                e.put("be",tag);
            }
            entries.add(e);
        }
        return entries;
    }
    private void disassemble(ServerSubLevel body,BlockPos origin) throws IOException {
        var level=server.overworld();var b=new BoundingBox3i(body.getPlot().getBoundingBox());
        var blocks=positions(b).stream().filter(p->!level.getBlockState(p).isAir()).toList();
        var states=blocks.stream().map(level::getBlockState).toList();
        var transform=new SubLevelAssemblyHelper.AssemblyTransform(origin,PAD,0,Rotation.NONE,level);
        ((ServerLevelPlot)body.getPlot()).kickAllEntities();
        if(!blocks.isEmpty())SubLevelAssemblyHelper.moveBlocks(level,transform,blocks);
        SubLevelAssemblyHelper.moveTrackingPoints(level,b,null,transform);
        for(int i=0;i<blocks.size();i++)if(!sameTransferredState(states.get(i),level.getBlockState(transform.apply(blocks.get(i)))))throw new IOException("拆解结果不完整，停止转换并保留备份："+blocks.get(i).toShortString()+" / 预期="+states.get(i)+" / 实际="+level.getBlockState(transform.apply(blocks.get(i))));
        dev.ryanhcode.sable.api.sublevel.SubLevelContainer.getContainer(level).removeSubLevel(body,dev.ryanhcode.sable.sublevel.storage.SubLevelRemovalReason.REMOVED);
    }
    private ServerSubLevel assemble(FleetModule m,boolean restored) throws IOException {
        var level=server.overworld();var blocks=restored?positions(bounds()).stream().filter(p->!level.getBlockState(p).isAir()).toList():selectedBlocks();
        if(blocks.isEmpty())throw new IllegalArgumentException("船体为空");
        var states=blocks.stream().map(level::getBlockState).toList();
        setPhase(restored?"RESTORING":"ASSEMBLING");
        try{
            var body=SubLevelAssemblyHelper.assembleBlocks(level,PAD,blocks,restored?bounds():assemblyBounds(blocks));
            if(body==null||body.isRemoved())throw new IOException("Sable装配失败");
            journal.putUUID("candidate",body.getUniqueId());
            // Commit the allocated UUID before changing authoritative identity metadata.
            DockFiles.replace(file,journal);
            var plotOrigin=body.getPlot().getCenterBlock();
            for(int i=0;i<blocks.size();i++){
                var target=plotOrigin.offset(blocks.get(i).subtract(PAD));
                if(!level.getBlockState(blocks.get(i)).isAir()||!sameTransferredState(states.get(i),level.getBlockState(target))
                        ||states.get(i).hasBlockEntity()&&level.getBlockEntity(target)==null)
                    throw new IOException("装配结果不完整，已保留原始备份和候选船体记录："+blocks.get(i).toShortString()+" / 预期="+states.get(i)+" / 实际="+level.getBlockState(target)+" / 原位="+level.getBlockState(blocks.get(i)));
            }
            var expected=m.registry().flagship().orElseThrow().bodyId();m.rebindForRefit(expected,body);
            m.starter().adoptRefitBody(body);m.starter().tick(m);
            saveWorld();setPhase(restored?"RESTORED":"VALIDATING");
            return body;
        }catch(IOException|RuntimeException e){blocked=true;throw e;}
    }
    private static boolean sameTransferredState(BlockState expected,BlockState actual){
        if(expected.equals(actual))return true;
        if(expected.getBlock()!=actual.getBlock())return false;
        // Create recalculates pipe arms when moving into a plot. Arms are derived from neighbours,
        // not inventory or orientation; still verify block identity, waterlogging and every other property.
        String block=net.minecraft.core.registries.BuiltInRegistries.BLOCK.getKey(expected.getBlock()).toString();
        Set<String> arms=switch(block){
            case "create:fluid_pipe" -> Set.of("north","south","east","west","up","down");
            case "create:clutch", "create:gearshift" -> Set.of("powered");
            default -> Set.of();
        };
        if(arms.isEmpty())return false;
        for(var property:expected.getProperties())if(!arms.contains(property.getName())&&!expected.getValue(property).equals(actual.getValue(property)))return false;
        return true;
    }
    private void restoreWorld() throws IOException {
        var level=server.overworld();var entries=journal.getList("original",Tag.TAG_COMPOUND);
        var states=new ArrayList<BlockState>();
        for(int i=0;i<entries.size();i++){
            var state=NbtUtils.readBlockState(level.holderLookup(Registries.BLOCK),entries.getCompound(i).getCompound("state"));
            states.add(state);
        }
        // Restore all states before any neighbour updates: doors, beds and attached blocks
        // must not break while their other half/support has not been restored yet.
        int restoreFlags=net.minecraft.world.level.block.Block.UPDATE_CLIENTS|net.minecraft.world.level.block.Block.UPDATE_KNOWN_SHAPE;
        var cleanup=new HashSet<>(positions(bounds()));cleanup.addAll(selectedBlocks());
        for(var p:cleanup){level.removeBlockEntity(p);level.setBlock(p,Blocks.AIR.defaultBlockState(),restoreFlags);}
        for(int i=0;i<entries.size();i++){
            var p=PAD.offset(BlockPos.of(entries.getCompound(i).getLong("offset")));
            level.setBlock(p,states.get(i),restoreFlags);
        }
        for(int i=0;i<entries.size();i++){
            var e=entries.getCompound(i);var p=PAD.offset(BlockPos.of(e.getLong("offset")));var state=states.get(i);
            if(e.contains("be")){
                var tag=e.getCompound("be").copy();tag.putInt("x",p.getX());tag.putInt("y",p.getY());tag.putInt("z",p.getZ());
                var be=BlockEntity.loadStatic(p,state,tag,level.registryAccess());if(be==null)throw new IOException("设备恢复失败");
                level.setBlockEntity(be);be.setChanged();level.sendBlockUpdated(p,state,state,3);
            }
        }
    }
    private void rollback(FleetModule m,String outcome) throws IOException {
        if(phase().equals("STATIC"))rememberConnected();
        try{
            setPhase("ROLLING_BACK");
            if(journal.hasUUID("candidate")){
                var candidate=m.adapter().find(server.overworld(),journal.getUUID("candidate"));
                if(candidate==null)throw new IOException("待还原物理船体未加载，保留备份");
                m.starter().releaseMooring();disassemble(candidate,candidate.getPlot().getCenterBlock());
                journal.remove("candidate");
            }
            restoreWorld();assemble(m,true);
            if(journal.hasUUID("businessBackup"))m.storage().store(m).restore(journal.getUUID("businessBackup"));
            finish(outcome);m.storage().capture(m);
        }catch(IOException|RuntimeException e){blocked=true;throw e;}
    }
    public void recover(FleetModule m) throws IOException {
        legacy.recover(m);
        if(!recovering||blocked||journal==null)return;
        try{
            verifyIdentity(m);
            for(var p:positions(bounds()))server.overworld().getChunkAt(p);
            var old=m.adapter().find(server.overworld(),journal.getUUID("originalBody"));
            if(old!=null&&!old.isRemoved())throw new IOException("原物理船体仍存在，禁止重复恢复");
            rollback(m,"interrupted");recovering=false;
        }catch(IOException|RuntimeException e){blocked=true;throw e;}
    }
    public void process(FleetModule m) throws IOException {
        if(journal==null){legacy.process(m);return;}
        if(!checking()||blocked||recovering||server.getTickCount()%20!=0)return;
        try{verifyIdentity(m);}catch(RuntimeException e){blocked=true;throw e;}
        var body=currentBody(m);if(body==null)return;
        var q=m.inspection().inspect(body).qualification();
        if(!q.eligible()&&server.overworld().getGameTime()<deadline)return;
        var owner=server.getPlayerList().getPlayer(journal.getUUID("owner"));String result;
        if(q.eligible()){finish("committed");result="三项验收通过：改装完成，已恢复物理船体；主艇永久身份不变。";}
        else{rollback(m,"rejected");result="完成验收失败，已恢复改装前物理船体："+String.join("；",q.reasons());}
        if(owner!=null&&!io.github.low2uu.dbv.fleet.ui.FleetPanel.notifyResult(owner,result))owner.sendSystemMessage(net.minecraft.network.chat.Component.literal(result));
    }
    private void finish(String outcome) throws IOException {
        saveWorld();var history=file.getParent().resolve("history");Files.createDirectories(history);
        Files.move(file,history.resolve(UUID.randomUUID()+"-blocks-"+outcome+".nbt"),StandardCopyOption.ATOMIC_MOVE);
        journal=null;deadline=0;
    }
    private void setPhase(String phase) throws IOException {journal.putString("phase",phase);DockFiles.replace(file,journal);}
    private void saveWorld() throws IOException {if(!server.saveEverything(false,true,true))throw new IOException("世界保存失败，保留改装备份");}
    private void verifyIdentity(FleetModule m){
        if(m.registry().flagship().isEmpty()||!m.registry().flagship().orElseThrow().vesselId().equals(journal.getUUID("vessel")))
            throw new IllegalStateException("活动改装与永久主艇身份不符，禁止转换");
    }
    private void validate() throws IOException {
        if(journal.getInt("schema")!=2||!journal.hasUUID("vessel")||!journal.hasUUID("owner")||!journal.hasUUID("originalBody"))throw new IOException("方块改装档案头损坏");
        if(!Set.of("PREPARING","STATIC","ASSEMBLING","VALIDATING","ROLLING_BACK","RESTORING","RESTORED").contains(phase()))throw new IOException("未知改装转换阶段");
        var b=journal.getIntArray("worldBounds");
        if(b.length!=6||b[0]!=60||b[1]!=WorldHeights.fromLegacy(68)||b[2]!=150||b[3]<b[0]||b[4]<b[1]||b[5]<b[2]||b[3]>128||b[4]>WorldHeights.fromLegacy(100)||b[5]>240)throw new IOException("改装区域无效");
        long volume=(long)(b[3]-b[0]+1)*(b[4]-b[1]+1)*(b[5]-b[2]+1);var list=journal.getList("original",Tag.TAG_COMPOUND);
        if(volume>4096||list.size()!=volume)throw new IOException("改装备份数量无效");
        if(journal.contains("assembledBlocks")){
            var selection=selectedBlocks();
            if(selection.size()>4096||new HashSet<>(selection).size()!=selection.size()||selection.stream().anyMatch(p->!contains(p)))throw new IOException("扩建方块记录无效");
            var box=assemblyBounds(selection);
            if((long)(box.maxX()-box.minX()+1)*(box.maxY()-box.minY()+1)*(box.maxZ()-box.minZ()+1)>4096)throw new IOException("扩建记录范围过大");
        }
        var seen=new HashSet<Long>();
        for(int i=0;i<list.size();i++){
            var e=list.getCompound(i);var p=BlockPos.of(e.getLong("offset"));
            if(!e.contains("state",Tag.TAG_COMPOUND)||!seen.add(p.asLong())||p.getX()<0||p.getY()<0||p.getZ()<0||p.getX()>b[3]-b[0]||p.getY()>b[4]-b[1]||p.getZ()>b[5]-b[2])throw new IOException("改装备份条目无效");
        }
    }
    public boolean mayEdit(ServerPlayer p,BlockPos pos,BlockState state){
        if(legacy.active())return legacy.mayEdit(p,pos,state);
        return journal!=null&&phase().equals("STATIC")&&!blocked&&!recovering&&contains(pos)&&p.serverLevel()==server.overworld()&&p.isCreative()&&p.hasPermissions(2)&&ConstructionMode.ENABLED&&journal.getUUID("owner").equals(p.getUUID());
    }
    public VesselInspection.Report report(FleetModule m){
        if(!blockMode()){var body=currentBody(m);return body==null?new VesselInspection.Report(new Qualification(false,false,false,true),List.of(),server.overworld().getGameTime()):m.inspection().inspect(body);}
        boolean controller=false;var devices=new ArrayList<VesselInspection.Device>();var level=server.overworld();
        for(var p:connectedBlocks()){
            var id=net.minecraft.core.registries.BuiltInRegistries.BLOCK.getKey(level.getBlockState(p).getBlock()).toString();
            if(id.equals("create_submarine:command_sub"))controller=true;
            if(level.getBlockState(p).hasBlockEntity())devices.add(new VesselInspection.Device(id,id.equals("create_submarine:command_sub")?"中央控制器":id.equals("create_submarine:electrolyzer")?"氧气制造":id.equals("create_submarine:oxygene_diffuser")?"供氧":"其他设备",p,p.subtract(PAD),"方块改装阶段；运行资格在完成时检测"));
        }
        return new VesselInspection.Report(new Qualification(controller,false,false,true),devices,level.getGameTime());
    }
}
