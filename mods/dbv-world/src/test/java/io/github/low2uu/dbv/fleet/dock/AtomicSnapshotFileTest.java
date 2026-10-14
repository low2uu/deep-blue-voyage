package io.github.low2uu.dbv.fleet.dock;

import static org.junit.jupiter.api.Assertions.*;
import java.nio.file.*;
import java.io.IOException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class AtomicSnapshotFileTest {
    @TempDir Path directory;
    @Test void completedSnapshotPreservesAllBytes() throws Exception {
        byte[] bytes=new byte[65537];new java.util.Random(1).nextBytes(bytes);
        var file=directory.resolve("active.nbt");AtomicSnapshotFile.create(file,bytes);
        assertArrayEquals(bytes,Files.readAllBytes(file));
        assertFalse(Files.exists(directory.resolve("active.nbt.pending")));
    }
    @Test void secondWriteCannotReplaceOriginalSnapshot() throws Exception {
        var file=directory.resolve("active.nbt");byte[] original={1,2,3};AtomicSnapshotFile.create(file,original);
        assertThrows(IOException.class,()->AtomicSnapshotFile.create(file,new byte[]{9}));
        assertArrayEquals(original,Files.readAllBytes(file));
    }
    @Test void interruptedWriteBlocksReadAndReplacement() throws Exception {
        var file=directory.resolve("active.nbt");AtomicSnapshotFile.create(file,new byte[]{1});
        Files.writeString(directory.resolve("active.nbt.pending"),"interrupted");
        assertThrows(IOException.class,()->AtomicSnapshotFile.requireComplete(file));
        assertThrows(IOException.class,()->AtomicSnapshotFile.create(file,new byte[]{2}));
    }
    @Test void unpublishedPartialSnapshotCannotBeSilentlyReplaced() throws Exception {
        var file=directory.resolve("active.nbt");var pending=directory.resolve("active.nbt.pending");
        Files.writeString(pending,"partial");
        assertThrows(IOException.class,()->AtomicSnapshotFile.create(file,new byte[]{2}));
        assertFalse(Files.exists(file));assertEquals("partial",Files.readString(pending));
    }
}
