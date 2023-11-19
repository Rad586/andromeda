package me.melontini.andromeda.base;

import me.melontini.andromeda.config.BasicConfig;
import me.melontini.andromeda.registries.Common;
import me.melontini.andromeda.base.annotations.FeatureEnvironment;
import me.melontini.dark_matter.api.base.util.Utilities;
import me.melontini.dark_matter.api.config.ConfigManager;

@SuppressWarnings("UnstableApiUsage")
public interface Module {

    default void onClient() {
        try {
            Class<?> cls = Class.forName(this.getClass().getPackageName() + ".client.Client");
            Common.bootstrap(cls);
        } catch (ClassNotFoundException ignored) { }
    }
    default void onServer() { }
    default void onMain() {
        try {
            Class<?> cls = Class.forName(this.getClass().getPackageName() + ".Content");
            Common.bootstrap(cls);
        } catch (ClassNotFoundException ignored) { }
    }
    default void onPreLaunch() { }

    default Environment environment() {
        FeatureEnvironment env = this.getClass().getAnnotation(FeatureEnvironment.class);
        if (env != null) return env.value();
        return Environment.BOTH;
    }
    default Class<? extends BasicConfig> configClass() { return BasicConfig.class; }

    default <T extends BasicConfig> ConfigManager<T> manager() { return Utilities.cast(ModuleManager.get().getConfig(this.getClass())); }
    default <T extends BasicConfig> T config(Class<T> cls) {return Utilities.cast(manager().getConfig());}
    default <T extends BasicConfig> T config() {return Utilities.cast(manager().getConfig());}
    default boolean enabled() { return manager().get(boolean.class, "enable"); }
}
