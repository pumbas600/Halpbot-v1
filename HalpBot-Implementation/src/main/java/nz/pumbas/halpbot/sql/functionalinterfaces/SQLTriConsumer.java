package nz.pumbas.halpbot.sql.functionalinterfaces;

import java.sql.SQLException;

@FunctionalInterface
public interface SQLTriConsumer<T, R, U>
{
    void accept(T input1, R input2, U input3) throws SQLException;
}
