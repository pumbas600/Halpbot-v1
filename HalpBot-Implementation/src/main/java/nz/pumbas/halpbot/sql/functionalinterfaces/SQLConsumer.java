package nz.pumbas.halpbot.sql.functionalinterfaces;

import java.sql.SQLException;

@FunctionalInterface
public interface SQLConsumer<T>
{
    void accept(T input) throws SQLException;
}
