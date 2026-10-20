package io.github.low2uu.dbv.fleet.mixin;

import com.maxenonyme.createsubmarine.submarine.system.SubmarinePressureSystem;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Optional copycat APIs may contain client-only method signatures on dedicated servers. */
@Mixin(value = SubmarinePressureSystem.class, remap = false)
public abstract class SubmarineMaterialLookupMixin {
    @Unique
    private static final Map<Class<?>, Map<String, Optional<Method>>> dbv$materialMethods = new ConcurrentHashMap<>();

    @Inject(method = "lookup", at = @At("HEAD"), cancellable = true, remap = false)
    private static void dbv$safeLookup(Class<?> type, String name, CallbackInfoReturnable<Optional<Method>> cir) {
        cir.setReturnValue(dbv$materialMethods.computeIfAbsent(type, ignored -> new ConcurrentHashMap<>())
                .computeIfAbsent(name, ignored -> {
                    try {
                        return Optional.of(type.getMethod(name));
                    } catch (NoSuchMethodException absent) {
                        return Optional.empty();
                    } catch (RuntimeException | LinkageError unavailable) {
                        // Preserve upstream's NBT/original-state fallback, never skip hull pressure checks.
                        LoggerFactory.getLogger("dbv_world").warn(
                                "材质兼容接口无法加载，使用原模组的材质回退：{}#{} ({})",
                                type.getName(), name, unavailable.toString());
                        return Optional.empty();
                    }
                }));
    }
}
