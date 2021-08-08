package nz.pumbas.halpbot.commands;

import nz.pumbas.halpbot.commands.annotations.Command;
import nz.pumbas.halpbot.customparameters.math.Vector3;

public class VectorCommands
{

    @Command(alias = "VAdd", description = "Adds 2 vectors")
    public Vector3 onAdd(Vector3 a, Vector3 b) {
        return a.add(b);
    }

    @Command(alias = "VSubtract", description = "Subtracts a vector from another")
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
