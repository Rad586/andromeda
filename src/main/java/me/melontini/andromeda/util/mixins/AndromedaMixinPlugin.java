package me.melontini.andromeda.util.mixins;

import lombok.CustomLog;
import me.melontini.andromeda.util.Debug;
import me.melontini.andromeda.util.EarlyLanguage;
import me.melontini.dark_matter.api.mixin.ExtendablePlugin;
import org.spongepowered.asm.mixin.Mixins;

import java.util.List;

@SuppressWarnings("UnstableApiUsage")
@CustomLog
public class AndromedaMixinPlugin extends ExtendablePlugin {

    private String mixinPackage;

    @Override
    public void onPluginLoad(String mixinPackage) {
        this.mixinPackage = mixinPackage;

        Debug.load();
        EarlyLanguage.load();

        AndromedaMixins.getClassPath().addUrl(this.getClass().getProtectionDomain().getCodeSource().getLocation());

        Mixins.registerErrorHandlerClass(ErrorHandler.class.getName());
    }

    @Override
    protected void getMixins(List<String> mixins) {
        mixins.addAll(AndromedaMixins.discoverInPackage(this.mixinPackage));
    }
}