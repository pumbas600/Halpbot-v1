package nz.pumbas.halpbot.utilities;

import org.jetbrains.annotations.NotNull;

public class Int extends Number implements Comparable<Integer>
{
    private int value;

    public Int(int value) {
        this.value = value;
    }

    @Override
    public int compareTo(@NotNull Integer o) {
        return ((Integer) this.value).compareTo(o);
    }

    @Override
    public int intValue() {
        return this.value;
    }

    @Override
    public long longValue() {
        return this.value;
    }

    @Override
    public float floatValue() {
        return this.value;
    }

    @Override
    public double doubleValue() {
        return this.value;
    }

    public void value(int value) {
        this.value = value;
    }

    public int incrementAfter() {
        return this.value++;
    }

    public int incrementBefore() {
        return ++this.value;
    }

    public boolean lessThen(int value) {
        return this.value < value;
    }

    public boolean greaterThen(int value) {
        return this.value > value;
    }

    public boolean lessThenOrEqual(int value) {
        return this.value <= value;
    }

    public boolean greaterThenOrEqual(int value) {
        return this.value >= value;
    }
}
