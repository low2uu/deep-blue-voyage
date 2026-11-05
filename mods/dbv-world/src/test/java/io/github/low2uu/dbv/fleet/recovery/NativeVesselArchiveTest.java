package io.github.low2uu.dbv.fleet.recovery;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class NativeVesselArchiveTest {
    private static CompoundTag equipment(String id) { var t = new CompoundTag(); t.putString("id", id); return t; }
    private static CompoundTag item(int count) {
        var t = new CompoundTag(); t.putString("id", "minecraft:diamond"); t.putInt("count", count);
        var data = new CompoundTag(); data.putUUID("UUID", new java.util.UUID(17, 29)); data.putString("Pos", "keep item custom data");
        t.put("components", data); return t;
    }
    private static CompoundTag fluid(int amount) {
        var f = new CompoundTag(); f.putString("id", "minecraft:water"); f.putInt("amount", amount);
        var tank = new CompoundTag(); tank.put("Fluid", f); return tank;
    }

    @Test void itemContentsAndCustomDataRemainExactDuringNormalization() {
        var source = equipment("minecraft:chest"); var list = new ListTag(); list.add(item(23)); source.put("Items", list);
        var normalized = NativeVesselArchive.normalizeEquipment(source, BlockPos.ZERO);
        assertEquals(source.get("Items"), normalized.get("Items"));
        var changed = source.copy(); changed.getList("Items", Tag.TAG_COMPOUND).getCompound(0).putInt("count", 22);
        assertNotEquals(normalized, NativeVesselArchive.normalizeEquipment(changed, BlockPos.ZERO));
    }

    @Test void relocatingTankControllerDoesNotChangeFluidAmounts() {
        var a = equipment("create:fluid_tank"); a.putIntArray("Controller", new int[]{20000101, 31, 20000203});
        a.put("TankContent", fluid(7000));
        var b = a.copy(); b.putIntArray("Controller", new int[]{22000101, 31, 24000203});
        assertEquals(NativeVesselArchive.normalizeEquipment(a, new BlockPos(20000000, 0, 20000000)),
                NativeVesselArchive.normalizeEquipment(b, new BlockPos(22000000, 0, 24000000)));
        b.getCompound("TankContent").getCompound("Fluid").putInt("amount", 6999);
        assertNotEquals(NativeVesselArchive.normalizeEquipment(a, new BlockPos(20000000, 0, 20000000)),
                NativeVesselArchive.normalizeEquipment(b, new BlockPos(22000000, 0, 24000000)));
    }

    @Test void controllerTelemetryIsDerivedButTargetDepthAndAutopilotAreSaved() {
        var a = equipment("create_submarine:command_sub"); a.putInt("TargetDepth", 13); a.putInt("Depth", 30); a.putBoolean("Autopilot", true);
        var b = a.copy(); b.putInt("Depth", 1);
        assertEquals(NativeVesselArchive.normalizeEquipment(a, BlockPos.ZERO), NativeVesselArchive.normalizeEquipment(b, BlockPos.ZERO));
        b.putInt("TargetDepth", 1);
        assertNotEquals(NativeVesselArchive.normalizeEquipment(a, BlockPos.ZERO), NativeVesselArchive.normalizeEquipment(b, BlockPos.ZERO));
    }

    @Test void resourceReapplicationKeepsRemappedLinksAndKinetics() {
        var source = equipment("create_submarine:electrolyzer"); source.put("WaterTank", fluid(3000)); source.putInt("Energy", 7500);
        var target = source.copy(); target.put("WaterTank", fluid(2999)); target.putInt("Energy", 7400);
        target.putLong("Network", 915); target.putIntArray("Controller", new int[]{22000001, 30, 24000003});
        NativeVesselArchive.copyResourceFields(source, target);
        assertEquals(source.get("WaterTank"), target.get("WaterTank")); assertEquals(7500, target.getInt("Energy"));
        assertEquals(915, target.getLong("Network")); assertArrayEquals(new int[]{22000001, 30, 24000003}, target.getIntArray("Controller"));
    }

    @Test void anEmptyInventoryReplacesUnexpectedCandidateItems() {
        var source = equipment("minecraft:barrel"); source.put("Items", new ListTag());
        var target = source.copy(); var list = new ListTag(); list.add(item(64)); target.put("Items", list);
        NativeVesselArchive.copyResourceFields(source, target);
        assertTrue(target.getList("Items", Tag.TAG_COMPOUND).isEmpty());
    }

    @Test void unknownDeviceSettingsAreNotSilentlyDiscarded() {
        var source = equipment("thirdparty:machine"); source.putString("Mode", "A"); source.putUUID("Owner", new java.util.UUID(1, 2));
        var changed = source.copy(); changed.putString("Mode", "B");
        assertNotEquals(NativeVesselArchive.normalizeEquipment(source, BlockPos.ZERO), NativeVesselArchive.normalizeEquipment(changed, BlockPos.ZERO));
        assertTrue(NativeVesselArchive.normalizeEquipment(source, BlockPos.ZERO).hasUUID("Owner"));
    }

    @Test void movingAssemblyAnchorRelocatesWhileItsInventoryRemainsChecked() {
        var a = new CompoundTag(); a.putIntArray("Anchor", new int[]{20000101, 32, 20000203});
        var list = new ListTag(); list.add(item(12)); a.put("Items", list);
        var b = a.copy(); b.putIntArray("Anchor", new int[]{22000101, 32, 24000203}); b.putBoolean("Stalled", true);
        var x = NativeVesselArchive.normalizeContraption(a, new BlockPos(20000000, 0, 20000000));
        assertEquals(x, NativeVesselArchive.normalizeContraption(b, new BlockPos(22000000, 0, 24000000)));
        b.getList("Items", Tag.TAG_COMPOUND).getCompound(0).putInt("count", 11);
        assertNotEquals(x, NativeVesselArchive.normalizeContraption(b, new BlockPos(22000000, 0, 24000000)));
    }

    @Test void stagingIdenticalChildrenKeepsDistinctIdentityAndDoesNotMutateTheArchive() {
        var archive = new CompoundTag(); var bodies = new ListTag();
        for (int i = 0; i < 2; i++) {
            var body = new CompoundTag(); body.putUUID("sublevel_id", new java.util.UUID(9, i));
            body.putString("name", "相同外形的侧舱"); body.putBoolean("AstDisableStructureCollision", false);
            bodies.add(body);
        }
        archive.put("sublevels", bodies);
        var staged = NativeVesselArchive.stageArchive(archive, "receipt/").getList("sublevels", Tag.TAG_COMPOUND);
        assertNotEquals(staged.getCompound(0).getString("name"), staged.getCompound(1).getString("name"));
        assertTrue(staged.getCompound(0).getBoolean("AstDisableStructureCollision"));
        assertEquals("相同外形的侧舱", bodies.getCompound(0).getString("name"));
        assertFalse(bodies.getCompound(0).getBoolean("AstDisableStructureCollision"));
    }

    @Test void movingBlockDamageBaselineUsesPositionsAndMaterialNotPaletteOrder() throws Exception {
        var contraption = new CompoundTag(); var blocks = new CompoundTag(); var palette = new ListTag();
        var iron = new CompoundTag(); iron.putString("Name", "minecraft:iron_block"); palette.add(iron);
        var glass = new CompoundTag(); glass.putString("Name", "minecraft:glass"); palette.add(glass);
        var list = new ListTag();
        var a = new CompoundTag(); a.putLong("Pos", new BlockPos(1, 2, 3).asLong()); a.putInt("State", 0); list.add(a);
        var b = new CompoundTag(); b.putLong("Pos", new BlockPos(4, 2, 3).asLong()); b.putInt("State", 1); list.add(b);
        blocks.put("Palette", palette); blocks.put("BlockList", list); contraption.put("Blocks", blocks);
        var before = new com.google.gson.JsonObject(); NativeVesselArchive.appendContraptionTopology(before, "assembly", contraption);
        var reversed = new ListTag(); reversed.add(glass.copy()); reversed.add(iron.copy()); blocks.put("Palette", reversed);
        a.putInt("State", 1); b.putInt("State", 0);
        var after = new com.google.gson.JsonObject(); NativeVesselArchive.appendContraptionTopology(after, "assembly", contraption);
        assertEquals(before, after);
        list.remove(1); after = new com.google.gson.JsonObject(); NativeVesselArchive.appendContraptionTopology(after, "assembly", contraption);
        assertEquals(1, after.size()); assertFalse(after.has("assembly/block/4, 2, 3"));
    }

    @Test void legacyMultiHullProximitySnapshotsAreNotTrustedAsOwnershipEvidence() throws Exception {
        var structure = new com.google.gson.JsonObject();
        NativeVesselArchive.validateCaptureScope(structure, 1);
        assertThrows(java.io.IOException.class, () -> NativeVesselArchive.validateCaptureScope(structure, 2));
        structure.addProperty("captureScope", "connected-only-v1");
        NativeVesselArchive.validateCaptureScope(structure, 2);
    }

    @Test void energyRestorationBypassesTransferRatesAndPreservesExactSavedAmount() {
        var energy = new net.neoforged.neoforge.energy.EnergyStorage(10000, 1000, 1000);
        assertEquals(1000, energy.receiveEnergy(1234, false));
        var saved = new CompoundTag(); saved.putInt("Energy", 1234);
        NativeVesselArchive.restoreExactEnergy(energy, null, saved);
        assertEquals(1234, energy.getEnergyStored());
        saved.putInt("Energy", 10000); NativeVesselArchive.restoreExactEnergy(energy, null, saved);
        assertEquals(10000, energy.getEnergyStored());
        saved.putInt("Energy", 0); NativeVesselArchive.restoreExactEnergy(energy, null, saved);
        assertEquals(0, energy.getEnergyStored());
    }

    @Test void velocityMeasurementsAreDerivedButSensorConfigurationRemainsExact() {
        var a = equipment("simulated:velocity_sensor"); a.putFloat("AdjustedVelocity", 0); a.putInt("SignedRedstoneStrength", 0); a.putInt("MaxSpeed", 20);
        var b = a.copy(); b.putFloat("AdjustedVelocity", .07f); b.putInt("SignedRedstoneStrength", 1);
        assertEquals(NativeVesselArchive.normalizeEquipment(a, BlockPos.ZERO), NativeVesselArchive.normalizeEquipment(b, BlockPos.ZERO));
        b.putInt("MaxSpeed", 21);
        assertNotEquals(NativeVesselArchive.normalizeEquipment(a, BlockPos.ZERO), NativeVesselArchive.normalizeEquipment(b, BlockPos.ZERO));
    }
}
