package me.melontini.andromeda.base;

import com.google.common.reflect.ClassPath;
import com.google.gson.JsonObject;
import lombok.CustomLog;
import me.melontini.andromeda.base.annotations.MixinEnvironment;
import me.melontini.andromeda.util.CommonValues;
import me.melontini.andromeda.util.exceptions.MixinVerifyError;
import me.melontini.dark_matter.api.base.reflect.wrappers.GenericField;
import me.melontini.dark_matter.api.base.reflect.wrappers.GenericMethod;
import me.melontini.dark_matter.api.base.util.Utilities;
import me.melontini.dark_matter.api.base.util.mixin.AsmUtil;
import me.melontini.dark_matter.api.base.util.mixin.ExtendablePlugin;
import me.melontini.dark_matter.api.base.util.mixin.IPluginPlugin;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.FabricUtil;
import org.spongepowered.asm.mixin.Mixins;
import org.spongepowered.asm.service.IMixinService;
import org.spongepowered.asm.service.MixinService;
import org.spongepowered.asm.util.Annotations;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.util.List;
import java.util.Set;

@CustomLog
public class MixinProcessor {

    public static boolean checkNode(ClassNode n) {
        boolean load = true;
        AnnotationNode envNode = Annotations.getVisible(n, MixinEnvironment.class);
        if (envNode != null) {
            EnvType value = AsmUtil.getAnnotationValue(envNode, "value", null);
            if (value != null) {
                if (value != CommonValues.environment()) return false;
            }
        }

        if (FabricLoader.getInstance().isDevelopmentEnvironment()) verifyMixin(n, n.name);

        return load;
    }

    private static void verifyMixin(ClassNode mixinNode, String mixinClassName) {
        LOGGER.debug("Verifying access flags!");
        if ((mixinNode.access & Modifier.PUBLIC) == Modifier.PUBLIC) {
            throw new MixinVerifyError("Public Mixin! " + mixinClassName);
        }
    }

    private static final ThreadLocal<InputStream> CONFIG = ThreadLocal.withInitial(() -> null);
    private static boolean done = false;

    public static void addMixins() {
        if (done) return;
        IMixinService service = MixinService.getService();
        MixinProcessor.injectService(service);
        ModuleManager.get().loaded().forEach((module) -> {
            JsonObject config = createConfig(module);

            String cfg = "andromeda$$" + module.id().replace('/', '.') + ".mixins.json";
            try (ByteArrayInputStream bais = new ByteArrayInputStream(config.toString().getBytes())) {
                CONFIG.set(bais);
                Mixins.addConfiguration(cfg);
                ModuleManager.get().mixinConfigs.put(cfg, module.id());
            } catch (IOException e) {
                throw new IllegalStateException("Couldn't inject mixin config for module '%s'".formatted(module.id()));
            } finally {
                CONFIG.remove();
            }
        });
        MixinProcessor.dejectService(service);
        done = true;

        Mixins.getConfigs().forEach(config1 -> {
            if (ModuleManager.get().mixinConfigs.containsKey(config1.getName())) {
                config1.getConfig().decorate(FabricUtil.KEY_MOD_ID, "andromeda");
            }
        });
    }

    public static JsonObject createConfig(Module<?> module) {
        JsonObject object = new JsonObject();
        object.addProperty("required", true);
        object.addProperty("minVersion", "0.8");
        object.addProperty("package", module.mixins());
        object.addProperty("compatibilityLevel", "JAVA_17");
        object.addProperty("plugin", Plugin.class.getName());
        object.addProperty("refmap", "andromeda-refmap.json");
        JsonObject injectors = new JsonObject();
        injectors.addProperty("defaultRequire", 1);
        object.add("injectors", injectors);

        module.acceptMixinConfig(object);

        return object;
    }

    private static final GenericMethod<?, MixinService> GET_INSTANCE = GenericMethod.of(MixinService.class, "getInstance");
    private static final GenericField<MixinService, IMixinService> SERVICE = GenericField.of(MixinService.class, "service");

    public static void injectService(IMixinService currentService) {
        IMixinService service = (IMixinService) Proxy.newProxyInstance(MixinProcessor.class.getClassLoader(), new Class[]{IMixinService.class}, (proxy, method, args) -> {
            if (method.getName().equals("getResourceAsStream")) {
                if (args[0] instanceof String s) {
                    if (s.startsWith("andromeda$$")) {
                        return CONFIG.get();
                    }
                }
            }

            return method.invoke(currentService, args);
        });
        Utilities.runUnchecked(() -> {
            MixinService serviceProxy = GET_INSTANCE.accessible(true).invoke(null);
            SERVICE.accessible(true).set(serviceProxy, service);
        });
    }

    public static void dejectService(IMixinService realService) {
        Utilities.runUnchecked(() -> {
            MixinService serviceProxy = GET_INSTANCE.accessible(true).invoke(null);
            SERVICE.accessible(true).set(serviceProxy, realService);
        });
    }

    @SuppressWarnings("UnstableApiUsage")
    public static class Plugin extends ExtendablePlugin {
        private String mixinPackage;

        @Override
        protected void collectPlugins(Set<IPluginPlugin> plugins) {
            plugins.add(DefaultPlugins.constructDummyPlugin());
        }

        protected void onPluginLoad(String mixinPackage) {
            this.mixinPackage = mixinPackage;
        }

        protected void getMixins(List<String> mixins) {
            ClassPath p = Utilities.supplyUnchecked(() -> ClassPath.from(Plugin.class.getClassLoader()));

            p.getTopLevelClassesRecursive(this.mixinPackage).stream()
                    .map(ClassPath.ClassInfo::getName)
                    .map((s) -> Utilities.supplyUnchecked(() -> MixinService.getService().getBytecodeProvider().getClassNode(s.replace('.', '/'))))
                    .filter(MixinProcessor::checkNode)
                    .map((n) -> n.name.replace('/', '.').substring((this.mixinPackage + ".").length()))
                    .forEach(mixins::add);
        }
    }
}
