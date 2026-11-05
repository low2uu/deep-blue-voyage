package io.github.low2uu.dbv.fleet.dock;

import java.nio.file.*;
import java.io.IOException;
import net.minecraft.nbt.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;

class DockFilesTest {
    @TempDir Path directory;

    @Test void snapshotLargerThanOldReadBudgetSurvivesCreateAndReplace() throws Exception {
        var path=directory.resolve("large-refit.json");
        var snapshot=new CompoundTag();
        var payload=new byte[17 * 1024 * 1024];
        payload[0]=37;payload[payload.length-1]=83;
        snapshot.putByteArray("payload",payload);
        snapshot.putString("phase","PREPARING");
        DockFiles.create(path,snapshot);
        assertArrayEquals(payload,DockFiles.read(path).getByteArray("payload"));
        snapshot.putString("phase","STATIC");
        DockFiles.replace(path,snapshot);
        var recovered=DockFiles.read(path);
        assertEquals("STATIC",recovered.getString("phase"));
        assertArrayEquals(payload,recovered.getByteArray("payload"));
    }

    @Test void typedJsonPreservesAllDeviceValueTypesAndLargeIntegers() throws Exception {
        var data=deviceSnapshot();var path=directory.resolve("starter.json");DockFiles.create(path,data);
        String text=Files.readString(path);
        assertTrue(text.startsWith("{"));assertTrue(text.contains("\"format\":\"dbv-dock-json\""));
        assertTrue(text.contains("\"schemaVersion\":1"));assertTrue(text.contains("设备库存"));
        assertEquals(data,DockFiles.read(path));
        data.putString("phase","STATIC");DockFiles.replace(path,data);
        assertEquals(data,DockFiles.read(path));assertFalse(Files.exists(directory.resolve("starter.json.pending")));
    }

    @Test void corruptOrFutureJsonCannotBeReadOrSilentlyTreatedAsNbt() throws Exception {
        var path=directory.resolve("block-refit.json");DockFiles.create(path,deviceSnapshot());
        String original=Files.readString(path);
        Files.writeString(path,original.replace("设备库存","损坏内容"));
        assertThrows(IOException.class,()->DockFiles.read(path));
        Files.writeString(path,original.replace("\"schemaVersion\":1","\"schemaVersion\":2"));
        assertThrows(IOException.class,()->DockFiles.read(path));
        Files.writeString(path,original+"{}");assertThrows(IOException.class,()->DockFiles.read(path));
        assertThrows(IOException.class,()->DockFiles.create(directory.resolve("new.nbt"),deviceSnapshot()));
    }

    @Test void rootAndHistoryNbtMigrateOnceWithOriginalBytesArchived() throws Exception {
        var data=deviceSnapshot();var legacy=directory.resolve("starter.nbt");NbtIo.writeCompressed(data,legacy);
        byte[] original=Files.readAllBytes(legacy);Files.createDirectories(directory.resolve("history"));
        NbtIo.writeCompressed(data,directory.resolve("history/previous.nbt"));
        DockFiles.migrateDirectory(directory);
        assertEquals(data,DockFiles.read(directory.resolve("starter.json")));
        assertEquals(data,DockFiles.read(directory.resolve("history/previous.json")));
        assertArrayEquals(original,Files.readAllBytes(directory.resolve("legacy-nbt/starter.nbt")));
        assertFalse(Files.exists(legacy));assertFalse(Files.exists(directory.resolve("history/previous.nbt")));
        byte[] published=Files.readAllBytes(directory.resolve("starter.json"));DockFiles.migrateDirectory(directory);
        assertArrayEquals(published,Files.readAllBytes(directory.resolve("starter.json")));
    }

    @Test void migrationResumesAfterJsonPublicationWithoutReplacingIt() throws Exception {
        var data=deviceSnapshot();NbtIo.writeCompressed(data,directory.resolve("block-refit.nbt"));
        DockFiles.create(directory.resolve("block-refit.json"),data);
        byte[] published=Files.readAllBytes(directory.resolve("block-refit.json"));
        DockFiles.migrateDirectory(directory);
        assertArrayEquals(published,Files.readAllBytes(directory.resolve("block-refit.json")));
        assertFalse(Files.exists(directory.resolve("block-refit.nbt")));
        assertTrue(Files.exists(directory.resolve("legacy-nbt/block-refit.nbt")));
    }

    @Test void conflictingMigrationPreservesBothSources() throws Exception {
        var data=deviceSnapshot();NbtIo.writeCompressed(data,directory.resolve("starter.nbt"));
        var different=data.copy();different.putString("phase","OTHER");DockFiles.create(directory.resolve("starter.json"),different);
        assertThrows(IOException.class,()->DockFiles.migrateDirectory(directory));
        assertTrue(Files.exists(directory.resolve("starter.nbt")));
        assertEquals(different,DockFiles.read(directory.resolve("starter.json")));
    }

    @Test void interruptedLegacyOrJsonWriteNeverLosesOriginal() throws Exception {
        NbtIo.writeCompressed(deviceSnapshot(),directory.resolve("starter.nbt"));
        Files.writeString(directory.resolve("block-refit.nbt.pending"),"partial");
        assertThrows(IOException.class,()->DockFiles.migrateDirectory(directory));
        assertFalse(Files.exists(directory.resolve("starter.json")));
        Files.delete(directory.resolve("block-refit.nbt.pending"));
        Files.writeString(directory.resolve("starter.json.pending"),"partial");
        assertThrows(IOException.class,()->DockFiles.migrateDirectory(directory));
        assertTrue(Files.exists(directory.resolve("starter.nbt")));
        assertEquals("partial",Files.readString(directory.resolve("starter.json.pending")));
    }

    private static CompoundTag deviceSnapshot(){
        var data=new CompoundTag();data.putByte("byte",Byte.MIN_VALUE);data.putShort("short",Short.MAX_VALUE);
        data.putInt("int",Integer.MIN_VALUE);data.putLong("long",Long.MAX_VALUE);
        data.putFloat("float",0.125f);data.putDouble("double",Math.PI);data.putString("name","设备库存\n\"氧气\"");
        data.putByteArray("bytes",new byte[]{-128,0,127});data.putIntArray("ints",new int[]{Integer.MIN_VALUE,Integer.MAX_VALUE});
        data.putLongArray("longs",new long[]{Long.MIN_VALUE,9007199254740993L,Long.MAX_VALUE});
        data.putUUID("body",java.util.UUID.fromString("12345678-1234-1234-1234-123456789abc"));
        var nested=new CompoundTag();nested.putLong("energy",9876543210L);nested.putString("fluid","createdieselgenerators:diesel");
        var list=new ListTag();list.add(nested);data.put("devices",list);data.put("empty",new ListTag());return data;
    }
}
