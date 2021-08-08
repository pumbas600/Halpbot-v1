package nz.pumbas.halpbot.customparameters.electrical;

import nz.pumbas.halpbot.commands.annotations.ParameterConstruction;

public class PowerSupply extends ElectricalComponent {

    @ParameterConstruction
    public PowerSupply(double rating) {
        this.voltageDrop = -rating;
    }

    public double voltageRating() {
        return -this.voltageDrop;
    }
}
