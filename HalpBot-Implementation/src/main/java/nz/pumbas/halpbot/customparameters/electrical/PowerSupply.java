package nz.pumbas.halpbot.customparameters.electrical;

import nz.pumbas.commands.annotations.ParameterConstruction;

public class PowerSupply extends ElectricalComponent {

    protected double voltageRating;

    @ParameterConstruction
    public PowerSupply(double rating) {
        this.voltageRating = rating;
    }


    public double voltageRating() {
        return this.voltageRating;
    }
}
