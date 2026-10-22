package io.github.low2uu.dbv.fleet.door;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.phys.BlockHitResult;

public final class PersonnelHatch extends DoorBlock {
    public static final MapCodec<PersonnelHatch> CODEC=simpleCodec(PersonnelHatch::new);
    public PersonnelHatch(Properties p){super(BlockSetType.IRON,p);registerDefaultState(defaultBlockState().setValue(CabinDoors.LOCKED,false));}
    @Override public MapCodec<? extends DoorBlock> codec(){return CODEC;}
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> b){super.createBlockStateDefinition(b);b.add(CabinDoors.LOCKED);}
    // All opening is routed through the server safety check; redstone cannot bypass the lock.
    @Override protected void neighborChanged(BlockState s,Level l,BlockPos p,Block b,BlockPos from,boolean moving){}
    @Override public BlockState getStateForPlacement(net.minecraft.world.item.context.BlockPlaceContext c){
        var state=super.getStateForPlacement(c);
        return state==null?null:state.setValue(OPEN,false).setValue(POWERED,false).setValue(CabinDoors.LOCKED,false);
    }
    @Override protected InteractionResult useWithoutItem(BlockState s,Level l,BlockPos p,Player player,BlockHitResult hit){return InteractionResult.CONSUME;}
}
