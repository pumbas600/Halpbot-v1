package nz.pumbas.halpbot.customparameters;

import nz.pumbas.commands.Annotations.ParameterConstruction;

public class Line {

    private final Vector3 point;
    private final Vector3 gradient;

    @ParameterConstruction
    public Line(Vector3 point, Vector3 gradient) {
        this.point = point;
        this.gradient = gradient;
    }

    public static Line ofPoints(Vector3 point1, Vector3 point2) {
        return new Line(point1, point1.subtract(point2));
    }

    public Vector3 getPoint() {
        return this.point;
    }

    public Vector3 getGradient() {
        return this.gradient;
    }

    public Vector3 getPoint(double t) {
        return this.point.add(this.gradient.multiply(t));
    }

    @Override
    public String toString() {
        return String.format("x = %.2f + %.2ft\ny = %.2f + %.2ft\nz = %.2f + %.2ft",
                this.point.getX(), this.gradient.getX(), this.point.getY(), this.gradient.getY(), this.point.getZ(), this.gradient.getZ());
    }
}
