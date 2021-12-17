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

package nz.pumbas.halpbot.commands;

import org.dockbox.hartshorn.core.annotations.service.Service;

import nz.pumbas.halpbot.commands.annotations.Command;
import nz.pumbas.halpbot.customparameters.math.Vector3;

@Service
public class VectorCommands
{

    @Command(alias = "VAdd", description = "Adds 2 vectors")
    public Vector3 onAdd(Vector3 a, Vector3 b) {
        return a.add(b);
    }

    @Command(alias = { "VSubtract", "VSub" }, description = "Subtracts a vector from another")
    public Vector3 onSubtract(Vector3 a, Vector3 b) {
        return a.subtract(b);
    }

    @Command(alias = "VMultiply", description = "Multiplies a vector by a constant")
    public Vector3 onMultiply(Vector3 a, double b) {
        return a.multiply(b);
    }

    @Command(alias = "VDivide", description = "Divides a vector by a constant")
    public Vector3 onDivide(Vector3 a, double b) {
       return a.divide(b);
    }

    @Command(alias = "VUnit", description = "Determines the unit vector")
    public Vector3 onUnitVector(Vector3 a) {
        return a.getUnitVector();
    }

    @Command(alias = "VMagnitude", description = "Determines the magnitude of the vector")
    public double onMagnitude(Vector3 a) {
        return a.getMagnitude();
    }

    @Command(alias = "VDot", description = "Determines the dot product of the two vectors")
    public double onDot(Vector3 a, Vector3 b) {
        return a.dot(b);
    }

    @Command(alias = "VAngle", description = "Finds the angle between two vectors in degrees")
    public double onAngle(Vector3 a, Vector3 b) {
        return a.getAngleBetween(b);
    }

    @Command(alias = "VCross", description = "Determines the cross product of the two vectors")
    public Vector3 onCross(Vector3 a, Vector3 b) {
        return a.cross(b);
    }

    @Command(alias = "VProject", description = "Finds the projection of the first vector on to the second vector")
    public Vector3 onProject(Vector3 a, Vector3 b) {
        return a.getParallelComponent(b);
    }
}
