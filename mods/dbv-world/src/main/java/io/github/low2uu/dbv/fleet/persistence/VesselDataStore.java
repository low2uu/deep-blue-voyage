package io.github.low2uu.dbv.fleet.persistence;

import com.google.gson.*;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.MessageDigest;
import java.util.UUID;

/** Vessel business data. No Minecraft/NBT dependency; identity survives physics-body replacement. */
public final class VesselDataStore {
    private static final Gson JSON=new GsonBuilder().setPrettyPrinting().create();
    private final Path directory;
    private final UUID vessel;
    public VesselDataStore(Path world,UUID vessel){this.vessel=vessel;directory=world.resolve("dbv/fleet/vessels").resolve(vessel.toString());}
    public Path directory(){return directory;}
    public void requireStorageReady() throws IOException {
        Path marker=directory.resolve("storage-recovery.json");requireComplete(marker);
        if(Files.exists(marker))throw new IOException("上次存储恢复中断，已锁定档案；请保留 storage-recovery.json 与备份");
    }
    public void beginStorageRecovery(UUID source,UUID safety) throws IOException {
        requireStorageReady();var marker=new JsonObject();marker.addProperty("schemaVersion",1);marker.addProperty("source",source.toString());marker.addProperty("before",safety.toString());
        write(directory.resolve("storage-recovery.json"),JSON.toJson(marker),false);
    }
    public void finishStorageRecovery() throws IOException {Files.delete(directory.resolve("storage-recovery.json"));}
    public JsonObject read() throws IOException {
        Path file=directory.resolve("state.json");requireComplete(file);
        if(!Files.exists(file)){
            var root=new JsonObject();root.addProperty("schemaVersion",1);root.addProperty("vesselId",vessel.toString());root.add("modules",new JsonObject());return root;
        }
        return decode(Files.readString(file));
    }
    private JsonObject decode(String text) throws IOException {
        try{
            var root=JsonParser.parseString(text).getAsJsonObject();
            if(root.get("schemaVersion").getAsInt()!=1||!vessel.toString().equals(root.get("vesselId").getAsString())||!root.get("modules").isJsonObject())throw new IllegalArgumentException();
            return root;
        }catch(RuntimeException e){throw new IOException("主艇独立数据损坏或版本不支持，禁止覆盖",e);}
    }
    public void putModule(String key,JsonObject value) throws IOException {
        putModules(java.util.Map.of(key,value));
    }
    public void putModules(java.util.Map<String,JsonObject> values) throws IOException {
        var root=read();var modules=root.getAsJsonObject("modules");
        boolean changed=false;
        for(var entry:values.entrySet())if(!entry.getValue().equals(modules.get(entry.getKey()))){modules.add(entry.getKey(),entry.getValue().deepCopy());changed=true;}
        if(changed)write(directory.resolve("state.json"),JSON.toJson(root),true);
    }
    public UUID backup() throws IOException {
        var root=read();UUID id=UUID.randomUUID();String data=JSON.toJson(root);
        var envelope=new JsonObject();envelope.addProperty("schemaVersion",1);envelope.addProperty("sha256",hash(data));envelope.addProperty("payload",data);
        write(directory.resolve("backups").resolve(id+".json"),JSON.toJson(envelope),false);return id;
    }
    public UUID backupAutomatic() throws IOException {
        UUID id=UUID.randomUUID();String data=JSON.toJson(read());var envelope=new JsonObject();
        envelope.addProperty("schemaVersion",1);envelope.addProperty("sha256",hash(data));envelope.addProperty("payload",data);
        Path folder=directory.resolve("automatic");write(folder.resolve(id+".json"),JSON.toJson(envelope),false);
        try(var files=Files.list(folder)){
            var snapshots=files.filter(p->p.getFileName().toString().matches("[0-9a-f-]{36}\\.json")).sorted(java.util.Comparator.comparingLong(p->{try{return Files.getLastModifiedTime(p).toMillis();}catch(IOException e){return Long.MAX_VALUE;}})).toList();
            for(int i=0;i<snapshots.size()-20;i++)Files.delete(snapshots.get(i));
        }
        return id;
    }
    public static String fingerprint(JsonElement value){return hash(value.toString());}
    public JsonObject readBackup(UUID id) throws IOException {
        var path=directory.resolve("backups").resolve(id+".json");requireComplete(path);
        if(!Files.exists(path)){path=directory.resolve("automatic").resolve(id+".json");requireComplete(path);}
        try{
            var envelope=JsonParser.parseString(Files.readString(path)).getAsJsonObject();String data=envelope.get("payload").getAsString();
            if(envelope.get("schemaVersion").getAsInt()!=1||!hash(data).equals(envelope.get("sha256").getAsString()))throw new IOException("主艇备份校验失败");
            return decode(data);
        }catch(RuntimeException e){throw new IOException("主艇备份损坏",e);}
    }
    public void restore(UUID id) throws IOException {
        var root=readBackup(id);read(); // Refuse to overwrite an interrupted or corrupt current state.
        backup();write(directory.resolve("state.json"),JSON.toJson(root),true);
    }
    private static String hash(String data){
        try{return java.util.HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(data.getBytes(StandardCharsets.UTF_8)));}
        catch(java.security.NoSuchAlgorithmException e){throw new IllegalStateException(e);}
    }
    private static void requireComplete(Path path) throws IOException {
        if(Files.exists(path.resolveSibling(path.getFileName()+".pending")))throw new IOException("主艇独立数据写入中断，请保留数据目录");
    }
    private static void write(Path path,String text,boolean replace) throws IOException {
        Files.createDirectories(path.getParent());requireComplete(path);
        if(!replace&&Files.exists(path))throw new IOException("拒绝覆盖既有主艇备份");
        Path pending=path.resolveSibling(path.getFileName()+".pending");
        try(var file=FileChannel.open(pending,StandardOpenOption.CREATE_NEW,StandardOpenOption.WRITE)){
            var bytes=ByteBuffer.wrap(text.getBytes(StandardCharsets.UTF_8));while(bytes.hasRemaining())file.write(bytes);file.force(true);
        }
        if(replace)Files.move(pending,path,StandardCopyOption.ATOMIC_MOVE,StandardCopyOption.REPLACE_EXISTING);
        else Files.move(pending,path,StandardCopyOption.ATOMIC_MOVE);
    }
}
