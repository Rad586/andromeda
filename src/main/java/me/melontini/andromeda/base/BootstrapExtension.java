package me.melontini.andromeda.base;

import com.llamalad7.mixinextras.utils.MixinInternals;
import lombok.CustomLog;
import me.melontini.andromeda.util.CrashHandler;
import me.melontini.andromeda.util.exceptions.AndromedaException;
import me.melontini.dark_matter.api.base.util.classes.Context;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.LanguageAdapter;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.MixinEnvironment;
import org.spongepowered.asm.mixin.transformer.ext.IExtension;
import org.spongepowered.asm.mixin.transformer.ext.ITargetClassContext;

@CustomLog
public class BootstrapExtension implements IExtension {

    public static void add() {
        MixinInternals.registerExtension(new BootstrapExtension());
    }

    static boolean done = false;
    @Override
    public boolean checkActive(MixinEnvironment environment) {
        if (!done) {
            try {
                LanguageAdapter.getDefault().create(FabricLoader.getInstance().getModContainer("andromeda").orElseThrow(),
                        "me.melontini.andromeda.base.Bootstrap::onPreLaunch", Runnable.class).run();
            } catch (Throwable t) {
                handleThrowable(t);
            }
            done = true;
        }
        return false;
    }

    private static void handleThrowable(Throwable t) {
        var e = AndromedaException.builder().cause(t).message("Failed to bootstrap Andromeda!").build();
        CrashHandler.handleCrash(e, Context.builder().put("andromeda:skip_service", true).build());
        e.setAppender(b -> b.append("Statuses: ").append(AndromedaException.GSON.toJson(e.getStatuses())));

        LOGGER.error(e);
        System.exit(1);//The game will crash with a billion transformation errors if we don't exit here.
    }

    @Override public void preApply(ITargetClassContext context) {}
    @Override public void postApply(ITargetClassContext context) {}
    @Override public void export(MixinEnvironment env, String name, boolean force, ClassNode classNode) {}
}
