package me.melontini.andromeda.common.util;

import me.melontini.dark_matter.api.minecraft.debug.ValueTracker;

import java.lang.reflect.Field;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.function.Supplier;


public class DebugTracker {

    public static final Duration FIVE_S = Duration.of(5, ChronoUnit.SECONDS);
    public static final Duration TEN_S = Duration.of(10, ChronoUnit.SECONDS);

    public static void addTracker(String s, Supplier<?> supplier) {
        ValueTracker.addTracker(s, supplier);
    }

    public static void addTracker(String s, Supplier<?> supplier, Duration duration) {
        ValueTracker.addTracker(s, supplier, duration);
    }

    public static void addTracker(String s, Field f, Object o) {
        ValueTracker.addTracker(s, f, o);
    }

    public static void addTracker(String s, Field f, Object o, Duration duration) {
        ValueTracker.addTracker(s, f, o, duration);
    }

    public static void addTracker(String s, Field f) {
        ValueTracker.addTracker(s, f);
    }

    public static void addTracker(String s, Field f, Duration duration) {
        ValueTracker.addTracker(s, f, duration);
    }

    public static void removeTracker(String s) {
        ValueTracker.removeTracker(s);
    }
}
