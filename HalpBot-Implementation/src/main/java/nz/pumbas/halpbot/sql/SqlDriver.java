package nz.pumbas.halpbot.sql;

import org.jetbrains.annotations.Nullable;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import nz.pumbas.halpbot.sql.functionalinterfaces.SQLConsumer;

public interface SqlDriver
{
    @Nullable
    Connection createConnection();

    PreparedStatement createStatement(Connection connection, String sql, Object... parameters)
        throws SQLException;

    void registerLoadInitialision(SQLConsumer<Connection> consumer);
}
