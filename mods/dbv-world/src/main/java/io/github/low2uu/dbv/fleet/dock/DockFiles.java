package io.github.low2uu.dbv.fleet.dock;

import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import net.minecraft.nbt.*;

/** Versioned JSON is authoritative; binary NBT is read only for verified legacy migration. */
public final class DockFiles {
    private static final long MAX_SNAPSHOT_BYTES = 512L * 1024 * 1024;
    private DockFiles() {}
    public static void create(Path path, CompoundTag data) throws IOException {
        requireJson(path);AtomicSnapshotFile.create(path,encodeReadable(data));
    }
    public static CompoundTag read(Path path) throws IOException {
        requireJson(path);AtomicSnapshotFile.requireComplete(path);
        if(Files.size(path)>MAX_SNAPSHOT_BYTES)throw new IOException("船坞 JSON 超出读取预算");
        try(var input=Files.newInputStream(path)){return DockJsonCodec.decode(input);}
    }
    public static void replace(Path path,CompoundTag data) throws IOException {
        requireJson(path);AtomicSnapshotFile.replace(path,encodeReadable(data));
    }
    private static void requireJson(Path path) throws IOException {
        if(!path.getFileName().toString().endsWith(".json"))throw new IOException("新的船坞档案必须使用 JSON："+path.getFileName());
    }
    /** Reject snapshots that recovery cannot read before committing or changing world blocks. */
    private static byte[] encodeReadable(CompoundTag data) throws IOException {
        var bytes=DockJsonCodec.encode(data);
        if(bytes.length>MAX_SNAPSHOT_BYTES)throw new IOException("船坞 JSON 超出读取预算");
        try(var input=new java.io.ByteArrayInputStream(bytes)) {
            if(!data.equals(DockJsonCodec.decode(input)))throw new IOException("船坞 JSON 数据无法无损往返");
        }
        return bytes;
    }
    /** Restart after publication resumes archival only after comparing both representations. */
    public static void migrateDirectory(Path directory) throws IOException {
        if(!Files.exists(directory))return;
        var files=new ArrayList<Path>();
        for(Path folder:java.util.List.of(directory,directory.resolve("history"))){
            if(!Files.isDirectory(folder))continue;
            try(var entries=Files.list(folder)){
                for(Path path:entries.filter(Files::isRegularFile).toList()){
                    String name=path.getFileName().toString();
                    if(name.endsWith(".nbt.pending"))throw new IOException("旧船坞 NBT 写入中断，请保留现场："+name);
                    if(name.endsWith(".nbt"))files.add(path);
                }
            }
        }
        for(Path legacy:files){
            String name=legacy.getFileName().toString();
            Path target=legacy.resolveSibling(name.substring(0,name.length()-4)+".json");
            AtomicSnapshotFile.requireComplete(target);
            CompoundTag old=NbtIo.readCompressed(legacy,NbtAccounter.create(MAX_SNAPSHOT_BYTES));
            if(Files.exists(target)){
                if(!old.equals(read(target)))throw new IOException("船坞 JSON 与旧 NBT 内容冲突，请保留两份档案："+name);
            }else create(target,old);
            Path archived=directory.resolve("legacy-nbt").resolve(directory.relativize(legacy));
            Files.createDirectories(archived.getParent());
            if(Files.exists(archived)){
                if(Files.mismatch(legacy,archived)!=-1)throw new IOException("旧船坞档案归档冲突："+name);
                Files.delete(legacy);
            }else Files.move(legacy,archived,StandardCopyOption.ATOMIC_MOVE);
        }
    }
}
