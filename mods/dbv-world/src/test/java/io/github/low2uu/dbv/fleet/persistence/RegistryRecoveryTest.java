package io.github.low2uu.dbv.fleet.persistence;

import static io.github.low2uu.dbv.fleet.fixtures.VesselSnapshotFixtures.*;
import static org.junit.jupiter.api.Assertions.*;
import com.google.gson.*;
import io.github.low2uu.dbv.fleet.domain.FlagshipRegistry;
import io.github.low2uu.dbv.fleet.domain.VesselIdentity;
import io.github.low2uu.dbv.fleet.recovery.RecoveryJournal;
import java.io.IOException;
import java.nio.file.*;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class RegistryRecoveryTest {
    @TempDir Path world;
    private Path registry(){return world.resolve("dbv/fleet/registry.json");}
    private Path pending(){return world.resolve("dbv/fleet/registry.refit.pending.json");}
    private Path vesselDirectory(UUID id){return world.resolve("dbv/fleet/vessels").resolve(id.toString());}

    @Test void completePendingRebindCanCommitOnlyTheVerifiedCarrierAfterPowerLoss() throws Exception {
        var store=new RegistryStore(world);var old=create(store);UUID candidate=UUID.randomUUID();
        new RecoveryJournal(vesselDirectory(old.vesselId())).write(recoveryTransaction(old.vesselId(),old.bodyId(),candidate,"VERIFIED"));
        Files.writeString(pending(),replacement(candidate).toString());
        assertThrows(IOException.class,()->new RegistryStore(world).load());
        var reopened=new RegistryStore(world);reopened.recoverWholeVesselRebind();var restored=reopened.load().flagship().orElseThrow();
        assertEquals(candidate,restored.bodyId());assertEquals(old.vesselId(),restored.vesselId());
        assertEquals(old.name(),restored.name());assertEquals(old.dimension(),restored.dimension());assertEquals(old.generation()+1,restored.generation());
        assertFalse(Files.exists(pending()));reopened.recoverWholeVesselRebind();assertEquals(restored,reopened.load().flagship().orElseThrow());
        assertEquals("VERIFIED",new RecoveryJournal(vesselDirectory(old.vesselId())).read().get("phase").getAsString());
    }

    @Test void partialPendingRebindReturnsToOriginalIdentityAndArchivesTheEvidence() throws Exception {
        var store=new RegistryStore(world);var old=create(store);byte[] original=Files.readAllBytes(registry());
        new RecoveryJournal(vesselDirectory(old.vesselId())).write(recoveryTransaction(old.vesselId(),old.bodyId(),UUID.randomUUID(),"VERIFIED"));
        String partial="{\"schemaVersion\":1,\"flagship\":";Files.writeString(pending(),partial);
        var reopened=new RegistryStore(world);reopened.recoverWholeVesselRebind();
        assertEquals(old,reopened.load().flagship().orElseThrow());assertArrayEquals(original,Files.readAllBytes(registry()));assertFalse(Files.exists(pending()));
        try(var files=Files.list(vesselDirectory(old.vesselId()).resolve("recovery-history"))){
            var history=files.toList();assertEquals(1,history.size());assertEquals(partial,Files.readString(history.getFirst()));
        }
    }

    @Test void pendingRegistryCannotChangeCampaignIdentityNameOrGeneration() throws Exception {
        var store=new RegistryStore(world);var old=create(store);UUID candidate=UUID.randomUUID();byte[] original=Files.readAllBytes(registry());
        new RecoveryJournal(vesselDirectory(old.vesselId())).write(recoveryTransaction(old.vesselId(),old.bodyId(),candidate,"VERIFIED"));
        for(String field:new String[]{"vesselId","name","generation"}){
            var invalid=replacement(candidate);var ship=invalid.getAsJsonObject("flagship");
            if(field.equals("generation"))ship.addProperty(field,old.generation()+2);
            else ship.addProperty(field,field.equals("vesselId")?UUID.randomUUID().toString():"另一艘潜艇");
            Files.writeString(pending(),invalid.toString());new RegistryStore(world).recoverWholeVesselRebind();
            assertArrayEquals(original,Files.readAllBytes(registry()));assertEquals(old,new RegistryStore(world).load().flagship().orElseThrow());
        }
    }

    @Test void unverifiedOrForeignTransactionCannotAuthorizeARegistryReplacement() throws Exception {
        var store=new RegistryStore(world);var old=create(store);UUID candidate=UUID.randomUUID();
        var journal=new RecoveryJournal(vesselDirectory(old.vesselId()));
        journal.write(recoveryTransaction(old.vesselId(),old.bodyId(),candidate,"PLACED"));
        String replacement=replacement(candidate).toString();Files.writeString(pending(),replacement);
        new RegistryStore(world).recoverWholeVesselRebind();assertEquals(replacement,Files.readString(pending()));assertThrows(IOException.class,store::load);
        journal.write(recoveryTransaction(UUID.randomUUID(),old.bodyId(),candidate,"VERIFIED"));
        new RegistryStore(world).recoverWholeVesselRebind();assertEquals(replacement,Files.readString(pending()));assertThrows(IOException.class,store::load);
        journal.write(recoveryTransaction(old.vesselId(),UUID.randomUUID(),candidate,"VERIFIED"));
        new RegistryStore(world).recoverWholeVesselRebind();assertEquals(replacement,Files.readString(pending()));assertThrows(IOException.class,store::load);
    }

    @Test void pendingCommitMustNotBeSettledWhenRecoveryJournalChecksumIsBroken() throws Exception {
        var store=new RegistryStore(world);var old=create(store);UUID candidate=UUID.randomUUID();
        var journal=new RecoveryJournal(vesselDirectory(old.vesselId()));journal.write(recoveryTransaction(old.vesselId(),old.bodyId(),candidate,"VERIFIED"));
        Path file=vesselDirectory(old.vesselId()).resolve("recovery-transaction.json");
        var envelope=JsonParser.parseString(Files.readString(file)).getAsJsonObject();envelope.addProperty("sha256","wrong");Files.writeString(file,envelope.toString());
        String replacement=replacement(candidate).toString();Files.writeString(pending(),replacement);byte[] original=Files.readAllBytes(registry());
        assertThrows(IOException.class,()->new RegistryStore(world).recoverWholeVesselRebind());
        assertArrayEquals(original,Files.readAllBytes(registry()));assertEquals(replacement,Files.readString(pending()));
    }

    private VesselIdentity create(RegistryStore store) throws IOException {
        var registry=FlagshipRegistry.empty().register(UUID.randomUUID(),"minecraft:overworld","酒狐号·主潜艇");store.create(registry);return registry.flagship().orElseThrow();
    }
    private JsonObject replacement(UUID body) throws IOException {
        var root=JsonParser.parseString(Files.readString(registry())).getAsJsonObject();var ship=root.getAsJsonObject("flagship");
        ship.addProperty("bodyId",body.toString());ship.addProperty("generation",ship.get("generation").getAsLong()+1);return root;
    }
}
