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

package nz.pumbas.halpbot.customparameters.units;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public final class UnitManager
{
    private static final Function<Double, Double> DEFAULT_CONVERTER = d -> d;

    private static final Map<String, Map<String, Function<Double, Double>>> converters = new HashMap<>();

    private UnitManager() {}

    public static void register(UnitConverter converter) {
        if (converters.containsKey(converter.getSourceUnit()))
            converters.put(converter.getSourceUnit(), new HashMap<>());
        converters.get(converter.getSourceUnit())
            .put(converter.getTargetUnit(), converter.getConverter());
    }

    public static Function<Double, Double> getConverter(String sourceUnit, String targetUnit) {
        if (!converters.containsKey(sourceUnit))
            return DEFAULT_CONVERTER;
        if (!converters.get(sourceUnit).containsKey(targetUnit))
            return DEFAULT_CONVERTER;
        return converters.get(sourceUnit).get(targetUnit);
    }
}
