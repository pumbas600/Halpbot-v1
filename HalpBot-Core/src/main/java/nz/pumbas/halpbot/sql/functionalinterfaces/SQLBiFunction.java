package nz.pumbas.halpbot.sql.functionalinterfaces;

import java.sql.SQLException;

@FunctionalInterface
public interface SQLBiFunction<T, U, R>
{
    R accept(T input1, U input2) throws SQLException;
}
