package me.melontini.andromeda.modules.misc.unknown.client;

import me.melontini.andromeda.modules.misc.unknown.RoseOfTheValley;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.minecraft.client.render.RenderLayer;

public final class Client {

  Client() {
    RoseOfTheValley.ROSE_OF_THE_VALLEY_BLOCK.ifPresent(
        b -> BlockRenderLayerMap.INSTANCE.putBlocks(RenderLayer.getCutout(), b));
  }
}
