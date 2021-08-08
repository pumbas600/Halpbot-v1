package nz.pumbas.halpbot.utilities.functionalinterfaces;

import java.sql.SQLException;

@FunctionalInterface
public interface SQLFunction<T, R>
{
    R apply(T input) throws SQLException;
}
