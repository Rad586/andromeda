package me.melontini.andromeda.registries;

import me.melontini.andromeda.Andromeda;
import me.melontini.andromeda.networks.ServerSideNetworking;
import me.melontini.andromeda.util.AndromedaLog;
import me.melontini.dark_matter.api.base.util.classes.ThrowingRunnable;
import me.melontini.dark_matter.api.content.RegistryUtil;
import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ObjectShare;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;

import java.util.UUID;
import java.util.concurrent.Callable;

import static me.melontini.andromeda.config.ConfigHelper.run;
import static me.melontini.andromeda.util.SharedConstants.MODID;

public class Common {

    static Identifier id(String path) {
        return new Identifier(MODID, path);
    }

    static <T> T call(Callable<T> callable) {
        try {
            return callable.call();
        } catch (Throwable e) {
            AndromedaLog.error("Error while registering content: {}: {}", e.getClass().getName(), e.getLocalizedMessage());
            return null;
        }
    }

    static void call(ThrowingRunnable runnable) {
        try {
            runnable.run();
        } catch (Throwable e) {
            AndromedaLog.error("Error while registering content: {}: {}", e.getClass().getName(), e.getLocalizedMessage());
        }
    }

    static ObjectShare objectShare() {
        return FabricLoader.getInstance().getObjectShare();
    }

    static void share(String id, Object object) {
        objectShare().put(id, object);
    }

    public static void bootstrap() {
        BlockRegistry.register();
        ItemRegistry.register();
        EntityTypeRegistry.register();
        ServerSideNetworking.register();
        ResourceRegistry.register();
        ScreenHandlerRegistry.register();
        TagRegistry.register();

        Andromeda.get().AGONY = run(() -> RegistryKey.of(RegistryKeys.DAMAGE_TYPE, id("agony")), "minorInconvenience");
        Andromeda.get().BRICKED = run(() -> RegistryKey.of(RegistryKeys.DAMAGE_TYPE, id("bricked")));
        Andromeda.get().LEAF_SLOWNESS = run(() -> new EntityAttributeModifier(UUID.fromString("f72625eb-d4c4-4e1d-8e5c-1736b9bab349"), "Leaf Slowness", -0.3, EntityAttributeModifier.Operation.MULTIPLY_BASE), "leafSlowdown");
        Andromeda.get().KNOCKOFF_TOTEM_PARTICLE = RegistryUtil.create(id("knockoff_totem_particles"), "particle_type", FabricParticleTypes::simple);
    }
}
