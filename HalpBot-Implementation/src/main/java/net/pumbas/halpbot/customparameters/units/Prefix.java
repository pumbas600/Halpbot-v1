/*
 * MIT License
 *
 * Copyright (c) 2021 pumbas600
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package net.pumbas.halpbot.customparameters.units;

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
