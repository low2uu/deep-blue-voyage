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
    /** A pinned whole-vessel image, independent of the rotating automatic history. */
    public JsonObject recoveryCheckpoint() throws IOException {
        Path file=directory.resolve("recovery-checkpoint.json");
        if(!Files.exists(file))return null;
        try {
            var envelope=JsonParser.parseString(Files.readString(file)).getAsJsonObject();
            var payload=envelope.getAsJsonObject("payload");
            if(envelope.get("schemaVersion").getAsInt()!=1||!fingerprint(payload).equals(envelope.get("sha256").getAsString()))
                throw new IOException("整艇恢复点校验失败");
            decode(payload.toString());return envelope;
        } catch(RuntimeException e){throw new IOException("整艇恢复点损坏",e);}
    }
    public void checkpoint(JsonObject root,boolean frozen) throws IOException {
        decode(root.toString());
        if(!completeVessel(root))throw new IOException("档案缺少完整船体蓝图或存储，不能作为整艇恢复点");
        var envelope=new JsonObject();envelope.addProperty("schemaVersion",1);
        envelope.addProperty("capturedAt",java.time.Instant.now().toString());envelope.addProperty("damageDetected",frozen);
        envelope.add("payload",root.deepCopy());envelope.addProperty("sha256",fingerprint(root));
        write(directory.resolve("recovery-checkpoint.json"),JSON.toJson(envelope),true);
    }
    public static boolean completeVessel(JsonObject root){
        if(root==null||!root.has("modules"))return false;
        var modules=root.getAsJsonObject("modules");var structure=modules.getAsJsonObject("structure");
        return structure!=null&&structure.has("nativeBlueprint")&&structure.has("blocks")
                &&!structure.getAsJsonArray("blocks").isEmpty()&&modules.has("storage");
    }
    /** Missing or replaced blocks latch the previous complete image until an explicit backup/refit. */
    public void updateCheckpoint(JsonObject previous,JsonObject current) throws IOException {
        var saved=recoveryCheckpoint();
        if(saved!=null&&saved.get("damageDetected").getAsBoolean())return;
        // state.json can have advanced before a crash while the pinned image still contains the intact ship.
        // Always compare to that committed image, never to a possibly already damaged current mirror.
        var baseline=saved!=null?saved.getAsJsonObject("payload"):completeVessel(previous)?previous:null;
        if(!completeVessel(current)){
            if(completeVessel(baseline))checkpoint(baseline,true);return;
        }
        if(completeVessel(baseline)&&lostBlocks(baseline,current)){
            checkpoint(baseline,true);return;
        }
        checkpoint(current,false);
    }
    static boolean lostBlocks(JsonObject before,JsonObject after){
        var oldTopology=before.getAsJsonObject("modules").getAsJsonObject("structure").getAsJsonObject("recoveryTopology");
        var newTopology=after.getAsJsonObject("modules").getAsJsonObject("structure").getAsJsonObject("recoveryTopology");
        if(oldTopology!=null){
            if(newTopology==null)return true;
            for(var e:oldTopology.entrySet())if(!e.getValue().equals(newTopology.get(e.getKey())))return true;
        }
        var now=new java.util.HashMap<String,String>();
        for(var e:after.getAsJsonObject("modules").getAsJsonObject("structure").getAsJsonArray("blocks")){
            var b=e.getAsJsonObject();now.put(positionKey(b),b.getAsJsonObject("state").get("Name").getAsString());
        }
        for(var e:before.getAsJsonObject("modules").getAsJsonObject("structure").getAsJsonArray("blocks")){
            var b=e.getAsJsonObject();if(!b.getAsJsonObject("state").get("Name").getAsString().equals(now.get(positionKey(b))))return true;
        }
        return false;
    }
    private static String positionKey(JsonObject b){return b.get("x")+","+b.get("y")+","+b.get("z");}
    public void replaceFromRecovery(JsonObject root) throws IOException {
        decode(root.toString());retainInterrupted("state.json");retainInterrupted("recovery-checkpoint.json");
        retainInterrupted("storage-recovery.json");
        Path marker=directory.resolve("storage-recovery.json");
        if(Files.exists(marker)){
            Path history=directory.resolve("recovery-history");Files.createDirectories(history);
            Files.move(marker,history.resolve(UUID.randomUUID()+"-superseded-storage-recovery.json"),StandardCopyOption.ATOMIC_MOVE);
        }
        write(directory.resolve("state.json"),JSON.toJson(root),true);
    }
    /** A verified recovery point remains usable when the current mirror was only partially written. */
    public JsonObject recoveryBefore(JsonObject verifiedSource) throws IOException {
        decode(verifiedSource.toString());if(!completeVessel(verifiedSource))throw new IOException("整艇恢复源不完整");
        try{return read();}catch(IOException unreadable){
            Path evidence=directory.resolve("recovery-evidence").resolve(UUID.randomUUID().toString());Files.createDirectories(evidence);
            for(String name:java.util.List.of("state.json","state.json.pending","storage-recovery.json","storage-recovery.json.pending")){
                Path file=directory.resolve(name);if(Files.exists(file))Files.copy(file,evidence.resolve(name));
            }
            var before=verifiedSource.deepCopy();var marker=new JsonObject();marker.addProperty("unreadableCurrentState",true);
            marker.addProperty("evidence",directory.relativize(evidence).toString().replace('\\','/'));
            before.getAsJsonObject("modules").add("recoveryEvidence",marker);return before;
        }
    }
    private void retainInterrupted(String name) throws IOException {
        Path pending=directory.resolve(name+".pending");if(!Files.exists(pending))return;
        Path history=directory.resolve("recovery-history");Files.createDirectories(history);
        Files.move(pending,history.resolve(UUID.randomUUID()+"-"+name+".pending"),StandardCopyOption.ATOMIC_MOVE);
    }
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
