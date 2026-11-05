package io.github.low2uu.dbv.fleet.recovery;

import static io.github.low2uu.dbv.fleet.fixtures.VesselSnapshotFixtures.*;
import static org.junit.jupiter.api.Assertions.*;
import com.google.gson.*;
import io.github.low2uu.dbv.fleet.persistence.VesselDataStore;
import java.io.IOException;
import java.nio.file.*;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class RecoveryJournalTest {
    @TempDir Path directory;
    private Path file(){return directory.resolve("recovery-transaction.json");}
    private Path pending(){return directory.resolve("recovery-transaction.json.pending");}

    @Test void everyDurableStageRetainsTheSameSourceAndCarrierDecisionsAfterRestart() throws Exception {
        var transaction=transaction();var original=transaction.getAsJsonObject("source").deepCopy();
        for(String phase:new String[]{"PREPARED","PLACED","VERIFIED","COMMITTED"}){
            transaction.addProperty("phase",phase);new RecoveryJournal(directory).write(transaction);
            var reopened=new RecoveryJournal(directory).read();
            assertEquals(transaction,reopened);assertEquals(original,reopened.getAsJsonObject("source"));
            assertEquals(transaction.get("candidate"),reopened.get("candidate"));assertFalse(Files.exists(pending()));
        }
    }

    @Test void firstPreparedWriteSurvivesCrashAfterFsyncBeforeRename() throws Exception {
        var prepared=transaction();new RecoveryJournal(directory).write(prepared);
        Files.move(file(),pending());
        assertEquals(prepared,new RecoveryJournal(directory).read());assertTrue(Files.exists(file()));assertFalse(Files.exists(pending()));
    }

    @Test void failedPreparedImportCanRecordCleanupBodiesBeforeTheRootIsKnown() throws Exception {
        var partial=transaction();partial.remove("candidate");
        new RecoveryJournal(directory).write(partial);
        assertEquals(partial,new RecoveryJournal(directory).read());
        assertFalse(new RecoveryJournal(directory).read().getAsJsonArray("candidates").isEmpty());
    }

    @Test void partialFirstWriteIsPreservedAndNeverBecomesAnEmptyTransaction() throws Exception {
        Files.writeString(pending(),"{\"schemaVersion\":1,\"payload\":");
        assertThrows(IOException.class,()->new RecoveryJournal(directory).read());
        assertFalse(Files.exists(file()));assertEquals("{\"schemaVersion\":1,\"payload\":",Files.readString(pending()));
    }

    @Test void partialOrCompletedUnpublishedReplacementKeepsTheCommittedDecision() throws Exception {
        var prepared=transaction();var journal=new RecoveryJournal(directory);journal.write(prepared);byte[] committed=Files.readAllBytes(file());
        Files.writeString(pending(),"partial COMMITTED replacement");
        assertEquals(prepared,new RecoveryJournal(directory).read());assertArrayEquals(committed,Files.readAllBytes(file()));
        var committedCandidate=prepared.deepCopy();committedCandidate.addProperty("phase","COMMITTED");
        Path scratch=directory.resolve("other");new RecoveryJournal(scratch).write(committedCandidate);
        Files.copy(scratch.resolve("recovery-transaction.json"),pending());
        assertEquals(prepared,new RecoveryJournal(directory).read());assertFalse(Files.exists(pending()));
    }

    @Test void corruptCommittedDecisionDoesNotPromoteOrDeletePendingEvidence() throws Exception {
        var transaction=transaction();new RecoveryJournal(directory).write(transaction);
        byte[] valid=Files.readAllBytes(file());Files.write(pending(),valid);Files.writeString(file(),"{broken committed decision");
        assertThrows(IOException.class,()->new RecoveryJournal(directory).read());
        assertEquals("{broken committed decision",Files.readString(file()));assertArrayEquals(valid,Files.readAllBytes(pending()));
    }

    @Test void checksumOrUnknownSchemaAndPhaseCannotAdvanceRecovery() throws Exception {
        var transaction=transaction();var journal=new RecoveryJournal(directory);journal.write(transaction);
        var envelope=JsonParser.parseString(Files.readString(file())).getAsJsonObject();
        envelope.getAsJsonObject("payload").addProperty("phase","COMMITTED");Files.writeString(file(),envelope.toString());
        assertThrows(IOException.class,()->new RecoveryJournal(directory).read());
        transaction.addProperty("phase","TELEPORTED");assertThrows(IOException.class,()->journal.write(transaction));
        transaction.addProperty("phase","PREPARED");journal.write(transaction);
        envelope=JsonParser.parseString(Files.readString(file())).getAsJsonObject();envelope.addProperty("schemaVersion",2);
        Files.writeString(file(),envelope.toString());assertThrows(IOException.class,()->new RecoveryJournal(directory).read());
    }

    @Test void foreignVesselSourceAndUnidentifiedCandidateAreRejectedBeforePublication() throws Exception {
        var transaction=transaction();var journal=new RecoveryJournal(directory);
        transaction.getAsJsonObject("source").addProperty("vesselId",UUID.randomUUID().toString());
        assertThrows(IOException.class,()->journal.write(transaction));assertFalse(Files.exists(file()));
        var noCandidate=transaction();noCandidate.addProperty("phase","VERIFIED");noCandidate.remove("candidate");
        assertThrows(IOException.class,()->journal.write(noCandidate));assertFalse(Files.exists(file()));
        var invalidIds=transaction();invalidIds.getAsJsonArray("originalBodies").add("not-a-body-id");
        assertThrows(IOException.class,()->journal.write(invalidIds));
        var future=transaction();future.getAsJsonObject("source").addProperty("schemaVersion",2);
        assertThrows(IOException.class,()->journal.write(future));
        var untracked=transaction();untracked.addProperty("phase","COMMITTED");untracked.add("candidates",new JsonArray());
        assertThrows(IOException.class,()->journal.write(untracked));
        var overlap=transaction();overlap.getAsJsonArray("candidates").add(overlap.get("originalBody"));
        assertThrows(IOException.class,()->journal.write(overlap));
        var foreignBefore=transaction();foreignBefore.getAsJsonObject("before").addProperty("vesselId",UUID.randomUUID().toString());
        assertThrows(IOException.class,()->journal.write(foreignBefore));
    }

    @Test void finishArchivesExactTransactionAndAllowsAnotherIndependentRecovery() throws Exception {
        var transaction=transaction();transaction.addProperty("phase","COMMITTED");var journal=new RecoveryJournal(directory);journal.write(transaction);
        byte[] committed=Files.readAllBytes(file());journal.finish("committed");
        assertNull(new RecoveryJournal(directory).read());
        try(var paths=Files.list(directory.resolve("recovery-history"))){
            var history=paths.toList();assertEquals(1,history.size());assertArrayEquals(committed,Files.readAllBytes(history.getFirst()));
        }
        var retry=transaction();journal.write(retry);assertEquals(retry,new RecoveryJournal(directory).read());journal.finish("rolled-back");
        try(var paths=Files.list(directory.resolve("recovery-history"))){assertEquals(2,paths.count());}
    }

    private static JsonObject transaction(){
        return recoveryTransaction(UUID.randomUUID(),UUID.randomUUID(),UUID.randomUUID(),"PREPARED");
    }
}
