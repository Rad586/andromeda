package me.melontini.andromeda.util;

import com.google.common.collect.Sets;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import lombok.CustomLog;
import me.melontini.andromeda.base.AndromedaConfig;
import me.melontini.andromeda.util.exceptions.AndromedaException;
import me.melontini.dark_matter.api.base.util.Context;
import me.melontini.dark_matter.api.crash_handler.Crashlytics;
import me.melontini.dark_matter.api.crash_handler.uploading.Mixpanel;
import me.melontini.dark_matter.api.crash_handler.uploading.Uploader;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.impl.util.StringUtil;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.CompletableFuture;

@CustomLog
public class CrashHandler {

    public static final Context.Key<Boolean> SKIP_SERVICE = Context.key("andromeda:skip_service");

    private static final Mixpanel MIXPANEL = Mixpanel.get(new String(Base64.getDecoder().decode("NGQ3YWVhZGRjN2M5M2JkNzhiODRmNDViZWI3Y2NlOTE="), StandardCharsets.UTF_8), true);
    private static final Set<String> IMPORTANT_MODS = Sets.newHashSet("andromeda", "minecraft", "fabric-api", "fabricloader", "connectormod", "forge");

    private static Flag shouldReportRecursive(Throwable cause, Flag flag) {
        if (cause instanceof AndromedaException e) flag.mark(e.shouldReport());
        for (StackTraceElement element : cause.getStackTrace()) {
            if (element.isNativeMethod()) continue;

            if (element.getClassName().startsWith("me.melontini.andromeda")) {
                flag.mark(true);
                break;
            }
        }
        if (cause.getCause() != null) shouldReportRecursive(cause.getCause(), flag);
        return flag;
    }

    public static Optional<JsonObject> traverse(Throwable cause) {
        if (cause instanceof AndromedaException e) {
            JsonObject s = e.getStatuses();
            if (e.getCause() != null) {
                traverse(e.getCause()).ifPresent(object -> s.add("cause", object));
            }
            return Optional.ofNullable(s);
        }
        if (cause.getCause() != null) return traverse(cause.getCause());
        return Optional.empty();
    }

    private static final Set<String> BAD_PREFIXES = Set.of(
            "me.melontini.andromeda.util.exceptions.AndromedaException", //run and Builder.build
            "jdk.internal.reflect.", //Most likely, accessors
            "com.sun.proxy.jdk.", //No source, useless
            "java.lang.invoke.MethodHandleProxies$" //Internal class
    );

    public static void sanitizeTrace(Throwable cause) {
        List<StackTraceElement> e = new ArrayList<>(Arrays.asList(cause.getStackTrace()));

        e.removeIf(el -> BAD_PREFIXES.stream().anyMatch(s -> el.getClassName().startsWith(s)));

        cause.setStackTrace(e.toArray(StackTraceElement[]::new));
        if (cause.getCause() != null) sanitizeTrace(cause.getCause());
    }

    public static void handleCrash(Throwable cause, Context context) {
        if (Debug.Keys.DISABLE_NETWORK_FEATURES.isPresent()) return;

        if (!Debug.Keys.FORCE_CRASH_REPORT_UPLOAD.isPresent()) {
            if (FabricLoader.getInstance().isDevelopmentEnvironment() || !AndromedaConfig.get().sendCrashReports)
                return;
        }

        if (context.get(Crashlytics.Keys.MIXIN_INFO).filter(info -> info.getClassName().startsWith("me.melontini.andromeda")).isEmpty() && !shouldReportRecursive(cause, new Flag()).report())
            return;
        LOGGER.warn("Found Andromeda in trace, collecting and uploading crash report...");

        sanitizeTrace(cause);

        JsonObject object = new JsonObject();
        //fill trace.
        JsonArray stackTrace = new JsonArray();
        for (String string : getCauseAsString(cause).lines().flatMap(s -> StringUtil.wrapLines(s, 190).lines()).toList())
            stackTrace.add(string);
        object.add("stackTrace", stackTrace);

        object.add("statuses", traverse(cause).orElseGet(AndromedaException::defaultStatuses));

        JsonArray mods = new JsonArray();
        for (String importantMod : IMPORTANT_MODS) {
            FabricLoader.getInstance().getModContainer(importantMod).ifPresent(mod -> mods.add(importantMod + " (" + mod.getMetadata().getVersion().getFriendlyString() + ")"));
        }
        object.add("mods", mods);

        if (context.get(SKIP_SERVICE).orElse(false)) {
            upload(object);
        } else {
            CompletableFuture.runAsync(() -> upload(object), Uploader.SERVICE).handle((unused, throwable) -> {
                if (throwable != null) LOGGER.error("Failed to upload crash report!", throwable);
                return null;
            });
        }
    }

    private static void upload(JsonObject object) {
        MIXPANEL.upload(new Mixpanel.Context("Crash", object)).handle((unused, throwable) -> {
            if (throwable != null)
                System.err.printf("Failed to upload crash report! %s: %s%n", throwable.getClass().getSimpleName(), throwable.getMessage());
            return null;
        });
    }

    private static String getCauseAsString(Throwable cause) {
        try(var stringWriter = new StringWriter(); var printWriter = new PrintWriter(stringWriter)) {
            cause.printStackTrace(printWriter);
            return stringWriter.toString();
        } catch (IOException e) {
            return "Failed to get cause: " + e.getMessage();
        }
    }

    private static class Flag {
        @Nullable
        private Boolean report = null;

        private void mark(boolean report) {
            if (this.report == null || this.report) this.report = report;
        }

        public boolean report() {
            return this.report != null && this.report;
        }
    }
}
