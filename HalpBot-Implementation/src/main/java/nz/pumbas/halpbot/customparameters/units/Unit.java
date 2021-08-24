package nz.pumbas.halpbot.customparameters.units;

import java.util.function.Function;

import nz.pumbas.halpbot.converters.Converters;
import nz.pumbas.halpbot.converters.TypeConverter;
import nz.pumbas.halpbot.objects.Exceptional;

public class Unit
{
    static {
        System.out.println("Unit Loaded");
    }

    // Created from 100MPa
    // Unit.in(Giga)
    // Unit.as(Kelvin)

    private String baseUnit;
    private Prefix prefix;
    private double value;

    public Unit(double value, Prefix prefix, String baseUnit) {
        this.value = value;
        this.prefix = prefix;
        this.baseUnit = baseUnit;
    }

    public Unit to(Prefix newPrefix) {
        this.value *= this.prefix.getMultiplier() / newPrefix.getMultiplier();
        this.prefix = newPrefix;
        return this;
    }

    public Unit as(String newBaseUnit) {
        return this.as(newBaseUnit, Prefix.DEFAULT);
    }

    public Unit as(String newBaseUnit, Prefix newPrefix) {
        Function<Double, Double> converter = UnitManager.getConverter(this.baseUnit, newBaseUnit);
        this.to(Prefix.DEFAULT);
        this.value = converter.apply(this.value);
        this.baseUnit = newBaseUnit;
        this.to(newPrefix);
        return this;
    }

    @Override
    public String toString() {
        return this.value + this.prefix.getPrefix() + this.baseUnit;
    }
}
