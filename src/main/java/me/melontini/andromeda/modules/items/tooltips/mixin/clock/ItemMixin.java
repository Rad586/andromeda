package me.melontini.andromeda.modules.items.tooltips.mixin.clock;

import java.util.List;
import me.melontini.andromeda.common.client.AndromedaClient;
import me.melontini.andromeda.modules.items.tooltips.Tooltips;
import me.melontini.dark_matter.api.base.util.MathUtil;
import me.melontini.dark_matter.api.minecraft.util.TextUtil;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Item.class)
abstract class ItemMixin {

  @Inject(at = @At("HEAD"), method = "appendTooltip")
  public void andromeda$tooltip(
      ItemStack stack,
      @Nullable World world,
      List<Text> tooltip,
      TooltipContext context,
      CallbackInfo ci) {
    if (!AndromedaClient.HANDLER.get(Tooltips.CONFIG).clock) return;

    if (world != null && world.isClient) {
      if (stack.getItem() == Items.CLOCK) {
        // totally not stolen from here
        // https://bukkit.org/threads/how-can-i-convert-minecraft-long-time-to-real-hours-and-minutes.122912/
        int i = MathUtil.fastFloor((world.getTimeOfDay() / 1000d + 8) % 24);
        int j = MathUtil.fastFloor(60 * (world.getTimeOfDay() % 1000d) / 1000);
        tooltip.add(
            TextUtil.translatable("tooltip.andromeda.clock", String.format("%02d:%02d", i, j))
                .formatted(Formatting.GRAY));
      }
    }
  }
}
