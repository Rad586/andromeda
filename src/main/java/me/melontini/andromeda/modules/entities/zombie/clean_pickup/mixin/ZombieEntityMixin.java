package me.melontini.andromeda.modules.entities.zombie.clean_pickup.mixin;

import static java.util.Objects.requireNonNull;
import static me.melontini.andromeda.modules.mechanics.throwable_items.data.ItemBehaviorManager.RELOADER;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import me.melontini.andromeda.base.ModuleManager;
import me.melontini.andromeda.common.util.ConstantLootContextAccessor;
import me.melontini.andromeda.modules.entities.zombie.clean_pickup.Pickup;
import me.melontini.andromeda.modules.entities.zombie.clean_pickup.PickupTag;
import me.melontini.andromeda.modules.mechanics.throwable_items.ThrowableItems;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ZombieEntity.class)
abstract class ZombieEntityMixin extends HostileEntity {

  protected ZombieEntityMixin(EntityType<? extends HostileEntity> entityType, World world) {
    super(entityType, world);
  }

  @ModifyExpressionValue(
      at =
          @At(
              value = "INVOKE",
              target =
                  "Lnet/minecraft/entity/mob/HostileEntity;canPickupItem(Lnet/minecraft/item/ItemStack;)Z"),
      method = "canPickupItem")
  public boolean andromeda$canPickupItem(boolean original, ItemStack stack) {
    if (world.am$get(Pickup.CONFIG).available.asBoolean(ConstantLootContextAccessor.get(this))) {
      return original
          && (stack.isIn(PickupTag.ZOMBIES_PICKUP)
              || ModuleManager.get()
                  .getModule(ThrowableItems.class)
                  .map(m -> handleThrowableItems(m, world, stack))
                  .orElse(false));
    }
    return original;
  }

  @Unique private boolean handleThrowableItems(ThrowableItems m, World world, ItemStack stack) {
    return world.am$get(ThrowableItems.CONFIG).canZombiesThrowItems
        && requireNonNull(world.getServer()).dm$getReloader(RELOADER).hasBehaviors(stack);
  }
}
