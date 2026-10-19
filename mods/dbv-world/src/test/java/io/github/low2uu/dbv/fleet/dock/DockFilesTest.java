package io.github.low2uu.dbv.fleet.dock;

import java.nio.file.Path;
import net.minecraft.nbt.CompoundTag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;

class DockFilesTest {
    @TempDir Path directory;

    @Test void snapshotLargerThanOldReadBudgetSurvivesCreateAndReplace() throws Exception {
        var path=directory.resolve("large-refit.nbt");
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
}
