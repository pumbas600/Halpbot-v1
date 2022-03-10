/*
 * MIT License
 *
 * Copyright (c) 2021 pumbas600
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package net.pumbas.halpbot.customparameters.electrical;

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
