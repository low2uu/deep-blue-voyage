package dbv.acceptance;
public final class World5Probe {
 public static void remove(dev.ryanhcode.sable.sublevel.ServerSubLevel b){
  com.enxv.aeronauticsstructuretool.blueprint.lifecycle.SubLevelRemovalCoordinator.remove(b.getLevel(),dev.ryanhcode.sable.api.sublevel.SubLevelContainer.getContainer(b.getLevel()),java.util.List.of(b));
 }
 public static void console(dev.ryanhcode.sable.sublevel.ServerSubLevel b){
  var l=b.getLevel();var controllers=new java.util.ArrayList<net.minecraft.core.BlockPos>();
  for(var h:b.getPlot().getLoadedChunks())if(h.getChunk()!=null)for(var be:h.getChunk().getBlockEntities().values())
   if(be instanceof com.maxenonyme.createsubmarine.submarine.block.entity.CommandSubBlockEntity)controllers.add(be.getBlockPos());
  if(controllers.size()!=1)throw new IllegalStateException("控制器数量异常");
  var p=controllers.getFirst();var before=l.getBlockEntity(p).saveWithFullMetadata(l.registryAccess());
  var state=io.github.low2uu.dbv.fleet.ui.CabinPanelBlock.CONSOLE.get().defaultBlockState().setValue(net.minecraft.world.level.block.HorizontalDirectionalBlock.FACING,l.getBlockState(p).getValue(net.minecraft.world.level.block.HorizontalDirectionalBlock.FACING));
  l.setBlock(p,state,3);l.getBlockEntity(p).loadWithComponents(before,l.registryAccess());l.getBlockEntity(p).setChanged();
 }
 public static void adopt(net.minecraft.server.MinecraftServer s) throws Exception {
  var m=io.github.low2uu.dbv.fleet.FleetModule.get(s);var l=s.overworld();var old=m.refit().currentBody(m);
  var b=m.adapter().find(l,java.util.UUID.fromString("66d01998-fbc7-47d8-b8ac-7097dadc716a"));
  if(b==null||old==null||old==b)throw new IllegalStateException("交付目标异常");
  m.inspection().inspect(b).requireEligible();m.storage().backup(m);console(b);m.starter().releaseMooring();
  var box=b.boundingBox();var pos=new org.joml.Vector3d(b.logicalPose().position()).add(48-box.minX(),133-box.minY(),154-box.minZ());
  dev.ryanhcode.sable.sublevel.system.SubLevelPhysicsSystem.require(l).getPipeline().teleport(b,pos,b.logicalPose().orientation());b.updateLastPose();
  m.rebindForRefit(old.getUniqueId(),b);m.starter().adoptRefitBody(b);remove(old);
 }
}
