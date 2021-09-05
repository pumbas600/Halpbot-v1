package nz.pumbas.halpbot.sql;

import org.jetbrains.annotations.Nullable;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import nz.pumbas.halpbot.sql.functionalinterfaces.SQLConsumer;

public interface SQLDriver
{
    @Nullable
    Connection createConnection();

    PreparedStatement createStatement(Connection connection, String sql, Object... parameters)
        throws SQLException;

    void onLoad(SQLConsumer<Connection> consumer);

    default int executeUpdate(Connection connection, String sql, Object... parameters)
        throws SQLException {
        PreparedStatement statement = this.createStatement(connection, sql, parameters);
        int effectedRows = statement.executeUpdate();
        statement.close();
        return effectedRows;
    }

    default ResultSet executeQuery(Connection connection, String sql, Object... parameters)
        throws SQLException {
        PreparedStatement statement = this.createStatement(connection, sql, parameters);
        ResultSet resultSet = statement.executeQuery();
        statement.closeOnCompletion();
        return resultSet;
    }
}
