package me.melontini.andromeda.common.client.config;

import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import lombok.CustomLog;
import me.melontini.andromeda.base.AndromedaConfig;
import me.melontini.andromeda.base.Module;
import me.melontini.andromeda.base.ModuleManager;
import me.melontini.andromeda.base.util.Environment;
import me.melontini.andromeda.base.util.Experiments;
import me.melontini.andromeda.base.util.Promise;
import me.melontini.andromeda.base.util.annotations.Origin;
import me.melontini.andromeda.base.util.annotations.SpecialEnvironment;
import me.melontini.andromeda.common.client.OrderedTextUtil;
import me.melontini.andromeda.util.CommonValues;
import me.melontini.andromeda.util.Debug;
import me.melontini.dark_matter.api.base.reflect.Reflect;
import me.melontini.dark_matter.api.base.util.Exceptions;
import me.melontini.dark_matter.api.base.util.MakeSure;
import me.melontini.dark_matter.api.base.util.Support;
import me.melontini.dark_matter.api.base.util.Utilities;
import me.melontini.dark_matter.api.glitter.ScreenParticleHelper;
import me.melontini.dark_matter.api.minecraft.util.TextUtil;
import me.shedaniel.autoconfig.annotation.ConfigEntry;
import me.shedaniel.autoconfig.gui.DefaultGuiProviders;
import me.shedaniel.autoconfig.gui.DefaultGuiTransformers;
import me.shedaniel.autoconfig.gui.registry.GuiRegistry;
import me.shedaniel.clothconfig2.api.AbstractConfigEntry;
import me.shedaniel.clothconfig2.api.AbstractConfigListEntry;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.gui.entries.TooltipListEntry;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.TexturedButtonWidget;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.util.InputUtil;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.util.*;
import java.util.function.Consumer;

import static me.melontini.andromeda.common.client.config.ModMenuIntegration.*;

@CustomLog
public class AutoConfigScreen {

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private static final Optional<Field> saveCallback;
    private static final ThreadLocal<Set<Runnable>> saveQueue = ThreadLocal.withInitial(HashSet::new);

    static {
        saveCallback = Support.fallback("cloth-config", () -> {
            LOGGER.info("Loading ClothConfig support!");
            return Reflect.findField(AbstractConfigEntry.class, "saveCallback").or(() -> {
                LOGGER.error("AutoConfigScreen#saveCallback field is was not found! Selective config saves will not be available!");
                return Optional.empty();
            });
        }, () -> {
            LOGGER.error("AutoConfigScreen class loaded without Cloth Config!");
            return Optional.empty();
        });
    }

    public static Screen getLabScreen(Screen screen) {
        ConfigBuilder builder = ConfigBuilder.create()
                .setParentScreen(screen)
                .setTitle(TextUtil.translatable("config.andromeda.lab.title"))
                .setSavingRunnable(Experiments::save)
                .setDefaultBackgroundTexture(Identifier.tryParse("minecraft:textures/block/amethyst_block.png"));

        GuiRegistry registry = DefaultGuiTransformers.apply(DefaultGuiProviders.apply(new GuiRegistry()));

        ConfigCategory misc = builder.getOrCreateCategory(TextUtil.translatable("config.andromeda.category.main"));
        Arrays.stream(Experiments.Config.class.getFields()).forEach((field) -> {
            String opt = "config.andromeda.lab.option." + field.getName();
            registry.getAndTransform(opt, field, Experiments.get(), Experiments.getDefault(), registry).forEach(e -> {
                setOptionTooltip(e, opt + ".@Tooltip");
                appendEnvInfo(e, field);
                wrapTooltip(e);
                misc.addEntry(e);
            });
        });

        return builder.build();
    }

    private static final Splitter SPLITTER = Splitter.on("-");

