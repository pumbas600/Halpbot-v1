package nz.pumbas.halpbot.customparameters.units;

import java.util.function.Function;

public interface UnitConverter
{
    String getTargetUnit();
    String getSourceUnit();

    Function<Double, Double> getConverter();

}
