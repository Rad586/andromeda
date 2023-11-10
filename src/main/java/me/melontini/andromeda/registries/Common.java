package me.melontini.andromeda.registries;

import lombok.SneakyThrows;
import me.melontini.andromeda.Andromeda;
import me.melontini.andromeda.config.Config;
import me.melontini.andromeda.networks.ServerSideNetworking;
import me.melontini.andromeda.util.AndromedaLog;
import me.melontini.andromeda.util.annotations.AndromedaRegistry;
import me.melontini.andromeda.util.annotations.Feature;
import me.melontini.dark_matter.api.base.reflect.Reflect;
import me.melontini.dark_matter.api.base.util.Utilities;
import me.melontini.dark_matter.api.base.util.classes.ThrowingSupplier;
import me.melontini.dark_matter.api.content.ContentBuilder;
import me.melontini.dark_matter.api.content.RegistryUtil;
import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

import static me.melontini.andromeda.util.CommonValues.MODID;

public class Common {

    @SneakyThrows
    private static void bootstrap(Class<?> cls) {
        AndromedaRegistry registry = cls.getAnnotation(AndromedaRegistry.class);
        if (registry == null) return;

        initKeepers(cls);
        Reflect.findMethod(cls, "init").ifPresent(m -> Utilities.runUnchecked(() -> m.invoke(null)));

        share(registry.value(), cls);
        AndromedaLog.info("%s init complete!".formatted(cls.getSimpleName()));
    }

    private static void initKeepers(@NotNull Class<?> reg) {
        for (Field field : reg.getFields()) {
            if (field.getType() != Keeper.class) continue;

            Keeper<?> keeper = (Keeper<?>) Utilities.supplyUnchecked(() -> field.get(reg));
            if (keeper.initialized()) throw new IllegalStateException("Registry object bootstrapped before the registry itself!");
            Feature f = field.getAnnotation(Feature.class);
            try {
                keeper.initialize(f);
            } catch (Throwable t) {
                AndromedaLog.error("Failed to bootstrap registry object %s!".formatted(field.getName()), t);
                if (f != null) Config.processUnknownException(t, f.value());
            }
        }
    }

    static <T, R extends ContentBuilder.CommonBuilder<T>> Keeper<T> start(Supplier<R> supplier) {
        R builder = supplier.get();
        return new Keeper<>(() -> builder::build);
    }

    static Identifier id(String path) {
        return new Identifier(MODID, path);
    }

    static void share(String id, Object object) {
        FabricLoader.getInstance().getObjectShare().put(id, object);
    }

    @SneakyThrows
    public static void bootstrap() {
        List.of(BlockRegistry.class, EntityTypeRegistry.class, ItemRegistry.class, ScreenHandlerRegistry.class, TagRegistry.class).forEach(Common::bootstrap);
        ServerSideNetworking.register();
        ResourceRegistry.register();

        Andromeda.get().AGONY = run(() -> new DamageSource("andromeda_agony"), "minorInconvenience");
        Andromeda.get().LEAF_SLOWNESS = run(() -> new EntityAttributeModifier(UUID.fromString("f72625eb-d4c4-4e1d-8e5c-1736b9bab349"), "Leaf Slowness", -0.3, EntityAttributeModifier.Operation.MULTIPLY_BASE), "leafSlowdown");
        Andromeda.get().KNOCKOFF_TOTEM_PARTICLE = RegistryUtil.create(id("knockoff_totem_particles"), "particle_type", FabricParticleTypes::simple);
    }

    public static <T> T run(ThrowingSupplier<T, Throwable> callable, String... features) {
        try {
            return callable.get();
        } catch (Throwable e) {
            AndromedaLog.error("Something went very wrong! Disabling %s".formatted(Arrays.toString(features)), e);
            Config.processUnknownException(e, features);
            return null;
        }
    }
}
