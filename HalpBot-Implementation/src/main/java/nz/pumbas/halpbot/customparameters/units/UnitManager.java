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
