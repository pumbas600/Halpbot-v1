package nz.pumbas.halpbot.customparameters.electrical;

import nz.pumbas.commands.annotations.Children;

@Children({Resistor.class, PowerSupply.class})
public abstract class ElectricalComponent {

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
        return voltageDrop() * currentAcross();
    }
}
