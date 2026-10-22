package io.github.low2uu.dbv.fleet.door;

import com.maxenonyme.createsubmarine.submarine.compartment.CompartmentDetector;
import com.maxenonyme.createsubmarine.submarine.compartment.CompartmentTracker;
import dev.ryanhcode.sable.companion.SableCompanion;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import net.minecraft.core.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.Level;
import java.util.*;

/** Fresh conservative flood check, never accepts an unloaded or truncated enclosure as sealed. */
public final class DoorSafety {
    private record Room(boolean sealed,boolean wet,boolean controller,Set<BlockPos> cells){}
    public static void requireSafe(Level level,Set<BlockPos> gate,Direction facing){
        var first=gate.iterator().next();var owner=SableCompanion.INSTANCE.getContaining(level,first);
        var rooms=new ArrayList<Room>();var visited=new HashSet<BlockPos>();int[] budget={32768};
        for(var pos:gate)for(var side:Direction.values()){
            var seed=pos.relative(side);
            if(!level.hasChunkAt(seed))throw new IllegalStateException("unloaded");
            if(gate.contains(seed)||visited.contains(seed)||!CompartmentDetector.isPermeable(level.getBlockState(seed)))continue;
            var room=scan(level,seed,gate,first,budget);rooms.add(room);visited.addAll(room.cells());
        }
        if(rooms.isEmpty())throw new IllegalStateException("no_space");
        if(rooms.stream().allMatch(r->r.sealed()&&!r.wet()))return; // Dry internal connection.
        // An exterior opening requires an isolated lock chamber and a separate protected command room.
        if(rooms.stream().anyMatch(Room::controller))throw new IllegalStateException("main_cabin");
        if(!(owner instanceof ServerSubLevel body))throw new IllegalStateException("isolation");
        var id=body.getUniqueId();long age=level.getGameTime()-CompartmentTracker.lastUpdateTick(id);
        if(age<0||age>220||CompartmentTracker.isScanActive(id)||CompartmentTracker.isStructureDirty(id))throw new IllegalStateException("pending");
        boolean protectedRoom=CompartmentTracker.getCompartments(id).stream().anyMatch(r->r.sealed()
            &&!CompartmentTracker.isCompromised(id,r.anchor())&&!CompartmentTracker.isBreached(id,r)
            &&Collections.disjoint(r.internal(),visited)&&Collections.disjoint(r.hull(),gate)
            &&r.internal().stream().anyMatch(p->level.getFluidState(p).isEmpty())
            &&r.hull().stream().anyMatch(p->controller(level,p)));
        if(!protectedRoom)throw new IllegalStateException("isolation");
    }
    private static Room scan(Level level,BlockPos seed,Set<BlockPos> gate,BlockPos origin,int[] budget){
        var queue=new ArrayDeque<BlockPos>();var cells=new HashSet<BlockPos>();queue.add(seed);cells.add(seed);
        boolean sealed=true,wet=false,controller=false;
        while(!queue.isEmpty()){
            var p=queue.remove();
            if(--budget[0]<0)throw new IllegalStateException("scan_limit");
            if(!level.hasChunkAt(p))throw new IllegalStateException("unloaded");
            if(Math.abs(p.getX()-origin.getX())>64||Math.abs(p.getY()-origin.getY())>64||Math.abs(p.getZ()-origin.getZ())>64){sealed=false;continue;}
            wet|=!level.getFluidState(p).isEmpty();
            for(var dir:Direction.values()){
                var n=p.relative(dir);if(gate.contains(n)||cells.contains(n))continue;
                if(!level.hasChunkAt(n))throw new IllegalStateException("unloaded");
                var state=level.getBlockState(n);controller|=controller(level,n);
                if(CompartmentDetector.isPermeable(state)){
                    // Exterior ocean is immediately unsafe to call an enclosed room, avoiding an ocean-wide BFS.
                    if(!level.getFluidState(n).isEmpty()&&SableCompanion.INSTANCE.getContaining(level,n)==null){sealed=false;wet=true;continue;}
                    cells.add(n);queue.add(n);
                }
            }
        }
        return new Room(sealed,wet,controller,cells);
    }
    private static boolean controller(Level l,BlockPos p){var id=BuiltInRegistries.BLOCK.getKey(l.getBlockState(p).getBlock()).toString();return id.equals("dbv_world:jiuhu_console")||id.equals("create_submarine:command_sub");}
    private DoorSafety(){}
}
