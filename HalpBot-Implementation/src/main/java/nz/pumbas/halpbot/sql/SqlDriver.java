package nz.pumbas.halpbot.sql;

import org.jetbrains.annotations.Nullable;

import java.sql.Connection;
import java.sql.ResultSet;

public interface SqlDriver
{
    @Nullable
    Connection createConnection();

    @Nullable
    ResultSet executePreparedStatement(Connection connection, String sql, Object... parameters);

    @Nullable
    ResultSet executePreparedStatement(String sql, Object... parameters);
}
