package io.github.low2uu.dbv.content;

import com.google.gson.Gson;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Squid;
import net.minecraft.world.entity.monster.Drowned;
import net.minecraft.world.entity.monster.Guardian;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.DeferredSpawnEggItem;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Deliberately opt-in prototypes: no biome modifiers, recipes or ship physics changes. */
public final class PrototypeContent {
    private static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(Registries.ENTITY_TYPE, "dbv_world");
    private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(Registries.ITEM, "dbv_world");
    public record ItemSpec(String id, int stack) {}
    public record MobSpec(String id, String base, double health, double plannedHealth, float width, float height,
            float scale, double speed, double attackDamage, double armor, double followRange,
            double knockbackResistance, boolean boss, int eggPrimary, int eggSecondary) {}
    public record Catalog(List<ItemSpec> items, List<MobSpec> mobs) {}
    public record Entry<T extends net.minecraft.world.entity.Mob>(MobSpec spec, DeferredHolder<EntityType<?>, EntityType<T>> type) {}
    public static final List<Entry<Squid>> SQUIDS = new ArrayList<>();
    public static final List<Entry<Guardian>> GUARDIANS = new ArrayList<>();
    public static final List<Entry<Drowned>> DROWNED = new ArrayList<>();
    static {
        try (var reader = new InputStreamReader(Objects.requireNonNull(PrototypeContent.class.getResourceAsStream("/dbv_prototypes.json")), StandardCharsets.UTF_8)) {
            var catalog = new Gson().fromJson(reader, Catalog.class);
            for (var item : catalog.items()) ITEMS.register(item.id(), () -> new PrototypeItem(new Item.Properties().stacksTo(item.stack())));
            for (var m : catalog.mobs()) {
                switch (m.base()) {
                    case "squid" -> SQUIDS.add(register(m, Squid::new, MobCategory.WATER_CREATURE));
                    case "guardian" -> GUARDIANS.add(register(m, Guardian::new, MobCategory.MONSTER));
                    case "drowned" -> DROWNED.add(register(m, Drowned::new, MobCategory.MONSTER));
                    default -> throw new IllegalArgumentException("Unsupported prototype base: " + m.base());
                }
            }
        } catch (Exception e) { throw new IllegalStateException("Invalid bundled prototype catalog", e); }
    }
    private static <T extends net.minecraft.world.entity.Mob> Entry<T> register(MobSpec m, EntityType.EntityFactory<T> factory, MobCategory category) {
        var type = ENTITIES.register(m.id(), () -> EntityType.Builder.of(factory, category).sized(m.width(), m.height()).clientTrackingRange(10).build("dbv_world:" + m.id()));
        ITEMS.register(m.id() + "_spawn_egg", () -> new DeferredSpawnEggItem(type, m.eggPrimary(), m.eggSecondary(), new Item.Properties()));
        return new Entry<>(m, type);
    }
    public static void register(IEventBus bus) {
        ENTITIES.register(bus); ITEMS.register(bus); bus.addListener(PrototypeContent::attributes);
    }
    private static void attributes(EntityAttributeCreationEvent event) {
        for (var e : SQUIDS) event.put(e.type().get(), buildAttributes(Squid.createAttributes(), e.spec()).build());
        for (var e : GUARDIANS) event.put(e.type().get(), buildAttributes(Guardian.createAttributes(), e.spec()).build());
        for (var e : DROWNED) event.put(e.type().get(), buildAttributes(Drowned.createAttributes(), e.spec()).build());
    }
    private static net.minecraft.world.entity.ai.attributes.AttributeSupplier.Builder buildAttributes(net.minecraft.world.entity.ai.attributes.AttributeSupplier.Builder b, MobSpec m) {
        return b.add(Attributes.MAX_HEALTH, m.health()).add(Attributes.MOVEMENT_SPEED, m.speed())
                .add(Attributes.ATTACK_DAMAGE, m.attackDamage()).add(Attributes.ARMOR, m.armor())
                .add(Attributes.FOLLOW_RANGE, m.followRange()).add(Attributes.KNOCKBACK_RESISTANCE, m.knockbackResistance());
    }
    private static final class PrototypeItem extends Item {
        PrototypeItem(Properties properties) { super(properties); }
        @Override public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> lines, TooltipFlag flag) {
            lines.add(Component.translatable("tooltip.dbv_world.prototype"));
        }
    }
    private PrototypeContent() {}
}
