package io.github.low2uu.dbv.fleet.dock;

import com.maxenonyme.createsubmarine.submarine.block.entity.ElectrolyzerBlockEntity;
import io.github.low2uu.dbv.fleet.FleetModule;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction;
import java.util.LinkedHashMap;
import java.util.Map;

/** Dock blueprint. All machines are real registered blocks and use their own mechanics. */
public final class AnglerSubmarine {
    public static final BlockPos MIN = new BlockPos(60,68,150), MAX = new BlockPos(74,78,173);
    private final Map<BlockPos,BlockState> blocks = new LinkedHashMap<>();
    private void put(int x,int y,int z,String id,String... properties) {
        var key=ResourceLocation.parse(id.contains(":")?id:"create:"+id);
        if(!BuiltInRegistries.BLOCK.containsKey(key))throw new IllegalStateException("潜艇蓝图缺少方块："+key);
        BlockState state=BuiltInRegistries.BLOCK.get(key).defaultBlockState();
        for(String entry:properties){var pair=entry.split("=");var property=state.getBlock().getStateDefinition().getProperty(pair[0]);
            if(property==null)throw new IllegalStateException("蓝图属性不存在："+id+" / "+entry);
            state=property(state,property,pair[1]);}
        blocks.put(new BlockPos(x,y,z),state);
    }
    private static <T extends Comparable<T>> BlockState property(BlockState state,Property<T> property,String value){
        return state.setValue(property,property.getValue(value).orElseThrow());
    }
    private void iron(int x,int y,int z){put(x,y,z,"minecraft:iron_block");}
    private void shaft(int x,int y,int z,String axis){put(x,y,z,"shaft","axis="+axis);}
    private void gear(int x,int y,int z,String axis){put(x,y,z,"gearbox","axis="+axis);}
    private void pipe(int x,int y,int z){put(x,y,z,"fluid_pipe");}
    private void door(int x,int y,int z){
        put(x,y,z,"minecraft:iron_door","facing=south","half=lower");
        put(x,y+1,z,"minecraft:iron_door","facing=south","half=upper");
        put(x-1,y,z-1,"minecraft:stone_button","face=wall","facing=north");
        put(x+1,y,z+1,"minecraft:stone_button","face=wall","facing=south");
    }
    private static boolean inside(int x,int y,int z){
        if(z<150||z>173||y<69||y>76)return false;
        double width=z<153?2+(z-150)*1.5:z>165?Math.max(1.2,6-(z-165)*.65):6;
        return Math.pow((x-67)/width,2)+Math.pow((y-72.5)/3.7,2)<=1;
    }
    public static Map<BlockPos,BlockState> plan(){
        var b=new AnglerSubmarine();
        for(int x=61;x<=73;x++)for(int y=69;y<=76;y++)for(int z=150;z<=173;z++){
            if(!inside(x,y,z))continue;
            boolean edge=false;for(Direction d:Direction.values())if(!inside(x+d.getStepX(),y+d.getStepY(),z+d.getStepZ()))edge=true;
            if(edge){b.iron(x,y,z);if(z<=154&&y>=72&&y<=74)b.put(x,y,z,"create_submarine:iron_pressurizer");}
            else if(y==71)b.iron(x,y,z);
        }
        // The stalk bends towards the nose; its tip is the working sonar, not a lamp.
        b.iron(67,76,154);b.iron(67,77,154);b.iron(67,78,154);
        for(int z=151;z<=153;z++)b.iron(67,78,z);
        b.put(67,77,151,"create_submarine:sonar");
        b.put(67,72,153,"create_submarine:command_sub");
        b.put(66,72,153,"dbv_world:cabin_panel");
        b.put(69,72,159,"minecraft:chest");b.put(69,72,160,"minecraft:barrel");
        b.put(67,75,159,"minecraft:sea_lantern");
        b.put(67,76,160,"minecraft:iron_trapdoor","half=top");
        for(int y=72;y<=75;y++){b.iron(67,y,161);b.put(67,y,160,"minecraft:ladder","facing=north");}
        b.iron(68,75,160);b.put(68,76,160,"minecraft:lever","face=floor");
        for(int side:new int[]{-1,1}){
            int engine=67+4*side, bus=67+3*side, outer=67+7*side;
            b.put(engine,72,157,"createdieselgenerators:diesel_engine","facing=north");
            b.put(engine,72,156,"gearshift","axis=z");b.put(engine,73,156,"minecraft:lever","face=floor");b.gear(engine,72,155,"y");
            b.put(engine,71,154,"fluid_tank");
            b.put(engine,71,155,"mechanical_pump","facing=south");
            b.pipe(engine,71,156);b.pipe(engine,71,157);
            b.gear(bus,72,155,"y");
            b.put(bus,72,156,"cogwheel","axis=z");b.put(bus,71,156,"cogwheel","axis=z");
            b.put(bus,71,155,"cogwheel","axis=z");b.put(bus,70,155,"cogwheel","axis=z");
            // Independent propeller reversing and neutral. The service branch stays powered.
            b.put(engine+side,72,155,"clutch","axis=x","powered=true");
            b.shaft(engine+2*side,72,155,"x");
            b.put(engine+side,73,155,"minecraft:lever","face=floor","powered=true");
            b.iron(engine+2*side,73,155); // Armour behind the recessed lever keeps the pressure hull closed.
            b.gear(outer,72,155,"y");
            for(int z=156;z<=162;z++)b.shaft(outer,72,z,"z");
            b.put(outer,72,163,"create_submarine:submarine_propeller","facing=south");
            for(int z=156;z<=167;z++)b.shaft(bus,70,z,"z");
            for(int z:new int[]{154,164}){
                // Four isolated clusters, eight tanks each, at the bottom corners.
                for(int dx=0;dx<=1;dx++)for(int y=69;y<=70;y++)for(int dz=0;dz<=1;dz++)
                    b.put(engine+side*dx,y,z+dz,"create_submarine:ballast_tank");
                b.put(bus,70,z+2,"cogwheel","axis=z");
                b.put(engine,70,z+2,"create_submarine:pump_controller","facing=south");
                b.pipe(engine,70,z+3);b.pipe(engine,69,z+3);
                b.put(engine,68,z+3,"create_submarine:ballast_vent");
            }
        }
        // Diesel -> shaft alternator -> electrolyzer -> powered oxygen pump -> diffuser.
        b.put(65,72,155,"create_submarine:electrolyzer","facing=east");
        b.gear(66,72,155,"y");b.put(66,72,156,"cogwheel","axis=z");b.put(66,72,157,"cogwheel","axis=z");
        b.pipe(65,72,156);b.put(65,72,157,"mechanical_pump","facing=south");
        b.put(65,72,158,"create_submarine:oxygene_diffuser");b.put(64,72,158,"minecraft:redstone_block");
        b.put(65,73,153,"fluid_tank");b.put(65,73,154,"mechanical_pump","facing=south");b.pipe(65,73,155);
        b.put(66,73,156,"cogwheel","axis=z");b.shaft(66,73,155,"z");b.put(66,73,154,"cogwheel","axis=z");
        // Sealed aft airlock, with a dry inner door and a sea-facing outer door.
        for(int x=65;x<=69;x++)for(int y=71;y<=75;y++)for(int z=165;z<=169;z++){
            if(x==65||x==69||y==71||y==75||z==165||z==169)b.iron(x,y,z);
            else b.put(x,y,z,"minecraft:air");
        }
        b.door(67,72,165);b.door(67,72,169);
        b.put(67,74,166,"create_submarine:decompression_chamber","facing=down");
        b.pipe(67,75,166);b.put(67,75,167,"mechanical_pump","facing=north");b.pipe(67,75,168);
        b.pipe(67,76,168);b.pipe(68,76,168);b.put(69,76,168,"create_submarine:decompression_chamber","facing=east");
        b.gear(70,70,167,"y");b.gear(69,70,167,"z");
        for(int y=71;y<=74;y++)b.shaft(69,y,167,"y");
        b.put(69,73,167,"clutch","axis=y","powered=true");
        b.put(68,73,167,"minecraft:lever","face=wall","facing=west","powered=true");
        b.gear(69,75,167,"x");b.put(69,75,168,"cogwheel","axis=z");b.put(68,75,168,"cogwheel","axis=z");b.put(68,75,167,"cogwheel","axis=z");
        return java.util.Collections.unmodifiableMap(b.blocks);
    }

