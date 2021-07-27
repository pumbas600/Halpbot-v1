package nz.pumbas.halpbot.customparameters.electrical;

import nz.pumbas.commands.annotations.Children;

@SuppressWarnings("ClassReferencesSubclass")
@Children({Resistor.class, PowerSupply.class})
public class ElectricalComponent {

    protected double voltageDrop;
    protected double currentAcross;

    public double voltageDrop() {
        return this.voltageDrop;
    }
    public double currentAcross() {
        return this.currentAcross;
    }

    public void setVoltageDrop(double voltageDrop) {
        this.voltageDrop = voltageDrop;
    }

    public void setCurrentAcross(double currentAcross) {
        this.currentAcross = currentAcross;
    }

    public double powerUsage() {
        return this.voltageDrop() * this.currentAcross();
    }

    @Override
    public String toString() {
        return String.format("%s{voltage-drop=%sV, current-across=%sA, power-usage=%sW",
            this.getClass().getSimpleName(), this.voltageDrop(), this.currentAcross(), this.powerUsage());
    }
}
