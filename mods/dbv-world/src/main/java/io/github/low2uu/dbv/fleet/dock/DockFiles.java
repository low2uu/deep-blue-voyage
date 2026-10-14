package io.github.low2uu.dbv.fleet.dock;

import java.io.IOException;
import java.nio.file.*;
import net.minecraft.nbt.*;

/** Fail closed: never replace an existing snapshot or fall back to a non-atomic rename. */
final class DockFiles {
    private DockFiles() {}
    static void create(Path path, CompoundTag data) throws IOException {
        var bytes = new java.io.ByteArrayOutputStream();
        NbtIo.writeCompressed(data, bytes);
        AtomicSnapshotFile.create(path,bytes.toByteArray());
    }
    static CompoundTag read(Path path) throws IOException {
        AtomicSnapshotFile.requireComplete(path);
        return NbtIo.readCompressed(path, NbtAccounter.create(16 * 1024 * 1024));
    }
}
