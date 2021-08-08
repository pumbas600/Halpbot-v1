package nz.pumbas.halpbot.customparameters.electrical;

import nz.pumbas.halpbot.commands.annotations.ParameterConstruction;

public class Resistor extends ElectricalComponent {

    protected double resistance;

    @ParameterConstruction
    public Resistor(double resistance) {
        this.resistance = resistance;
    }

    public double resistance() {
        return this.resistance;
    }

}
