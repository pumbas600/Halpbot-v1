package nz.pumbas.steamtables.models;

import nz.pumbas.steamtables.annotations.Column;
import nz.pumbas.steamtables.annotations.Model;

@Model(tableName = "saturated")
public class SaturatedSteamModel
{
    @Column(displayName = "Temperature", units = "Celsius")
    public double temperature;

    @Column(displayName = "Pressure", units = "MPa")
    public double pressure;

    @Column(displayName = "Specific Volume (L)", units = "m^3/kg")
    public double volumeliquid;

    @Column(displayName = "Specific Volume (V)", units = "m^3/kg")
    public double volumevapour;

    @Column(displayName = "Specific Internal Energy (L)", units = "kJ/kg")
    public double internalenergyliquid;

    @Column(displayName = "Specific Internal Energy (V)", units = "kJ/kg")
    public double internalenergyvapour;

    @Column(displayName = "Specific Enthalpy (L)", units = "kJ/kg")
    public double enthalpyliquid;

    @Column(displayName = "Specific Enthalpy (V)", units = "kJ/kg")
    public double enthalpyvapour;

    @Column(displayName = "Specific Entropy (L)", units = "kJ/kg.K")
    public double entropyliquid;

    @Column(displayName = "Specific Entropy (V)", units = "kJ/kg.K")
    public double entropyvapour;
}
