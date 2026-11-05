package io.github.low2uu.dbv.fleet.door;

import com.maxenonyme.createsubmarine.submarine.compartment.CompartmentTracker;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import java.util.Set;

/** User-requested pre-opening information only; does not impose an extra opening policy. */
public final class DoorSafety {
    public static Component report(Level level,Set<BlockPos> gate){
        var body=new DoorBlockView(level,gate.iterator().next()).body;
        if(body==null)return Component.translatable("message.dbv_world.door.check_unavailable");
        var id=body.getUniqueId();long age=level.getGameTime()-CompartmentTracker.lastUpdateTick(id);
        if(age<0||age>220||CompartmentTracker.isScanActive(id)||CompartmentTracker.isStructureDirty(id))
            return Component.translatable("message.dbv_world.door.check_pending");
        var rooms=CompartmentTracker.getCompartments(id).stream()
            .filter(r->gate.stream().anyMatch(p->r.hull().contains(p)||r.internal().contains(p))).toList();
        boolean sealed=!rooms.isEmpty()&&rooms.stream().allMatch(r->r.sealed()
            &&!CompartmentTracker.isCompromised(id,r.anchor())&&!CompartmentTracker.isBreached(id,r));
        return Component.translatable(sealed?"message.dbv_world.door.check_sealed":"message.dbv_world.door.check_unsealed");
    }
    private DoorSafety(){}
}
