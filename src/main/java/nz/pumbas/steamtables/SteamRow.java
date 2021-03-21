package nz.pumbas.steamtables;

import nz.pumbas.steamtables.annotations.Column;

public class SteamRow
{
    @Column(displayName = "Temperature", units = "Celsius")
    private double temperature;

    @Column(displayName = "Pressure", units = "MPa")
    private double pressure;

    @Column(displayName = "Specific Volume (L)", units = "m^3/kg")
    private double volumeliquid;

    @Column(displayName = "Specific Volume (V)", units = "m^3/kg")
    private double volumevapour;

    @Column(displayName = "Specific Internal Energy (L)", units = "kJ/kg")
    private double internalenergyliquid;

    @Column(displayName = "Specific Internal Energy (V)", units = "kJ/kg")
    private double internalenergyvapour;

    @Column(displayName = "Specific Enthalpy (L)", units = "kJ/kg")
    private double enthalpyliquid;

    @Column(displayName = "Specific Enthalpy (V)", units = "kJ/kg")
    private double enthalpyvapour;

    @Column(displayName = "Specific Entropy (L)", units = "kJ/kg.K")
    private double entropyliquid;

    @Column(displayName = "Specific Entropy (V)", units = "kJ/kg.K")
    private double entropyvapour;
}
