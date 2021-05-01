package nz.pumbas.halpbot.commands.steamtables.models;

import nz.pumbas.utilities.maps.ClassMap;
import nz.pumbas.utilities.maps.FieldMap;
import nz.pumbas.halpbot.commands.steamtables.annotations.ModelObject;

@ClassMap(keys = {"displayName", "units"})
@ModelObject(tableName = "saturated")
public class SaturatedSteamModel implements Model
{
    @FieldMap({"Temperature", "Celsius"})
    public double temperature;

    @FieldMap({"Pressure", "MPa"})
    public double pressure;

    @FieldMap({"Specific Volume (L)", "m^3/kg"})
    public double volumeliquid;

    @FieldMap({"Specific Volume (V)", "m^3/kg"})
    public double volumevapour;

    @FieldMap({"Specific Internal Energy (L)", "kJ/kg"})
    public double internalenergyliquid;

    @FieldMap({"Specific Internal Energy (V)", "kJ/kg"})
    public double internalenergyvapour;

    @FieldMap({"Specific Enthalpy (L)", "kJ/kg"})
    public double enthalpyliquid;

    @FieldMap({"Specific Enthalpy (V)", "kJ/kg"})
    public double enthalpyvapour;

    @FieldMap({"Specific Entropy (L)", "kJ/kg.K"})
    public double entropyliquid;

    @FieldMap({"Specific Entropy (V)", "kJ/kg.K"})
    public double entropyvapour;
}
