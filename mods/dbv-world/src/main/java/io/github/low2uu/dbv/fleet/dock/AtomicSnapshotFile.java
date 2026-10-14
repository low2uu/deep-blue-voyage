package io.github.low2uu.dbv.fleet.dock;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.*;

/** Immutable publication with an explicit interrupted-write marker. */
final class AtomicSnapshotFile {
    private AtomicSnapshotFile() {}
    static void requireComplete(Path path) throws IOException {
        if(Files.exists(path.resolveSibling(path.getFileName()+".pending"))) throw new IOException("船坞档案写入中断，请保留现场");
    }
    static void create(Path path,byte[] bytes) throws IOException {
        Files.createDirectories(path.getParent());
        requireComplete(path);
        if(Files.exists(path)) throw new IOException("船坞档案已存在，拒绝覆盖："+path.getFileName());
        var pending=path.resolveSibling(path.getFileName()+".pending");
        try(var channel=FileChannel.open(pending,StandardOpenOption.CREATE_NEW,StandardOpenOption.WRITE)) {
            var buffer=ByteBuffer.wrap(bytes);while(buffer.hasRemaining())channel.write(buffer);channel.force(true);
        }
        Files.move(pending,path,StandardCopyOption.ATOMIC_MOVE);
    }
}
