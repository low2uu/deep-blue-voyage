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
    // Keep vanilla redstone operation for an unlocked door; the user-requested lock remains authoritative.
    @Override protected void neighborChanged(BlockState s,Level l,BlockPos p,Block b,BlockPos from,boolean moving){
        if(s.getValue(CabinDoors.LOCKED))return;
        var other=s.getValue(HALF)==net.minecraft.world.level.block.state.properties.DoubleBlockHalf.LOWER?p.above():p.below();
        if(!l.isClientSide&&!s.getValue(OPEN)&&(l.hasNeighborSignal(p)||l.hasNeighborSignal(other)))
            DoorSafety.report(l,java.util.Set.of(p,other));
        super.neighborChanged(s,l,p,b,from,moving);
    }
    @Override public BlockState getStateForPlacement(net.minecraft.world.item.context.BlockPlaceContext c){
        var state=super.getStateForPlacement(c);
        return state==null?null:state.setValue(CabinDoors.LOCKED,false);
    }
    @Override protected InteractionResult useWithoutItem(BlockState s,Level l,BlockPos p,Player player,BlockHitResult hit){return InteractionResult.CONSUME;}
}
