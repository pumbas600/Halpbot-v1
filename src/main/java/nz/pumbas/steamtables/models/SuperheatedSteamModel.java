package nz.pumbas.steamtables.models;

import nz.pumbas.steamtables.annotations.Model;
import nz.pumbas.utilities.maps.ClassMap;
import nz.pumbas.utilities.maps.FieldMap;

@Model(tableName = "superheated")
@ClassMap(keys = {"displayName", "units"})
public class SuperheatedSteamModel implements IModel
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
