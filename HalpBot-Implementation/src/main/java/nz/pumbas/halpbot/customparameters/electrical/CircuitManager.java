package nz.pumbas.halpbot.customparameters.electrical;

import java.util.List;

public class CircuitManager
{
    public void solve(List<ElectricalComponent> electricalComponents) {
        double totalResistance = this.totalResistance(electricalComponents);
        double totalVoltage = this.totalVoltage(electricalComponents);

        double current = totalVoltage / totalResistance;
        for (ElectricalComponent electricalComponent : electricalComponents) {
            electricalComponent.currentAcross(current);
            if (electricalComponent instanceof Resistor) {
                Resistor resistor = (Resistor) electricalComponent;
                resistor.voltageDrop(current * resistor.resistance());
            }
        }
    }

    public double totalResistance(List<ElectricalComponent> electricalComponents) {
        return electricalComponents.stream()
            .filter(e -> e instanceof Resistor)
            .map(e -> (Resistor) e)
            .mapToDouble(Resistor::resistance)
            .sum();
    }

    public double totalVoltage(List<ElectricalComponent> electricalComponents) {
        return electricalComponents.stream()
            .filter(e -> e instanceof PowerSupply)
            .map(e -> (PowerSupply) e)
            .mapToDouble(PowerSupply::voltageRating)
            .sum();
    }

    public String buildCircuitOutput(List<ElectricalComponent> electricalComponents) {
        StringBuilder builder = new StringBuilder();
        if (!electricalComponents.isEmpty())
            builder.append(electricalComponents.get(0).toString());

        for (int i = 1; i < electricalComponents.size(); i++) {
            builder.append(",\n");
            builder.append(electricalComponents.get(i).toString());
        }

        return builder.toString();
    }
}
