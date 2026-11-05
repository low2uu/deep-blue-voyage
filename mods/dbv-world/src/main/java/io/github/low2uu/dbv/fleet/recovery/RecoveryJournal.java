package io.github.low2uu.dbv.fleet.recovery;

import static io.github.low2uu.dbv.fleet.recovery.RecoveryMessages.text;
import com.google.gson.*;
import io.github.low2uu.dbv.fleet.persistence.VesselDataStore;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.Set;
import java.util.UUID;

/** Durable recovery decision. The old carrier is removed only after the registry has committed. */
public final class RecoveryJournal {
    private static final Gson JSON=new GsonBuilder().setPrettyPrinting().create();
    private final Path file;
    public RecoveryJournal(Path vesselDirectory){file=vesselDirectory.resolve("recovery-transaction.json");}
    public JsonObject read() throws IOException {
        Path pending=file.resolveSibling(file.getFileName()+".pending");
        // An interrupted replacement never invalidates the last committed decision.
        if(Files.exists(pending)){
            if(!Files.exists(file)){decode(Files.readString(pending));Files.move(pending,file,StandardCopyOption.ATOMIC_MOVE);}
            else {decode(Files.readString(file));Files.delete(pending);}
        }
        return Files.exists(file)?decode(Files.readString(file)):null;
    }
    private JsonObject decode(String text) throws IOException {
        try {
            var envelope=JsonParser.parseString(text).getAsJsonObject();var data=envelope.getAsJsonObject("payload");
            if(envelope.get("schemaVersion").getAsInt()!=1||!VesselDataStore.fingerprint(data).equals(envelope.get("sha256").getAsString()))throw new IllegalArgumentException("checksum");
            String phase=data.get("phase").getAsString();
            if(!Set.of("PREPARED","PLACED","VERIFIED","COMMITTED").contains(phase))throw new IllegalArgumentException("phase");
            UUID vessel=UUID.fromString(data.get("vesselId").getAsString());
            UUID original=UUID.fromString(data.get("originalBody").getAsString());
            var source=data.getAsJsonObject("source");var before=data.getAsJsonObject("before");
            for(var snapshot:java.util.List.of(source,before)){
                if(snapshot.get("schemaVersion").getAsInt()!=1||!vessel.toString().equals(snapshot.get("vesselId").getAsString())
                        ||!snapshot.get("modules").isJsonObject())throw new IllegalArgumentException("snapshot identity/version");
            }
            if(!VesselDataStore.completeVessel(source))throw new IllegalArgumentException("source");
            var originals=new java.util.HashSet<UUID>();var candidates=new java.util.HashSet<UUID>();
            for(var value:data.getAsJsonArray("originalBodies"))
                if(!originals.add(UUID.fromString(value.getAsString())))throw new IllegalArgumentException("duplicate original");
            for(var value:data.getAsJsonArray("candidates")){
                UUID id=UUID.fromString(value.getAsString());
                if(!candidates.add(id)||originals.contains(id)||original.equals(id))throw new IllegalArgumentException("candidate/original overlap");
            }
            if(!phase.equals("PREPARED")&&!data.has("candidate"))throw new IllegalArgumentException("missing candidate");
            if(data.has("candidate")&&!candidates.contains(UUID.fromString(data.get("candidate").getAsString())))
                throw new IllegalArgumentException("unrecorded candidate");
            return data;
        } catch(RuntimeException e){throw new IOException(text("journal.corrupt"),e);}
    }
    public void write(JsonObject data) throws IOException {
        var envelope=new JsonObject();envelope.addProperty("schemaVersion",1);envelope.add("payload",data.deepCopy());
        envelope.addProperty("sha256",VesselDataStore.fingerprint(data));String text=JSON.toJson(envelope);decode(text);
        Files.createDirectories(file.getParent());Path pending=file.resolveSibling(file.getFileName()+".pending");
        if(Files.exists(pending))read();
        try(var channel=FileChannel.open(pending,StandardOpenOption.CREATE_NEW,StandardOpenOption.WRITE)){
            var bytes=ByteBuffer.wrap(text.getBytes(StandardCharsets.UTF_8));while(bytes.hasRemaining())channel.write(bytes);channel.force(true);
        }
        Files.move(pending,file,StandardCopyOption.ATOMIC_MOVE,StandardCopyOption.REPLACE_EXISTING);
    }
    public void finish(String outcome) throws IOException {
        if(!Files.exists(file))return;
        Path history=file.getParent().resolve("recovery-history");Files.createDirectories(history);
        Files.move(file,history.resolve(UUID.randomUUID()+"-"+outcome+".json"),StandardCopyOption.ATOMIC_MOVE);
    }
}
