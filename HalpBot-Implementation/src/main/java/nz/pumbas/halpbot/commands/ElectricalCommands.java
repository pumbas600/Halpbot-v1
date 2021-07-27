package nz.pumbas.halpbot.commands;

import java.util.List;

import nz.pumbas.commands.annotations.Children;
import nz.pumbas.commands.annotations.Command;
import nz.pumbas.commands.validation.Implicit;
import nz.pumbas.halpbot.customparameters.electrical.ElectricalComponent;
import nz.pumbas.halpbot.customparameters.electrical.PowerSupply;
import nz.pumbas.halpbot.customparameters.electrical.Resistor;

public class ElectricalCommands
{
    @Command(alias = "solve", description = "Solves a series circuit with the specified electrical components")
    public List<ElectricalComponent> solve(
        @Children({PowerSupply.class, Resistor.class}) @Implicit List<ElectricalComponent> components)
    {
        return components;
    }
}
