package nz.pumbas.steamtables.models;

import nz.pumbas.steamtables.annotations.Column;
import nz.pumbas.steamtables.annotations.Model;

@Model(tableName = "superheated")
public class SuperheatedSteamModel
{
    @Column(displayName = "Temperature", units = "Celsius")
    public double temperature;

    @Column(displayName = "Specific Volume", units = "m^3/kg")
    public double volume;

    @Column(displayName = "Specific Internal Energy", units = "kJ/kg")
    public double internalenergy;

    @Column(displayName = "Specific Enthalpy", units = "kJ/kg")
    public double enthalpy;

    @Column(displayName = "Specific Entropy", units = "kJ/kg.K")
    public double entropy;
}
