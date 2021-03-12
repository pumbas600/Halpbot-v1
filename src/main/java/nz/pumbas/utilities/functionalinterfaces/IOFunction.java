package nz.pumbas.utilities.functionalinterfaces;

import java.io.IOException;

@FunctionalInterface
public interface IOFunction<T, R>
{
    R apply(T input) throws IOException;
}
