package io.github.low2uu.dbv.fleet.door;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.*;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import java.util.*;

/** Door state is ordinary block state, already included in vessel structure snapshots. */
public final class CabinDoors {
    public static final BooleanProperty LOCKED=BooleanProperty.create("locked");
    private static final DeferredRegister<Block> BLOCKS=DeferredRegister.create(Registries.BLOCK,"dbv_world");
    private static final DeferredRegister<Item> ITEMS=DeferredRegister.create(Registries.ITEM,"dbv_world");
    public static final java.util.function.Supplier<Block> PERSON=BLOCKS.register("personnel_hatch",()->new PersonnelHatch(BlockBehaviour.Properties.of().strength(5,6).sound(SoundType.METAL).noOcclusion()));
    public static final java.util.function.Supplier<Block> CORE=BLOCKS.register("hangar_door_core",()->new HangarDoorBlock(BlockBehaviour.Properties.of().strength(5,6).sound(SoundType.METAL).noOcclusion()));
    public static final java.util.function.Supplier<Block> PANEL=BLOCKS.register("hangar_door_panel",()->new HangarDoorBlock(BlockBehaviour.Properties.of().strength(5,6).sound(SoundType.METAL).noOcclusion()));
    static {
        ITEMS.register("personnel_hatch",()->new DoorItem(PERSON.get()));
        ITEMS.register("hangar_door_core",()->new DoorItem(CORE.get()));
        ITEMS.register("hangar_door_panel",()->new DoorItem(PANEL.get()));
    }
    public static boolean isDoor(BlockState s){return s.getBlock() instanceof PersonnelHatch||s.getBlock() instanceof HangarDoorBlock;}
    public static boolean sealed(BlockState s){return s.getValue(LOCKED)&&!s.getValue(BlockStateProperties.OPEN);}
    public static void register(IEventBus bus){BLOCKS.register(bus);ITEMS.register(bus);NeoForge.EVENT_BUS.addListener(CabinDoors::interact);}
    private static void interact(PlayerInteractEvent.RightClickBlock event){
        if(event.getHand()!=InteractionHand.MAIN_HAND||!event.getItemStack().isEmpty())return;
        var level=event.getLevel();var clicked=event.getPos();var state=level.getBlockState(clicked);
        if(!isDoor(state))return;
        event.setCanceled(true);event.setCancellationResult(InteractionResult.SUCCESS);
        if(!(event.getEntity() instanceof ServerPlayer player))return;
        try {
            if(!player.mayBuild())throw new IllegalStateException("permission");
            var view=new DoorBlockView(level,clicked);
            Set<BlockPos> cells=new LinkedHashSet<>();
            if(state.getBlock() instanceof PersonnelHatch){
                var lower=state.getValue(DoorBlock.HALF)==DoubleBlockHalf.UPPER?clicked.below():clicked;
                if(!view.state(lower).is(PERSON.get())||!view.state(lower.above()).is(PERSON.get()))throw new IllegalStateException("incomplete");
                cells.add(lower);cells.add(lower.above());
            }else{
                var queue=new ArrayDeque<BlockPos>();queue.add(clicked);cells.add(clicked);int cores=0;
                while(!queue.isEmpty()){
                    var pos=queue.remove();var part=view.state(pos);
                    if(part.is(CORE.get()))cores++;
                    if(part.getValue(HangarDoorBlock.FACING)!=state.getValue(HangarDoorBlock.FACING))throw new IllegalStateException("facing");
                    for(var direction:Direction.values()){
                        var next=pos.relative(direction);
                        if(view.exterior(next))continue;
                        if(view.state(next).getBlock() instanceof HangarDoorBlock&&cells.add(next)){
                            if(cells.size()>4096)throw new IllegalStateException("limit");queue.add(next);
                        }
                    }
                }
                if(cores!=1)throw new IllegalStateException("core");
            }
            for(var pos:cells)if(!level.mayInteract(player,pos))throw new IllegalStateException("permission");
            boolean open=state.getValue(BlockStateProperties.OPEN),locked=state.getValue(LOCKED);
            // New attached sections must match the assembly before it can operate.
            if(cells.stream().anyMatch(p->level.getBlockState(p).getValue(BlockStateProperties.OPEN)!=open))throw new IllegalStateException("mixed");
            boolean newOpen=open,newLocked=locked;
            String message;
            if(player.isShiftKeyDown()){
                if(open)throw new IllegalStateException("close_first");
                if(locked){DoorSafety.requireSafe(level,cells,state.getValue(BlockStateProperties.HORIZONTAL_FACING));newLocked=false;message="unlocked";}
                else {newLocked=true;message="locked";}
            }else if(open){newOpen=false;newLocked=false;message="closed";}
            else {
                if(cells.stream().anyMatch(p->level.getBlockState(p).getValue(LOCKED)))throw new IllegalStateException("unlock_first");
                DoorSafety.requireSafe(level,cells,state.getValue(BlockStateProperties.HORIZONTAL_FACING));newOpen=true;newLocked=false;message="opened";
            }
            if(!newOpen&&open){
                for(var pos:cells)if(!level.getEntities((net.minecraft.world.entity.Entity)null,new net.minecraft.world.phys.AABB(pos),e->e.isAlive()&&!e.isSpectator()).isEmpty())throw new IllegalStateException("occupied");
            }
            for(var pos:cells){var old=level.getBlockState(pos);level.setBlock(pos,old.setValue(BlockStateProperties.OPEN,newOpen).setValue(LOCKED,newLocked),Block.UPDATE_CLIENTS);}
            for(var pos:cells)level.updateNeighborsAt(pos,level.getBlockState(pos).getBlock());
            player.displayClientMessage(Component.translatable("message.dbv_world.door."+message),true);
        }catch(IllegalStateException error){player.displayClientMessage(Component.translatable("message.dbv_world.door."+error.getMessage()),true);}
    }
    private CabinDoors(){}
    private static final class DoorItem extends BlockItem {
        DoorItem(Block block){super(block,new Item.Properties());}
        @Override public void appendHoverText(net.minecraft.world.item.ItemStack stack,Item.TooltipContext context,List<Component> lines,net.minecraft.world.item.TooltipFlag flag){
            lines.add(Component.translatable("tooltip.dbv_world.hatch_controls"));
            lines.add(Component.translatable("tooltip.dbv_world.hatch_seal"));
            if(getBlock() instanceof HangarDoorBlock)lines.add(Component.translatable("tooltip.dbv_world.hangar_assembly"));
        }
    }
}
