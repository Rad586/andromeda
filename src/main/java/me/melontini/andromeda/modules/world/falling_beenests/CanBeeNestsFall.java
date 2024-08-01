package me.melontini.andromeda.modules.world.falling_beenests;

import me.melontini.andromeda.base.Module;
import me.melontini.andromeda.base.util.Environment;
import me.melontini.andromeda.base.util.annotations.ModuleInfo;
import me.melontini.andromeda.base.util.config.ConfigDefinition;
import me.melontini.andromeda.base.util.config.ConfigState;
import me.melontini.andromeda.base.util.config.GameConfig;

@ModuleInfo(name = "falling_beenests", category = "world", environment = Environment.SERVER)
public final class CanBeeNestsFall extends Module {

  public static final ConfigDefinition<GameConfig> CONFIG =
      new ConfigDefinition<>(() -> GameConfig.class);

  CanBeeNestsFall() {
    this.defineConfig(ConfigState.GAME, CONFIG);
  }
}
