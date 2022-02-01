package nz.pumbas.halpbot.objects;


import java.util.concurrent.TimeUnit;

public record AsyncDuration(long value, TimeUnit unit)
{
    public boolean isZero() {
        return this.value == 0;
    }

    public boolean isNegative() {
        return this.value < 0;
    }
}
