package nz.pumbas.halpbot.utilities.functionalinterfaces;

public interface CheckedFunction<T, R>
{
    R apply(T value) throws Exception;
}
