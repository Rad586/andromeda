package me.melontini.andromeda.mixin.entities.flower_duplication;

import me.melontini.andromeda.config.Config;
import me.melontini.andromeda.util.annotations.Feature;
import net.minecraft.block.BlockState;
import net.minecraft.block.TallFlowerBlock;
import net.minecraft.item.BoneMealItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BoneMealItem.class)
@Feature({"beeFlowerDuplication", "beeTallFlowerDuplication"})
class BoneMealItemMixin {
    @Inject(at = @At("HEAD"), method = "useOnFertilizable", cancellable = true)
    private static void andromeda$useOnFertilizable(ItemStack stack, World world, BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        if (!Config.get().beeTallFlowerDuplication) return;

        BlockState blockState = world.getBlockState(pos);
        if (blockState.getBlock() instanceof TallFlowerBlock) {
            if (!world.isClient) {
                if (Config.get().unknown && world.random.nextInt(100) == 0) {
                    world.createExplosion(null,
                            pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 3.0F,
                            false, World.ExplosionSourceType.BLOCK);
                }
            }
            cir.setReturnValue(false);
        }
    }
}
