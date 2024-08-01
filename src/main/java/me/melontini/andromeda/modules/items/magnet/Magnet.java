package me.melontini.andromeda.modules.items.magnet;

import me.melontini.andromeda.base.Module;
import me.melontini.andromeda.base.events.InitEvent;
import me.melontini.andromeda.base.util.annotations.ModuleInfo;
import me.melontini.andromeda.base.util.config.ConfigDefinition;
import me.melontini.andromeda.base.util.config.ConfigState;
import me.melontini.andromeda.base.util.config.VerifiedConfig;
import me.melontini.andromeda.util.commander.number.DoubleIntermediary;

@ModuleInfo(name = "magnet", category = "items")
public final class Magnet extends Module {

  public static final ConfigDefinition<Config> CONFIG = new ConfigDefinition<>(() -> Config.class);

  Magnet() {
    this.defineConfig(ConfigState.GAME, CONFIG);
    InitEvent.main(this).listen(() -> () -> MagnetItem.init(this));
  }

  public static class Config extends VerifiedConfig {
    public DoubleIntermediary rangeMultiplier = DoubleIntermediary.of(5);
  }
}
