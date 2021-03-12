package nz.pumbas.utilities;

public class Vector2
{
    private final float x;
    private final float y;

    public Vector2(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public Vector2(float magnitude, float angle, boolean fromX) {
        angle %= Utilities.fullCircle;

        if (Utilities.quarterCircle < angle) {
            if (Utilities.halfCircle > angle) {
                angle = Utilities.halfCircle - angle;
            }
            else if (Utilities.threeQuarterCircle > angle) {
                angle -= Utilities.halfCircle;
            }
            else {
                angle = Utilities.fullCircle - angle;
            }
        }

        double radians = angle * Math.PI / Utilities.halfCircle;
        if (fromX) {
            this.x = magnitude * (float)Math.cos(radians);
            this.y = magnitude * (float)Math.sin(radians);
        }
        else {
            this.x = magnitude * (float)Math.sin(radians);
            this.y = magnitude * (float)Math.cos(radians);
        }

    }

    public float getSqrMagnitude() {
        return this.x * this.x + this.y * this.y;
    }

    public float getMagnitude() {
        return (float)Math.sqrt(this.getSqrMagnitude());
    }

    public float getX()
    {
        return this.x;
    }

    public float getY()
    {
        return this.y;
    }

    @Override
    public String toString() {
        return String.format("%.1fi + %.1fj N", this.getX(), this.getY());
    }
}
