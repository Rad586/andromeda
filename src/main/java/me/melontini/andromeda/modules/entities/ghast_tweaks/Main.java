package me.melontini.andromeda.modules.entities.ghast_tweaks;

import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.entity.mob.GhastEntity;
import net.minecraft.world.World;

public class Main {

    Main(GhastTweaks module) {
        ServerLivingEntityEvents.AFTER_DEATH.register((entity, damageSource) -> {
            if (entity instanceof GhastEntity) {
                var c = entity.world.am$get(module);
                if (c.explodeOnDeath) entity.world.createExplosion(entity, entity.getX(), entity.getY(), entity.getZ(), c.explosionPower, World.ExplosionSourceType.MOB);
            }
        });
    }
}
