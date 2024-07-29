package me.melontini.andromeda.modules.blocks.campfire_effects.mixin;

import java.util.ArrayList;
import java.util.List;
import me.melontini.andromeda.common.util.LootContextUtil;
import me.melontini.andromeda.modules.blocks.campfire_effects.CampfireEffects;
import me.melontini.dark_matter.api.base.util.functions.Memoize;
import net.minecraft.block.BlockState;
import net.minecraft.block.CampfireBlock;
import net.minecraft.block.entity.CampfireBlockEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CampfireBlockEntity.class)
abstract class CampfireBlockEntityMixin {

  @Inject(at = @At("HEAD"), method = "litServerTick")
  private static void andromeda$litServerTick(
      World world, BlockPos pos, BlockState state, CampfireBlockEntity campfire, CallbackInfo ci) {
    if (world.getTime() % 180 == 0) {
      if (state.get(CampfireBlock.LIT)) {
        var config = world.am$get(CampfireEffects.CONFIG);
        var supplier = Memoize.supplier(
            LootContextUtil.block(world, Vec3d.ofCenter(pos), state, null, null, campfire));
        if (!config.available.asBoolean(supplier)) return;

        List<LivingEntity> entities = new ArrayList<>();
        double rad = config.effectsRange.asDouble(supplier);
        boolean affectsPassive = config.affectsPassive.asBoolean(supplier);
        world.getEntityLookup().forEachIntersects(new Box(pos).expand(rad), entity -> {
          if ((entity instanceof PassiveEntity && affectsPassive)
              || entity instanceof PlayerEntity) {
            entities.add((LivingEntity) entity);
          }
        });
        List<CampfireEffects.Config.Effect> effects = config.effectList;

        for (LivingEntity player : entities) {
          for (CampfireEffects.Config.Effect effect : effects) {
            StatusEffectInstance effectInstance = new StatusEffectInstance(
                effect.identifier, 200, effect.amplifier.asInt(supplier), true, false, true);
            player.addStatusEffect(effectInstance);
          }
        }
      }
    }
  }
}
