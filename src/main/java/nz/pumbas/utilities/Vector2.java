package nz.pumbas.utilities;

public class Vector2
{
    private final double x;
    private final double y;

    public Vector2(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public Vector2(double magnitude, double angle, boolean fromX) {
        if (!fromX) //Makes the angle from the x-axis
            angle += Utilities.quarterRotation;
        angle %= Utilities.fullRotation;

//        if (Utilities.quarterRotation < angle) {
//            if (Utilities.halfRotation > angle) {
//                angle = Utilities.halfRotation - angle;
//            }
//            else if (Utilities.threeQuarterRotation > angle) {
//                angle -= Utilities.halfRotation;
//            }
//            else {
//                angle = Utilities.fullRotation - angle;
//            }
//        }

        double radians = Math.toRadians(angle);
        this.x = magnitude * Math.cos(radians);
        this.y = magnitude * Math.sin(radians);
    }

    public double getSqrMagnitude() {
        return this.x * this.x + this.y * this.y;
    }

    public double getMagnitude() {
        return Math.sqrt(this.getSqrMagnitude());
    }

    public double getX()
    {
        return this.x;
    }

    public double getY()
    {
        return this.y;
    }

    @Override
    public String toString() {
        return String.format("%.1fi + %.1fj N", this.getX(), this.getY());
    }
}
