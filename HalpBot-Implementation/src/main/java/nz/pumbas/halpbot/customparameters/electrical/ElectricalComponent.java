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

    public void voltageDrop(double voltageDrop) {
        this.voltageDrop = voltageDrop;
    }

    public void currentAcross(double currentAcross) {
        this.currentAcross = currentAcross;
    }

    public double powerUsage() {
        return this.voltageDrop() * this.currentAcross();
    }

    @Override
    public String toString() {
        return String.format("%s{voltage-drop=%.4fV, current-across=%.4fA, power-usage=%.4fW}",
            this.getClass().getSimpleName(), this.voltageDrop(), this.currentAcross(), this.powerUsage());
    }
}
