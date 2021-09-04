package nz.pumbas.halpbot.sql;

import org.jetbrains.annotations.Nullable;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

import nz.pumbas.halpbot.sql.functionalinterfaces.SQLTriConsumer;
import nz.pumbas.halpbot.utilities.ErrorManager;

public class SQLiteDriver implements SqlDriver
{

    private static final Map<Class<?>, SQLTriConsumer<PreparedStatement, Integer, Object>> setValueMappings
        = Map.of(
        boolean.class, (p, i, o) -> p.setBoolean(i, (boolean)o),
        int.class,     (p, i, o) -> p.setInt(i, (int) o),
        double.class,  (p, i, o) -> p.setDouble(i, (double)o),
        String.class,  (p, i, o) -> p.setString(i, (String)o));

    private final String database;

    public SQLiteDriver(String database) {
        this.database = database;
    }

    @Override
    public @Nullable Connection createConnection() {
        Connection connection = null;
        try {
            connection = DriverManager.getConnection(
                String.format("jdbc:sqlite:%s.sqlite", this.database));
        } catch (SQLException e) {
            ErrorManager.handle(e);
        }
        return connection;
    }

    @Override
    public @Nullable ResultSet executePreparedStatement(Connection connection, String sql, Object... parameters) {
        try {
            PreparedStatement statement = connection.prepareStatement(sql);
            for (int i = 0; i < parameters.length; i++) {
                Object parameter = parameters[i];
                setValueMappings.get(parameter.getClass())
                    .accept(statement, i + 1, parameter);
            }
            return statement.executeQuery();
        } catch (SQLException e) {
            ErrorManager.handle(e);
        }
        return null;
    }

    @Override
    public @Nullable ResultSet executePreparedStatement(String sql, Object... parameters) {
        try (Connection connection = this.createConnection())
        {
            return this.executePreparedStatement(connection, sql, parameters);
        } catch (SQLException e) {
            ErrorManager.handle(e);
        }
        return null;
    }
}
