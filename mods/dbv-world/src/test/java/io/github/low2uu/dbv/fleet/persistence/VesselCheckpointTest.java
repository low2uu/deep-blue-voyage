package io.github.low2uu.dbv.fleet.persistence;

import static io.github.low2uu.dbv.fleet.fixtures.VesselSnapshotFixtures.*;
import static org.junit.jupiter.api.Assertions.*;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.FileTime;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class VesselCheckpointTest {
    @TempDir Path world;

    @Test void damagePinsThePreviousWholeVesselAcrossFurtherDamageAndRestart() throws Exception {
        UUID vessel=UUID.randomUUID();var store=new VesselDataStore(world,vessel);
        var intact=snapshot(vessel,64,IRON,CHEST,GLASS);store.updateCheckpoint(null,intact);
        var damaged=snapshot(vessel,3,IRON,CHEST);store.updateCheckpoint(intact,damaged);
        var saved=store.recoveryCheckpoint();assertTrue(saved.get("damageDetected").getAsBoolean());
        assertEquals(intact,saved.getAsJsonObject("payload"));
        byte[] protectedImage=Files.readAllBytes(store.directory().resolve("recovery-checkpoint.json"));
        var reopened=new VesselDataStore(world,vessel);
        reopened.updateCheckpoint(damaged,snapshot(vessel,0,IRON));
        reopened.updateCheckpoint(snapshot(vessel,0,IRON),snapshot(vessel,0,IRON,CHEST,GLASS));
        assertArrayEquals(protectedImage,Files.readAllBytes(store.directory().resolve("recovery-checkpoint.json")));
        assertEquals(intact,reopened.recoveryCheckpoint().getAsJsonObject("payload"));
    }

    @Test void crashAfterPublishingDamagedStateCannotReplaceTheCommittedIntactCheckpoint() throws Exception {
        UUID vessel=UUID.randomUUID();var store=new VesselDataStore(world,vessel);
        var intact=snapshot(vessel,64,IRON,CHEST,GLASS);
        var initialModules=new java.util.HashMap<String,JsonObject>();
        for(var entry:intact.getAsJsonObject("modules").entrySet())initialModules.put(entry.getKey(),entry.getValue().getAsJsonObject());
        store.putModules(initialModules);store.checkpoint(store.read(),false);

        // Capture publishes state.json first. Simulate power loss before updateCheckpoint can compare it.
        var damaged=snapshot(vessel,3,IRON,CHEST);var changed=damaged.getAsJsonObject("modules");
        store.putModules(java.util.Map.of("structure",changed.getAsJsonObject("structure"),"storage",changed.getAsJsonObject("storage")));
        assertEquals(damaged,store.read());assertFalse(store.recoveryCheckpoint().get("damageDetected").getAsBoolean());

        // On the next process both the "previous" mirror and a new capture already describe the wreck.
        var reopened=new VesselDataStore(world,vessel);var persistedWreck=reopened.read();
        reopened.updateCheckpoint(persistedWreck,persistedWreck.deepCopy());
        var checkpoint=reopened.recoveryCheckpoint();assertTrue(checkpoint.get("damageDetected").getAsBoolean());
        assertEquals(intact,checkpoint.getAsJsonObject("payload"));
        assertEquals(64,checkpoint.getAsJsonObject("payload").getAsJsonObject("modules").getAsJsonObject("storage")
                .getAsJsonArray("containers").get(0).getAsJsonObject().getAsJsonArray("items").get(0).getAsJsonObject()
                .getAsJsonObject("stack").get("count").getAsInt());
        assertEquals(damaged,reopened.read()); // Pinning recovery data does not rewrite the live mirror.
    }

    @Test void replacedShellAlsoPinsTheLastIntactDevicesAndInventory() throws Exception {
        UUID vessel=UUID.randomUUID();var store=new VesselDataStore(world,vessel);
        var before=snapshot(vessel,47,IRON,CHEST,GLASS);
        var after=snapshot(vessel,2,"minecraft:air",CHEST,GLASS);
        store.updateCheckpoint(before,after);
        assertTrue(store.recoveryCheckpoint().get("damageDetected").getAsBoolean());
        assertEquals(before,store.recoveryCheckpoint().getAsJsonObject("payload"));
    }

    @Test void manualCheckpointAcceptsTheNewDesignAndResumesNormalUpdates() throws Exception {
        UUID vessel=UUID.randomUUID();var store=new VesselDataStore(world,vessel);
        var initial=snapshot(vessel,64,IRON,CHEST,GLASS);var smaller=snapshot(vessel,12,IRON,CHEST);
        store.updateCheckpoint(initial,smaller);assertTrue(store.recoveryCheckpoint().get("damageDetected").getAsBoolean());
        store.checkpoint(smaller,false);
        assertFalse(store.recoveryCheckpoint().get("damageDetected").getAsBoolean());
        assertEquals(smaller,store.recoveryCheckpoint().getAsJsonObject("payload"));
        var extended=snapshot(vessel,11,IRON,CHEST,GLASS,IRON);store.updateCheckpoint(smaller,extended);
        assertEquals(extended,store.recoveryCheckpoint().getAsJsonObject("payload"));
        assertFalse(store.recoveryCheckpoint().get("damageDetected").getAsBoolean());
    }

    @Test void periodicInventoryChangesDoUpdateAnUndamagedCheckpoint() throws Exception {
        UUID vessel=UUID.randomUUID();var store=new VesselDataStore(world,vessel);
        var before=snapshot(vessel,64,IRON,CHEST);var after=snapshot(vessel,17,IRON,CHEST);
        store.updateCheckpoint(null,before);store.updateCheckpoint(before,after);
        assertFalse(store.recoveryCheckpoint().get("damageDetected").getAsBoolean());
        assertEquals(after,store.recoveryCheckpoint().getAsJsonObject("payload"));
    }

    @Test void childOrMovingAssemblyDamagePinsTheWholeImageEvenWhenRootBlocksAreUnchanged() throws Exception {
        for(boolean loseChild:new boolean[]{true,false}){
            UUID vessel=UUID.randomUUID();var store=new VesselDataStore(world,vessel);
            var intact=snapshot(vessel,64,IRON,CHEST);var structure=intact.getAsJsonObject("modules").getAsJsonObject("structure");
            structure.addProperty("schemaVersion",2);structure.addProperty("captureScope","connected-only-v1");
            String rootBody="body/"+UUID.randomUUID(),child="body/"+UUID.randomUUID();
            String moving=rootBody+"/runtime/create_controlled/2, 0, 0";
            var topology=new JsonObject();topology.addProperty(rootBody+"/@body","body");
            topology.addProperty(rootBody+"/block/0, 0, 0",IRON);topology.addProperty(rootBody+"/block/1, 0, 0",CHEST);
            topology.addProperty(child+"/@body","body");topology.addProperty(child+"/block/0, 0, 0",IRON);
            topology.addProperty(moving+"/@assembly","com.simibubi.create.content.contraptions.ControlledContraptionEntity:bearing");
            topology.addProperty(moving+"/block/0, 1, 0","create:sail_frame");
            structure.add("recoveryTopology",topology);store.updateCheckpoint(null,intact);

            // A normal voyage consumes fuel/cargo without changing the ship or bearing-mounted blocks.
            var consumed=intact.deepCopy();var modules=consumed.getAsJsonObject("modules");
            modules.getAsJsonObject("storage").getAsJsonArray("containers").get(0).getAsJsonObject().getAsJsonArray("items")
                    .get(0).getAsJsonObject().getAsJsonObject("stack").addProperty("count",63);
            modules.getAsJsonObject("structure").getAsJsonObject("nativeBlueprint").getAsJsonObject("value")
                    .getAsJsonObject("equipment").getAsJsonObject("value").getAsJsonObject("diesel").addProperty("value","8000");
            store.updateCheckpoint(intact,consumed);
            assertFalse(store.recoveryCheckpoint().get("damageDetected").getAsBoolean());
            assertEquals(consumed,store.recoveryCheckpoint().getAsJsonObject("payload"));

            var damaged=consumed.deepCopy();var damagedTopology=damaged.getAsJsonObject("modules").getAsJsonObject("structure").getAsJsonObject("recoveryTopology");
            if(loseChild){damagedTopology.remove(child+"/@body");damagedTopology.remove(child+"/block/0, 0, 0");}
            else damagedTopology.remove(moving+"/block/0, 1, 0");
            assertEquals(structure.getAsJsonArray("blocks"),damaged.getAsJsonObject("modules").getAsJsonObject("structure").getAsJsonArray("blocks"));
            store.updateCheckpoint(consumed,damaged);
            assertTrue(store.recoveryCheckpoint().get("damageDetected").getAsBoolean());
            assertEquals(consumed,store.recoveryCheckpoint().getAsJsonObject("payload"));
            byte[] pinned=Files.readAllBytes(store.directory().resolve("recovery-checkpoint.json"));
            // A later capture, even with some repaired blocks, cannot silently accept the damaged design.
            var reopened=new VesselDataStore(world,vessel);reopened.updateCheckpoint(damaged,intact);
            assertArrayEquals(pinned,Files.readAllBytes(store.directory().resolve("recovery-checkpoint.json")));
        }
    }

    @Test void automaticRotationCannotDeletePinnedOrManualWholeVesselCopies() throws Exception {
        UUID vessel=UUID.randomUUID();var store=new VesselDataStore(world,vessel);
        var intact=snapshot(vessel,64,IRON,CHEST,GLASS);store.checkpoint(intact,true);store.replaceFromRecovery(intact);
        UUID manual=store.backup();byte[] pinned=Files.readAllBytes(store.directory().resolve("recovery-checkpoint.json"));
        UUID oldest=null,latest=null;
        for(int i=0;i<25;i++){
            store.replaceFromRecovery(snapshot(vessel,i,IRON,CHEST));latest=store.backupAutomatic();if(i==0)oldest=latest;
            // Establish a strict order without sleeps or relying on filesystem timestamp resolution.
            Files.setLastModifiedTime(store.directory().resolve("automatic").resolve(latest+".json"),FileTime.fromMillis(1000L+i));
        }
        try(var files=Files.list(store.directory().resolve("automatic"))){assertEquals(20,files.count());}
        assertFalse(Files.exists(store.directory().resolve("automatic").resolve(oldest+".json")));
        assertEquals(snapshot(vessel,24,IRON,CHEST),store.readBackup(latest));
        assertEquals(intact,store.readBackup(manual));assertArrayEquals(pinned,Files.readAllBytes(store.directory().resolve("recovery-checkpoint.json")));
    }

    @Test void damagedChecksumForeignIdentityAndIncompleteImagesAreRejected() throws Exception {
        UUID vessel=UUID.randomUUID();var store=new VesselDataStore(world,vessel);var intact=snapshot(vessel,64,IRON,CHEST);
        store.checkpoint(intact,false);Path file=store.directory().resolve("recovery-checkpoint.json");
        String original=Files.readString(file);var envelope=JsonParser.parseString(original).getAsJsonObject();
        envelope.getAsJsonObject("payload").getAsJsonObject("modules").getAsJsonObject("captain").addProperty("questStep",999);
        Files.writeString(file,envelope.toString());assertThrows(IOException.class,store::recoveryCheckpoint);
        assertThrows(IOException.class,()->store.updateCheckpoint(intact,snapshot(vessel,7,IRON,CHEST)));
        assertEquals(envelope,JsonParser.parseString(Files.readString(file)));
        Files.writeString(file,original);
        var other=new VesselDataStore(world,UUID.randomUUID());Files.createDirectories(other.directory());
        Files.copy(file,other.directory().resolve("recovery-checkpoint.json"));assertThrows(IOException.class,other::recoveryCheckpoint);
        assertThrows(IOException.class,()->other.checkpoint(intact,false));assertThrows(IOException.class,()->other.replaceFromRecovery(intact));
        var incomplete=intact.deepCopy();incomplete.getAsJsonObject("modules").getAsJsonObject("structure").remove("nativeBlueprint");
        assertThrows(IOException.class,()->store.checkpoint(incomplete,false));assertEquals(original,Files.readString(file));
    }

    @Test void interruptedCheckpointPublicationPreservesTheLastImageAndPendingEvidence() throws Exception {
        UUID vessel=UUID.randomUUID();var store=new VesselDataStore(world,vessel);var intact=snapshot(vessel,64,IRON,CHEST);
        store.checkpoint(intact,false);Path file=store.directory().resolve("recovery-checkpoint.json");byte[] original=Files.readAllBytes(file);
        Path pending=store.directory().resolve("recovery-checkpoint.json.pending");Files.writeString(pending,"{\"partial\":");
        var reopened=new VesselDataStore(world,vessel);
        assertEquals(intact,reopened.recoveryCheckpoint().getAsJsonObject("payload"));
        assertThrows(IOException.class,()->reopened.checkpoint(snapshot(vessel,0,IRON),false));
        assertThrows(IOException.class,()->reopened.updateCheckpoint(intact,snapshot(vessel,0,IRON)));
        assertArrayEquals(original,Files.readAllBytes(file));assertEquals("{\"partial\":",Files.readString(pending));
    }

    @Test void unreadableCurrentMirrorRetainsRawEvidenceAndBuildsAValidRecoveryBeforeImage() throws Exception {
        for(boolean pendingWrite:new boolean[]{false,true}){
            UUID vessel=UUID.randomUUID();var store=new VesselDataStore(world,vessel);var intact=snapshot(vessel,64,IRON,CHEST);
            store.replaceFromRecovery(intact);store.checkpoint(intact,false);
            var evidence=new java.util.LinkedHashMap<String,String>();
            if(pendingWrite)evidence.put("state.json.pending","{\"modules\":");
            else evidence.put("state.json","{corrupt published state");
            evidence.put("storage-recovery.json","{\"source\":\"retained-old-recovery\"}");
            evidence.put("storage-recovery.json.pending","partial inventory recovery write");
            for(var entry:evidence.entrySet())Files.writeString(store.directory().resolve(entry.getKey()),entry.getValue());
            evidence.putIfAbsent("state.json",Files.readString(store.directory().resolve("state.json")));

            var reopened=new VesselDataStore(world,vessel);assertThrows(IOException.class,reopened::read);
            var source=reopened.recoveryCheckpoint().getAsJsonObject("payload");var before=reopened.recoveryBefore(source);
            assertEquals(vessel.toString(),before.get("vesselId").getAsString());
            assertEquals(1,before.get("schemaVersion").getAsInt());
            assertEquals(source.getAsJsonObject("modules").get("storage"),before.getAsJsonObject("modules").get("storage"));
            var marker=before.getAsJsonObject("modules").getAsJsonObject("recoveryEvidence");
            assertTrue(marker.get("unreadableCurrentState").getAsBoolean());
            Path copied=store.directory().resolve(marker.get("evidence").getAsString());
            for(var entry:evidence.entrySet()){
                assertEquals(entry.getValue(),Files.readString(copied.resolve(entry.getKey())));
                assertEquals(entry.getValue(),Files.readString(store.directory().resolve(entry.getKey())));
            }
            assertFalse(source.getAsJsonObject("modules").has("recoveryEvidence"));
            var transaction=recoveryTransaction(vessel,UUID.randomUUID(),UUID.randomUUID(),"PREPARED");
            transaction.add("source",source);transaction.add("before",before);
            var journal=new io.github.low2uu.dbv.fleet.recovery.RecoveryJournal(store.directory());journal.write(transaction);
            assertEquals(before,new io.github.low2uu.dbv.fleet.recovery.RecoveryJournal(store.directory()).read().getAsJsonObject("before"));
        }
    }

    @Test void wholeVesselReplacementArchivesSupersededInventoryTransactionsAndResumesCaptureWrites() throws Exception {
        UUID vessel=UUID.randomUUID();var store=new VesselDataStore(world,vessel);var intact=snapshot(vessel,64,IRON,CHEST);
        store.replaceFromRecovery(intact);store.checkpoint(intact,false);
        store.beginStorageRecovery(UUID.randomUUID(),UUID.randomUUID());
        var retained=new java.util.LinkedHashMap<String,String>();
        retained.put("superseded-storage-recovery.json",Files.readString(store.directory().resolve("storage-recovery.json")));
        for(String name:new String[]{"state.json","recovery-checkpoint.json","storage-recovery.json"}){
            String content="interrupted "+name;Files.writeString(store.directory().resolve(name+".pending"),content);retained.put(name+".pending",content);
        }
        assertThrows(IOException.class,store::requireStorageReady);
        var reopened=new VesselDataStore(world,vessel);reopened.replaceFromRecovery(intact);
        assertDoesNotThrow(reopened::requireStorageReady);assertEquals(intact,reopened.read());
        assertFalse(Files.exists(store.directory().resolve("storage-recovery.json")));
        for(String name:new String[]{"state.json","recovery-checkpoint.json","storage-recovery.json"})
            assertFalse(Files.exists(store.directory().resolve(name+".pending")));
        try(var paths=Files.list(store.directory().resolve("recovery-history"))){
            var archived=paths.toList();assertEquals(retained.size(),archived.size());
            for(var entry:retained.entrySet()){
                var matching=archived.stream().filter(p->p.getFileName().toString().endsWith("-"+entry.getKey())).toList();
                assertEquals(1,matching.size(),entry.getKey());assertEquals(entry.getValue(),Files.readString(matching.getFirst()));
            }
        }
        // The next periodic capture can publish a changed container and update the restored recovery point.
        var next=snapshot(vessel,63,IRON,CHEST);
        reopened.putModule("storage",next.getAsJsonObject("modules").getAsJsonObject("storage"));
        reopened.updateCheckpoint(intact,reopened.read());
        assertEquals(next,new VesselDataStore(world,vessel).read());
        assertEquals(next,reopened.recoveryCheckpoint().getAsJsonObject("payload"));
    }

    @Test void completelyLostOrUnavailableStructureDoesNotOverwriteTheLastCheckpoint() throws Exception {
        UUID vessel=UUID.randomUUID();var store=new VesselDataStore(world,vessel);var intact=snapshot(vessel,64,IRON,CHEST);
        store.checkpoint(intact,false);byte[] original=Files.readAllBytes(store.directory().resolve("recovery-checkpoint.json"));
        store.updateCheckpoint(intact,snapshot(vessel,0));
        var partial=intact.deepCopy();partial.getAsJsonObject("modules").remove("storage");store.updateCheckpoint(intact,partial);
        assertEquals(intact,store.recoveryCheckpoint().getAsJsonObject("payload"));
        // A few blocks later appearing in the destroyed carrier must not be accepted as a whole new design.
        store.updateCheckpoint(snapshot(vessel,0),snapshot(vessel,0,IRON));
        assertEquals(intact,store.recoveryCheckpoint().getAsJsonObject("payload"));
    }
}
