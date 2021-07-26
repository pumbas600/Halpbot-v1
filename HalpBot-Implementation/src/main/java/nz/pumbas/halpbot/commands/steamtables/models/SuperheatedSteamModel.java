package nz.pumbas.halpbot.commands.steamtables.models;

import nz.pumbas.halpbot.commands.steamtables.annotations.ModelObject;
import nz.pumbas.utilities.maps.ClassMap;
import nz.pumbas.utilities.maps.FieldMap;

@ModelObject(tableName = "superheated")
@ClassMap(keys = {"displayName", "units"})
public class SuperheatedSteamModel implements Model
{
    @FieldMap({"Temperature", "Celsius"})
    public double temperature;

    @FieldMap({"Specific Volume", "m^3/kg"})
    public double volume;

    @FieldMap({"Specific Internal Energy", "kJ/kg"})
    public double internalenergy;

    @FieldMap({"Specific Enthalpy", "kJ/kg"})
    public double enthalpy;

    @FieldMap({"Specific Entropy", "kJ/kg.K"})
    public double entropy;
}
