package io.github.low2uu.dbv.fleet;

import io.github.low2uu.dbv.fleet.domain.FlagshipRegistry;
import io.github.low2uu.dbv.fleet.domain.VesselIdentity;
import io.github.low2uu.dbv.fleet.persistence.RegistryStore;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.CountDownLatch;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;

class RegistryStoreTest {
    @TempDir Path world;

    @Test void refitChangesOnlyCarrierAndGeneration() throws Exception {
        var store=new RegistryStore(world);var first=ship("酒狐号");store.create(first);
        var before=first.flagship().orElseThrow();var body=UUID.randomUUID();
        var after=store.rebind(before.bodyId(),body).flagship().orElseThrow();
        assertEquals(before.vesselId(),after.vesselId());assertEquals(before.name(),after.name());
        assertEquals(before.dimension(),after.dimension());assertEquals(before.generation()+1,after.generation());
        assertEquals(body,after.bodyId());assertEquals(after,new RegistryStore(world).load().flagship().orElseThrow());
        assertThrows(IOException.class,()->store.rebind(before.bodyId(),UUID.randomUUID()));
        assertEquals(after,store.load().flagship().orElseThrow());
    }
    @Test void interruptedCarrierReplacementCannotBecomeANewShip() throws Exception {
        var store=new RegistryStore(world);store.create(ship("酒狐号"));
        Files.writeString(world.resolve("dbv/fleet/registry.refit.pending.json"),"partial");
        assertThrows(IOException.class,store::load);
    }

    private FlagshipRegistry ship(String name) {
        return FlagshipRegistry.empty().register(UUID.randomUUID(), "minecraft:overworld", name);
    }

    @Test void survivesFreshStoreAndPreservesChineseName() throws Exception {
        var registry = ship("酒狐与深蓝远航");
        new RegistryStore(world).create(registry);
        assertEquals(registry, new RegistryStore(world).load());
        assertFalse(Files.exists(world.resolve("dbv/fleet/registration.lock")));
    }

    @Test void refusesSecondRegistrationWithoutReplacingFirst() throws Exception {
        var first = ship("第一艘");
        new RegistryStore(world).create(first);
        assertThrows(IOException.class, () -> new RegistryStore(world).create(ship("第二艘")));
        assertEquals(first, new RegistryStore(world).load());
    }

    @Test void concurrentRequestsHaveExactlyOneWinner() throws Exception {
        var start = new CountDownLatch(1);
        Callable<Boolean> register = () -> {
            start.await();
            try { new RegistryStore(world).create(ship("主艇")); return true; }
            catch (IOException exception) { return false; }
        };
        try (var pool = Executors.newFixedThreadPool(2)) {
            var a = pool.submit(register); var b = pool.submit(register); start.countDown();
            assertEquals(1, (a.get() ? 1 : 0) + (b.get() ? 1 : 0));
        }
        assertTrue(new RegistryStore(world).load().flagship().isPresent());
    }

    @Test void corruptRegistryDoesNotBecomeAnEmptyWorld() throws Exception {
        Path file = world.resolve("dbv/fleet/registry.json");
        Files.createDirectories(file.getParent()); Files.writeString(file, "{broken");
        assertThrows(IOException.class, () -> new RegistryStore(world).load());
        assertEquals("{broken", Files.readString(file));
    }

    @Test void interruptedRegistrationLocksNewRegistration() throws Exception {
        Path lock = world.resolve("dbv/fleet/registration.lock");
        Files.createDirectories(lock.getParent()); Files.createFile(lock);
        assertThrows(IOException.class, () -> new RegistryStore(world).load());
        assertThrows(IOException.class, () -> new RegistryStore(world).create(ship("另一艘")));
        assertFalse(Files.exists(world.resolve("dbv/fleet/registry.json")));
    }

    @Test void unsupportedSchemaAndFractionalGenerationAreRejected() throws Exception {
        new RegistryStore(world).create(ship("主艇"));
        Path file = world.resolve("dbv/fleet/registry.json");
        String valid = Files.readString(file);
        Files.writeString(file, valid.replace("\"schemaVersion\":1", "\"schemaVersion\":2"));
        assertThrows(IOException.class, () -> new RegistryStore(world).load());
        Files.writeString(file, valid.replace("\"generation\":1", "\"generation\":1.5"));
        assertThrows(IOException.class, () -> new RegistryStore(world).load());
    }

    @Test void worldsDoNotShareIdentities() throws Exception {
        var first = ship("第一世界"); var second = ship("第二世界");
        new RegistryStore(world.resolve("a")).create(first);
        new RegistryStore(world.resolve("b")).create(second);
        assertEquals(first, new RegistryStore(world.resolve("a")).load());
        assertEquals(second, new RegistryStore(world.resolve("b")).load());
        assertNotEquals(first.flagship().orElseThrow().vesselId(), second.flagship().orElseThrow().vesselId());
    }

    @Test void unloadingCannotClearTheRegistrySlot() {
        var registered = ship("主艇");
        assertThrows(IllegalStateException.class, () -> registered.register(UUID.randomUUID(), "minecraft:the_nether", "新艇"));
    }

    @Test void invalidIdentityIsRejectedBeforePersistence() {
        assertThrows(IllegalArgumentException.class, () -> ship("\n"));
        assertThrows(IllegalArgumentException.class, () -> new VesselIdentity(UUID.randomUUID(), UUID.randomUUID(), "../bad", "主艇", 1));
        assertThrows(IllegalArgumentException.class, () -> new VesselIdentity(UUID.randomUUID(), UUID.randomUUID(), "minecraft:overworld", "主艇", 0));
    }
}