    public static Screen get(Screen parent) {
        ConfigBuilder builder = ConfigBuilder.create()
                .setParentScreen(parent)
                .setTitle(TextUtil.translatable("config.andromeda.title", Iterables.get(SPLITTER.split(CommonValues.version()), 0)))
                .setSavingRunnable(AutoConfigScreen::powerSave)
                .setDefaultBackgroundTexture(Identifier.tryParse("minecraft:textures/block/amethyst_block.png"));

        var eb = builder.entryBuilder();

        GuiRegistry registry = DefaultGuiTransformers.apply(DefaultGuiProviders.apply(new GuiRegistry()));

        ModuleManager.get().all().stream().map(Promise::get).forEach(module -> {
            List<Field> fields = new ArrayList<>(MakeSure.notEmpty(Arrays.asList(ModuleManager.get().getConfigClass(module.getClass()).getFields())));
            fields.removeIf(field -> field.isAnnotationPresent(ConfigEntry.Gui.Excluded.class));
            fields.sort(Comparator.comparingInt(value -> !"enabled".equals(value.getName()) ? 1 : 0));

            var category = getOrCreateCategoryForField(module, builder);
            String moduleText = "config.andromeda.%s".formatted(module.meta().dotted());

            if (fields.size() <= 1) {
                registry.getAndTransform(moduleText, fields.get(0), module.config(), module.defaultConfig(), registry)
                        .forEach(e -> {
                            if (checkOptionManager(e, module, fields.get(0))) {
                                setModuleTooltip(e, module);
                                appendEnvInfo(e, module.meta().environment());
                            }
                            appendDeprecationInfo(e, module);
                            appendOrigin(e, module);
                            wrapTooltip(e);
                            wrapSaveCallback(e, module::save);
                            category.addEntry(e);
                        });
            } else {
                List<AbstractConfigListEntry<?>> list = new ArrayList<>();
                fields.forEach((field) -> {
                    String opt = "enabled".equals(field.getName()) ? "config.andromeda.option.enabled" : "config.andromeda.%s.option.%s".formatted(module.meta().dotted(), field.getName());
                    registry.getAndTransform(opt, field, module.config(), module.defaultConfig(), registry).forEach(e -> {
                        if (checkOptionManager(e, module, field)) {
                            setOptionTooltip(e, opt + ".@Tooltip");
                            appendEnvInfo(e, field);
                        }
                        wrapTooltip(e);
                        wrapSaveCallback(e, module::save);
                        list.add(e);
                    });
                });
                var built = eb.startSubCategory(TextUtil.translatable("config.andromeda.%s".formatted(module.meta().dotted())), Utilities.cast(list)).build();
                setModuleTooltip(built, module);
                appendDeprecationInfo(built, module);
                appendOrigin(built, module);
                appendEnvInfo(built, module.meta().environment());
                wrapTooltip(built);
                category.addEntry(built);
            }
        });

        ConfigCategory misc = builder.getOrCreateCategory(TextUtil.translatable("config.andromeda.category.misc"));
        Arrays.stream(AndromedaConfig.Config.class.getFields()).forEach((field) -> {
            String opt = "config.andromeda.base.option." + field.getName();
            registry.getAndTransform(opt, field, AndromedaConfig.get(), AndromedaConfig.getDefault(), registry).forEach(e -> {
                setOptionTooltip(e, opt + ".@Tooltip");
                appendEnvInfo(e, field);
                wrapTooltip(e);
                wrapSaveCallback(e, AndromedaConfig::save);
                misc.addEntry(e);
            });
        });

        Screen screen = builder.build();
        ScreenEvents.AFTER_INIT.register((client, screen1, scaledWidth, scaledHeight) -> {
            if (screen == screen1) {
                var wiki = getWikiButton(client, screen);
                addDrawableChild(screen, wiki);

                var lab = new TexturedButtonWidget(screen.width - 62, 13, 20, 20, 0, 0, 20, LAB_BUTTON_TEXTURE, 32, 64, button -> client.setScreen(AutoConfigScreen.getLabScreen(screen1)));
                lab.setTooltip(Tooltip.of(TextUtil.translatable("config.andromeda.button.lab.tooltip")));
                addDrawableChild(screen, lab);
            }
        });
        return screen;
    }

    @NotNull private static TexturedButtonWidget getWikiButton(MinecraftClient client, Screen screen) {
        var wiki = new TexturedButtonWidget(screen.width - 40, 13, 20, 20, 0, 0, 20, WIKI_BUTTON_TEXTURE, 32, 64, button -> {
            if (InputUtil.isKeyPressed(client.getWindow().getHandle(), InputUtil.GLFW_KEY_LEFT_SHIFT)) {
                Debug.load();
                ScreenParticleHelper.addScreenParticles(ParticleTypes.ANGRY_VILLAGER, screen.width - 30, 23, 0.5, 0.5, 0.5, 1);
                LOGGER.info("Reloaded Debug Keys!");
            } else {
                screen.handleTextClick(WIKI_LINK);
            }
        });
        wiki.setTooltip(Tooltip.of(TextUtil.translatable("config.andromeda.button.wiki")));
        return wiki;
    }

    private static void wrapSaveCallback(AbstractConfigEntry<?> e, Runnable saveFunc) {
        if (saveCallback.isEmpty()) return;
        saveCallback.get().setAccessible(true);
        Consumer<Object> original = (Consumer<Object>) Exceptions.supply(() -> saveCallback.get().get(e));
        if (original == null) return;
        Exceptions.run(() -> saveCallback.get().set(e, (Consumer<Object>) o -> {
            if (e.isEdited()) {
                original.accept(o);
                saveQueue.get().add(saveFunc);
            }
        }));
    }