    public static void provision(ServerLevel level){
        ((com.maxenonyme.createsubmarine.submarine.block.entity.CommandSubBlockEntity)level.getBlockEntity(new BlockPos(67,72,153))).targetDepth=0;
        var diesel=BuiltInRegistries.FLUID.get(ResourceLocation.parse("createdieselgenerators:diesel"));
        if(diesel==Fluids.EMPTY)throw new IllegalStateException("柴油流体未注册");
        for(int x:new int[]{63,71}){
            fill(level,new BlockPos(x,71,154),new FluidStack(diesel,16000));
            fill(level,new BlockPos(x,72,157),new FluidStack(diesel,1000));
        }
        fill(level,new BlockPos(65,73,153),new FluidStack(Fluids.WATER,16000));
        var electrolyzer=(ElectrolyzerBlockEntity)level.getBlockEntity(new BlockPos(65,72,155));
        electrolyzer.module.setStackInSlot(0,new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.parse("create:shaft"))));
        electrolyzer.data.set(6,1);
        electrolyzer.waterTank.fill(new FluidStack(Fluids.WATER,4000),FluidAction.EXECUTE);
    }
    private static void fill(ServerLevel level,BlockPos p,FluidStack fluid){
        var handler=level.getCapability(Capabilities.FluidHandler.BLOCK,p,null);
        if(handler==null||handler.fill(fluid,FluidAction.EXECUTE)==0)throw new IllegalStateException("储罐初始化失败："+p);
    }
}
