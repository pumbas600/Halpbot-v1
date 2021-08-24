package nz.pumbas.halpbot.customparameters.units;

import java.util.HashMap;
import java.util.Map;

public enum Prefix
{
    MILLI("m", 1e-3),
    DEFAULT("", 1),
    KILO("k", 1e3),
    MEGA("M", 1e6),
    GIGA("G", 1e9);

    private final String prefix;
    private final double multiplier;

    private static final Map<Character, Prefix> prefixes = new HashMap<>();

    static {
        for (Prefix prefix : values()) {
            if (!prefix.getPrefix().isEmpty())
                prefixes.put(prefix.getPrefix().charAt(0), prefix);
        }
    }

    Prefix(String prefix, double multiplier) {
        this.prefix = prefix;
        this.multiplier = multiplier;
    }

    public String getPrefix() {
        return this.prefix;
    }

    public double getMultiplier() {
        return this.multiplier;
    }

    public static boolean isPrefix(char prefix) {
        return prefixes.containsKey(prefix);
    }

    public static Prefix getPrefix(char prefix) {
        return prefixes.getOrDefault(prefix, DEFAULT);
    }
}
