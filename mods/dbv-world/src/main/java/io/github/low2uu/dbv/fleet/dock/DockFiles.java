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
    static void replace(Path path,CompoundTag data) throws IOException {
        AtomicSnapshotFile.requireComplete(path);
        var pending=path.resolveSibling(path.getFileName()+".pending");
        var bytes=new java.io.ByteArrayOutputStream();NbtIo.writeCompressed(data,bytes);
        try(var channel=java.nio.channels.FileChannel.open(pending,StandardOpenOption.CREATE_NEW,StandardOpenOption.WRITE)){
            var buffer=java.nio.ByteBuffer.wrap(bytes.toByteArray());
            while(buffer.hasRemaining())channel.write(buffer);channel.force(true);
        }
        Files.move(pending,path,StandardCopyOption.ATOMIC_MOVE,StandardCopyOption.REPLACE_EXISTING);
    }
}
