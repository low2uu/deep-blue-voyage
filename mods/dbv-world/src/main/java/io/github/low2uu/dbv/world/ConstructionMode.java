package io.github.low2uu.dbv.world;

import com.mojang.logging.LogUtils;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.Map;
import java.util.Properties;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.storage.LevelResource;
import net.neoforged.neoforge.event.server.ServerStartedEvent;

/** Opt-in construction session settings, with a per-save snapshot for later restoration. */
public final class ConstructionMode {
    public static final boolean ENABLED = Boolean.getBoolean("dbv.construction");
    private static final Map<String, GameRules.Key<GameRules.BooleanValue>> RULES = Map.of(
            "doMobSpawning", GameRules.RULE_DOMOBSPAWNING,
            "doPatrolSpawning", GameRules.RULE_DO_PATROL_SPAWNING,
            "doTraderSpawning", GameRules.RULE_DO_TRADER_SPAWNING,
            "doInsomnia", GameRules.RULE_DOINSOMNIA,
            "doWardenSpawning", GameRules.RULE_DO_WARDEN_SPAWNING,
            "disableRaids", GameRules.RULE_DISABLE_RAIDS);

    private ConstructionMode() {}

    public static void onServerStarted(ServerStartedEvent event) {
        var server = event.getServer();
        if (!(OceanWorlds.isProjectWorld(server.overworld().getChunkSource().getGenerator()))) { return; }
        var path = server.getWorldPath(LevelResource.ROOT).resolve("dbv-construction-rules.properties");
        GameRules rules = server.getGameRules();
        try {
            if (ENABLED) {
                if (!Files.exists(path)) {
                    Properties original = new Properties();
                    RULES.forEach((name, key) -> original.setProperty(name, Boolean.toString(rules.getBoolean(key))));
                    original.setProperty("spawnChunkRadius", Integer.toString(rules.getInt(GameRules.RULE_SPAWN_CHUNK_RADIUS)));
                    StringWriter writer = new StringWriter();
                    original.store(writer, "Original rules before temporary construction mode; keep until restored");
                    Files.writeString(path, writer.toString(), StandardOpenOption.CREATE_NEW);
                }
                boolean ecology = server.overworld().getChunkSource().getGenerator() instanceof FloodedWorldGenerator;
                RULES.forEach((name, key) -> rules.getRule(key).set(name.equals("disableRaids") || ecology && name.equals("doMobSpawning"), server));
                rules.getRule(GameRules.RULE_SPAWN_CHUNK_RADIUS).set(0, server);
                LogUtils.getLogger().info("DBV construction mode active: natural spawning {}, spawn chunk radius 0; original rules preserved.", ecology ? "enabled for flooded ecology" : "disabled");
            } else if (Files.exists(path)) {
                Properties original = new Properties();
                original.load(new StringReader(Files.readString(path)));
                // Validate the entire snapshot before changing any rule.
                for (String name : RULES.keySet()) {
                    if (!"true".equals(original.getProperty(name)) && !"false".equals(original.getProperty(name))) {
                        throw new IOException("Invalid saved rule: " + name);
                    }
                }
                int radius = Integer.parseInt(original.getProperty("spawnChunkRadius"));
                if (radius < 0 || radius > 32) { throw new IOException("Invalid saved spawn chunk radius"); }
                RULES.forEach((name, key) -> rules.getRule(key).set(Boolean.parseBoolean(original.getProperty(name)), server));
                rules.getRule(GameRules.RULE_SPAWN_CHUNK_RADIUS).set(radius, server);
                server.saveEverything(false, true, true);
                Files.delete(path);
                LogUtils.getLogger().info("DBV construction mode disabled: original world rules restored and saved.");
            }
        } catch (IOException | IllegalArgumentException exception) {
            throw new IllegalStateException("Cannot safely apply or restore DBV construction settings: " + path, exception);
        }
    }
}
