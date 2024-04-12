package me.melontini.andromeda.util;

import com.google.common.collect.Maps;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.SneakyThrows;
import me.melontini.andromeda.base.Module;
import me.melontini.andromeda.modules.misc.translations.Translations;
import me.melontini.dark_matter.api.base.util.Utilities;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

public class EarlyLanguage {

    private static final String DEFAULT = "en_us";
    private static final String LOCALE = Utilities.supply(() -> {
        var locale = Locale.getDefault();
        return locale.getLanguage() + "_" + locale.getCountry().toLowerCase(Locale.ROOT);
    });
    private static final Pattern TOKEN_PATTERN = Pattern.compile("%(\\d+\\$)?[\\d.]*[df]");

    private static Map<String, String> defaultTranslations;
    private static Map<String, String> translations;

    public static void load() {
        defaultTranslations = load(DEFAULT);
        Map<String, String> map = LOCALE.equals(DEFAULT) ? Collections.emptyMap() : load(LOCALE);
        translations = map.isEmpty() ? defaultTranslations : map;
    }

    public static String translate(String key, Object... args) {
        String translated = translations.get(key);
        if (translated == null) return defaultTranslations.getOrDefault(key, key);
        if (args.length == 0) return translated;
        return translated.formatted(args);
    }

    public static String translate(Module<?> module, String key, Object... args) {
        return translate("andromeda.%s.%s".formatted(module.meta().dotted(), key), args);
    }

    @SneakyThrows
    private static Map<String, String> load(String locale) {
        var opt = getPath(locale);
        if (opt.isEmpty()) return Collections.emptyMap();
        JsonObject object = JsonParser.parseReader(Files.newBufferedReader(opt.get())).getAsJsonObject();

        return Maps.transformValues(object.asMap(), input -> TOKEN_PATTERN.matcher(input.getAsString()).replaceAll("%$1s"));
    }

    private static Optional<Path> getPath(String locale) {
        var path = Translations.LANG_PATH.resolve(locale + ".json");
        if (Files.exists(path)) return Optional.of(path);
        return CommonValues.mod().findPath("assets/andromeda/lang/" + locale + ".json");
    }
}
