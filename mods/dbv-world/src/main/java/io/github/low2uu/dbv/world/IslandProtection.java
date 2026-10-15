package io.github.low2uu.dbv.world;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.level.ExplosionEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.entity.EntityMobGriefingEvent;
import net.neoforged.neoforge.event.level.PistonEvent;

/** Protects the stationary starter island; ship plots and authorised dock edits remain independent. */
public final class IslandProtection {
    private IslandProtection() {}
    public static boolean inBounds(int x,int z) { return x>=-280&&x<=280&&z>=-216&&z<=256; }
    public static boolean protectedAt(LevelAccessor level,BlockPos pos) {
        return level instanceof ServerLevel s && s.dimension()==Level.OVERWORLD
                && s.getChunkSource().getGenerator() instanceof StarterIslandGenerator
                && inBounds(pos.getX(),pos.getZ());
    }
    private static boolean bypass(Player player) { return player!=null&&player.isCreative()&&player.hasPermissions(2); }
    private static boolean deny(LevelAccessor level,BlockPos pos,Player player) {
        if(!protectedAt(level,pos)||bypass(player))return false;
        if(player!=null)player.displayClientMessage(Component.literal("起始岛保护：此处禁止破坏或放置方块；管理员可用创造模式建设。"),true);
        return true;
    }
    public static void initialize() {
        NeoForge.EVENT_BUS.addListener(IslandProtection::breakBlock);
        NeoForge.EVENT_BUS.addListener(IslandProtection::placeBlock);
        NeoForge.EVENT_BUS.addListener(IslandProtection::explode);
        NeoForge.EVENT_BUS.addListener(IslandProtection::interact);
        NeoForge.EVENT_BUS.addListener(IslandProtection::tool);
        NeoForge.EVENT_BUS.addListener(IslandProtection::trample);
        NeoForge.EVENT_BUS.addListener(IslandProtection::piston);
        NeoForge.EVENT_BUS.addListener(IslandProtection::mob);
    }
    private static void breakBlock(BlockEvent.BreakEvent e) { if(deny(e.getLevel(),e.getPos(),e.getPlayer()))e.setCanceled(true); }
    private static void placeBlock(BlockEvent.EntityPlaceEvent e) {
        var p=e.getEntity() instanceof Player player?player:null;
        boolean denied=deny(e.getLevel(),e.getPos(),p);
        if(e instanceof BlockEvent.EntityMultiPlaceEvent multi)
            denied|=multi.getReplacedBlockSnapshots().stream().anyMatch(b->deny(e.getLevel(),b.getPos(),p));
        if(denied)e.setCanceled(true);
    }
    private static void explode(ExplosionEvent.Detonate e) {
        e.getAffectedBlocks().removeIf(p->protectedAt(e.getLevel(),p));
        e.getAffectedEntities().removeIf(entity->protectedAt(e.getLevel(),entity.blockPosition()));
    }
    private static void interact(PlayerInteractEvent.RightClickBlock e) {
        var item=e.getItemStack().getItem();
        String id=net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(item).getPath();
        boolean dangerous=item instanceof net.minecraft.world.item.BucketItem
                ||item instanceof net.minecraft.world.item.FlintAndSteelItem
                ||item instanceof net.minecraft.world.item.FireChargeItem
                ||item instanceof net.minecraft.world.item.EndCrystalItem
                ||item instanceof net.minecraft.world.item.MinecartItem
                ||id.contains("wrench")||id.contains("schematicannon");
        if(dangerous&&(deny(e.getLevel(),e.getPos(),e.getEntity())
                ||e.getFace()!=null&&deny(e.getLevel(),e.getPos().relative(e.getFace()),e.getEntity())))e.setCanceled(true);
    }
    private static void tool(BlockEvent.BlockToolModificationEvent e) { if(deny(e.getLevel(),e.getPos(),e.getPlayer()))e.setCanceled(true); }
    private static void trample(BlockEvent.FarmlandTrampleEvent e) { if(protectedAt(e.getLevel(),e.getPos()))e.setCanceled(true); }
    private static void piston(PistonEvent.Pre e) {
        // A conservative boundary margin also prevents pistons outside the island pulling blocks out.
        for(int d=-13;d<=13;d++)if(protectedAt(e.getLevel(),e.getPos().relative(e.getDirection(),d))){e.setCanceled(true);return;}
    }
    private static void mob(EntityMobGriefingEvent e) { if(protectedAt(e.getEntity().level(),e.getEntity().blockPosition()))e.setCanGrief(false); }
}
