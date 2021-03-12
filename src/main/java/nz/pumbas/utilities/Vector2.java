package nz.pumbas.utilities;

public class Vector2
{
    private final float x;
    private final float y;

    public Vector2(float x, float y) {
        this.x = x;
        this.y = y;
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
}