    private static boolean checkOptionManager(AbstractConfigListEntry<?> e, Module<?> module, Field field) {
        var opt = FeatureBlockade.get().explain(ModuleManager.get(), module, field.getName());
        if (opt.isEmpty()) return true;

        e.setEditable(false);
        if (e instanceof TooltipListEntry<?> t) {
            Optional<Text[]> optional = Optional.of(opt.get().stream().map(text -> {
                if (text instanceof MutableText mt) return mt.formatted(Formatting.RED);
                return text.copy().formatted(Formatting.RED);
            }).toArray(Text[]::new));
            t.setTooltipSupplier(() -> optional);
        }
        return false;
    }

    private static void appendDeprecationInfo(AbstractConfigListEntry<?> e, Module<?> module) {
        if (e instanceof TooltipListEntry<?> t) {
            if (!module.getClass().isAnnotationPresent(Deprecated.class)) return;
            appendText(t, TextUtil.translatable("andromeda.config.tooltip.deprecated").formatted(Formatting.RED));
        }
    }

    private static void appendOrigin(AbstractConfigListEntry<?> e, Module<?> module) {
        if (e instanceof TooltipListEntry<?> t) {
            if (!module.getClass().isAnnotationPresent(Origin.class)) return;
            Origin origin = module.getClass().getAnnotation(Origin.class);
            appendText(t, TextUtil.translatable("andromeda.config.tooltip.origin", origin.mod(), origin.author()).formatted(Formatting.DARK_AQUA));
        }
    }

    private static void appendEnvInfo(AbstractConfigListEntry<?> e, Environment env) {
        if (e instanceof TooltipListEntry<?> t) {
            Text text = TextUtil.translatable("andromeda.config.tooltip.environment." + env.toString().toLowerCase(Locale.ROOT)).formatted(Formatting.YELLOW);
            appendText(t, text);
        }
    }

    private static void appendEnvInfo(AbstractConfigListEntry<?> e, Field f) {
        if (e instanceof TooltipListEntry<?> t && f.isAnnotationPresent(SpecialEnvironment.class)) {
            SpecialEnvironment env = f.getAnnotation(SpecialEnvironment.class);
            Text text = TextUtil.translatable("andromeda.config.tooltip.environment." + env.value().toString().toLowerCase(Locale.ROOT)).formatted(Formatting.YELLOW);
            appendText(t, text);
        }
    }

    private static void appendText(TooltipListEntry<?> t, Text text) {
        var supplier = t.getTooltipSupplier();
        Optional<Text[]> tooltip;
        if (supplier != null) {
            tooltip = Optional.of(supplier.get().map(texts -> ArrayUtils.add(texts, text))
                    .orElseGet(() -> new Text[]{text}));
        } else {
            tooltip = Optional.of(new Text[]{text});
        }
        t.setTooltipSupplier(() -> tooltip);
    }

    private static void setOptionTooltip(AbstractConfigListEntry<?> e, String option) {
        if (e instanceof TooltipListEntry<?> t) {
            if (I18n.hasTranslation(option)) {
                var opt = Optional.of(new Text[]{TextUtil.translatable(option)});
                t.setTooltipSupplier(() -> opt);
                return;
            }
            t.setTooltipSupplier(Optional::empty);
        }
    }

    private static void setModuleTooltip(AbstractConfigListEntry<?> e, Module<?> module) {
        if (e instanceof TooltipListEntry<?> t) {
            String s = "config.andromeda.%s.@Tooltip".formatted(module.meta().dotted());
            if (!I18n.hasTranslation(s)) return;

            var opt = Optional.of(new Text[]{TextUtil.translatable(s)});
            t.setTooltipSupplier(() -> opt);
        }
    }

    private static void wrapTooltip(AbstractConfigListEntry<?> e) {
        if (e instanceof TooltipListEntry<?> t) {
            var supplier = t.getTooltipSupplier();
            if (supplier == null) return;
            var opt = supplier.get().map(texts -> {
                List<Text> wrapped = new ArrayList<>();
                for (Text text : texts) {
                    wrapped.addAll(OrderedTextUtil.wrap(text, 250));
                }
                return wrapped.toArray(Text[]::new);
            });
            t.setTooltipSupplier(() -> opt);
        }
    }

    private static void powerSave() {
        if (saveCallback.isPresent()) {
            saveQueue.get().forEach(Runnable::run);
            saveQueue.get().clear();
            return;
        }
        AndromedaConfig.save();
        ModuleManager.get().all().forEach(future -> future.get().save());
    }

    private static ConfigCategory getOrCreateCategoryForField(Module<?> info, ConfigBuilder screenBuilder) {
        Text key = TextUtil.translatable("config.andromeda.category.%s".formatted(info.meta().category()));
        return screenBuilder.getOrCreateCategory(key);
    }
}
