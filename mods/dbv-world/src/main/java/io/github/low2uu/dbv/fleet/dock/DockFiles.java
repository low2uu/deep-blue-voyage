package io.github.low2uu.dbv.fleet.dock;

import java.io.IOException;
import java.nio.file.*;
import net.minecraft.nbt.*;

/** Fail closed: never replace an existing snapshot or fall back to a non-atomic rename. */
final class DockFiles {
    private static final long MAX_SNAPSHOT_BYTES = 512L * 1024 * 1024;
    private DockFiles() {}
    static void create(Path path, CompoundTag data) throws IOException {
        AtomicSnapshotFile.create(path,encodeReadable(data));
    }
    static CompoundTag read(Path path) throws IOException {
        AtomicSnapshotFile.requireComplete(path);
        return NbtIo.readCompressed(path, NbtAccounter.create(MAX_SNAPSHOT_BYTES));
    }
    static void replace(Path path,CompoundTag data) throws IOException {
        AtomicSnapshotFile.requireComplete(path);
        var pending=path.resolveSibling(path.getFileName()+".pending");
        var bytes=encodeReadable(data);
        try(var channel=java.nio.channels.FileChannel.open(pending,StandardOpenOption.CREATE_NEW,StandardOpenOption.WRITE)){
            var buffer=java.nio.ByteBuffer.wrap(bytes);
            while(buffer.hasRemaining())channel.write(buffer);channel.force(true);
        }
        Files.move(pending,path,StandardCopyOption.ATOMIC_MOVE,StandardCopyOption.REPLACE_EXISTING);
    }
    /** Reject snapshots that recovery cannot read before committing or changing world blocks. */
    private static byte[] encodeReadable(CompoundTag data) throws IOException {
        var output=new java.io.ByteArrayOutputStream();
        NbtIo.writeCompressed(data,output);
        var bytes=output.toByteArray();
        try(var input=new java.io.ByteArrayInputStream(bytes)) {
            NbtIo.readCompressed(input,NbtAccounter.create(MAX_SNAPSHOT_BYTES));
        }
        return bytes;
    }
}
