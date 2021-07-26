package nz.pumbas.halpbot.customparameters.math;

public class Vector
{

    private final double[] values;

    public Vector(double[] values) {
        this.values = values;
    }

    public double getComponent(int index)
    {
        return this.values[index];
    }

    public double[] getValues()
    {
        return this.values;
    }

    public int getComponents()
    {
        return this.values.length;
    }

    public double getSquareMagnitude() {
        double sqrMagnitude = 0;
        for (double value : this.values) {
            sqrMagnitude += value * value;
        }
        return sqrMagnitude;
    }

    public double getMagnitude() {
        return Math.sqrt(this.getSquareMagnitude());
    }

    public Vector divide(double divisor) {
        double[] newValues = new double[this.getComponents()];
        for (int i = 0; i < this.getComponents(); i++) {
            newValues[i] = this.values[i] / divisor;
        }

        return new Vector(newValues);
    }

    public Vector multiply(double multiplier) {
        double[] newValues = new double[this.getComponents()];
        for (int i = 0; i < this.getComponents(); i++) {
            newValues[i] = this.values[i] * multiplier;
        }

        return new Vector(newValues);
    }

    public Vector add(Vector other) {
        if (this.getComponents() != other.getComponents())
            throw new IllegalArgumentException(
                String.format("The vector %s doesn't have the same number of components as the vector %s",
                    other, this));

        double[] newValues = new double[this.getComponents()];
        for (int i = 0; i < this.getComponents(); i++) {
            newValues[i] = this.values[i] + other.getComponent(i);
        }
        return new Vector(newValues);
    }

    public Vector subtract(Vector other) {
        if (this.getComponents() != other.getComponents())
            throw new IllegalArgumentException(
                String.format("The vector %s doesn't have the same number of components as the vector %s",
                    other, this));

        double[] newValues = new double[this.getComponents()];
        for (int i = 0; i < this.getComponents(); i++) {
            newValues[i] = this.values[i] - other.getComponent(i);
        }
        return new Vector(newValues);
    }

    public Vector getUnitVector() {
        return this.divide(this.getMagnitude());
    }

    public double dot(Vector other) {
        if (this.getComponents() != other.getComponents())
            throw new IllegalArgumentException(
                String.format("The vector %s doesn't have the same number of components as the vector %s",
                    other, this));

        double dotProduct = 0;
        for (int i = 0; i < this.getComponents(); i++) {
            dotProduct += this.values[i] * other.getComponent(i);
        }

        return dotProduct;
    }

    public double getAngleBetween(Vector other) {
        return Math.toDegrees(Math.acos(
            this.dot(other) / (this.getMagnitude() * other.getMagnitude())
        ));
    }

    public Vector getParallelComponent(Vector other) {
        // (u.v/v.v)v
        return other.multiply(this.dot(other)/other.dot(other));
    }

    public Vector getPerpendicularComponent(Vector other) {
        return this.subtract(this.getParallelComponent(other));
    }
}
