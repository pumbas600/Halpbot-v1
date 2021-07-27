package nz.pumbas.halpbot.commands;

import java.util.List;

import nz.pumbas.commands.annotations.Children;
import nz.pumbas.commands.annotations.Command;
import nz.pumbas.commands.validation.Implicit;
import nz.pumbas.halpbot.customparameters.electrical.CircuitManager;
import nz.pumbas.halpbot.customparameters.electrical.ElectricalComponent;
import nz.pumbas.halpbot.customparameters.electrical.PowerSupply;
import nz.pumbas.halpbot.customparameters.electrical.Resistor;

public class ElectricalCommands
{
    private final CircuitManager manager = new CircuitManager();

    @Command(alias = "solve", description = "Solves a series circuit with the specified electrical components")
    public String solve(
        @Children({PowerSupply.class, Resistor.class}) @Implicit List<ElectricalComponent> components)
    {
        this.manager.solve(components);
        return this.manager.buildCircuitOutput(components);
    }
}
