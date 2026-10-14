package io.github.low2uu.dbv.fleet;

import com.google.gson.JsonObject;
import io.github.low2uu.dbv.fleet.persistence.VesselDataStore;
import java.nio.file.*;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;

class VesselDataStoreTest {
    @TempDir Path world;
    private JsonObject data(int count){var o=new JsonObject();o.addProperty("count",count);return o;}
    @Test void backupRestoreSurvivesReloadAndPreservesOtherModules() throws Exception {
        var id=UUID.randomUUID();var store=new VesselDataStore(world,id);
        store.putModule("storage",data(17));store.putModule("captain",data(3));var backup=store.backup();
        store.putModule("storage",data(0));store=new VesselDataStore(world,id);store.restore(backup);
        assertEquals(17,store.read().getAsJsonObject("modules").getAsJsonObject("storage").get("count").getAsInt());
        assertEquals(3,store.read().getAsJsonObject("modules").getAsJsonObject("captain").get("count").getAsInt());
    }
    @Test void corruptionAndInterruptedWritesNeverBecomeEmptyData() throws Exception {
        var store=new VesselDataStore(world,UUID.randomUUID());store.putModule("storage",data(17));var backup=store.backup();
        Path saved=store.directory().resolve("backups").resolve(backup+".json");
        Files.writeString(saved,Files.readString(saved).replace("17","18"));assertThrows(java.io.IOException.class,()->store.restore(backup));
        assertEquals(17,store.read().getAsJsonObject("modules").getAsJsonObject("storage").get("count").getAsInt());
        Files.writeString(store.directory().resolve("state.json.pending"),"interrupted");
        assertThrows(java.io.IOException.class,store::read);assertThrows(java.io.IOException.class,()->store.putModule("storage",data(0)));
    }
    @Test void backupFromAnotherVesselIsRejected() throws Exception {
        var a=new VesselDataStore(world,UUID.randomUUID());a.putModule("storage",data(17));var id=a.backup();
        var b=new VesselDataStore(world,UUID.randomUUID());Files.createDirectories(b.directory().resolve("backups"));
        Files.copy(a.directory().resolve("backups").resolve(id+".json"),b.directory().resolve("backups").resolve(id+".json"));
        assertThrows(java.io.IOException.class,()->b.restore(id));
    }
    @Test void interruptedInventoryRestoreRetainsBothSnapshotReferences() throws Exception {
        var store=new VesselDataStore(world,UUID.randomUUID());var source=store.backup();var before=store.backup();
        store.beginStorageRecovery(source,before);
        assertThrows(java.io.IOException.class,store::requireStorageReady);
        var reloaded=new VesselDataStore(world,UUID.fromString(store.read().get("vesselId").getAsString()));
        assertThrows(java.io.IOException.class,reloaded::requireStorageReady);
        assertNotNull(reloaded.readBackup(source));assertNotNull(reloaded.readBackup(before));
        reloaded.finishStorageRecovery();assertDoesNotThrow(reloaded::requireStorageReady);
    }
    @Test void automaticHistoryIsBoundedAndNeverDeletesManualBackups() throws Exception {
        var store=new VesselDataStore(world,UUID.randomUUID());store.putModules(java.util.Map.of("structure",data(1),"storage",data(17)));var manual=store.backup();
        UUID latest=null;for(int i=0;i<23;i++){store.putModule("structure",data(i));latest=store.backupAutomatic();}
        try(var files=Files.list(store.directory().resolve("automatic"))){assertEquals(20,files.count());}
        assertNotNull(store.readBackup(manual));
        assertEquals(22,store.readBackup(latest).getAsJsonObject("modules").getAsJsonObject("structure").get("count").getAsInt());
        assertEquals(17,store.read().getAsJsonObject("modules").getAsJsonObject("storage").get("count").getAsInt());
    }
}
