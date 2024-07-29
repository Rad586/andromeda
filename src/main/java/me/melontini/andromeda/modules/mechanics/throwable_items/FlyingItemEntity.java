package me.melontini.andromeda.modules.mechanics.throwable_items;

import static java.util.Objects.requireNonNull;

import me.melontini.andromeda.modules.mechanics.throwable_items.data.ItemBehaviorManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.projectile.thrown.ThrownItemEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.hit.HitResult;
import net.minecraft.world.World;

public class FlyingItemEntity extends ThrownItemEntity {

  public FlyingItemEntity(EntityType<? extends ThrownItemEntity> entityType, World world) {
    super(entityType, world);
    this.setItem(ItemStack.EMPTY);
  }

  public FlyingItemEntity(ItemStack stack, double d, double e, double f, World world) {
    super(Main.FLYING_ITEM.orThrow(), d, e, f, world);
    this.setItem(stack);
  }

  public FlyingItemEntity(ItemStack stack, Entity entity, World world) {
    super(Main.FLYING_ITEM.orThrow(), world);
    this.setOwner(entity);
    this.setItem(stack);
  }

  @Override
  protected void onCollision(HitResult hitResult) {
    if (!this.world.isClient()) {
      for (ItemBehavior behavior : requireNonNull(this.world.getServer())
          .dm$getReloader(ItemBehaviorManager.RELOADER)
          .getBehaviors(getItem().getItem())) {
        if (!this.isRemoved())
          behavior.onCollision(getItem(), this, (ServerWorld) world, getOwner(), hitResult);
      }
    }
    this.discard();
  }

  @Override
  protected Item getDefaultItem() {
    return getItem().getItem();
  }
}
