package nz.pumbas.halpbot.sql;

import org.jetbrains.annotations.Nullable;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import nz.pumbas.halpbot.sql.functionalinterfaces.SQLConsumer;
import nz.pumbas.halpbot.sql.functionalinterfaces.SQLTriConsumer;
import nz.pumbas.halpbot.utilities.ErrorManager;
import nz.pumbas.halpbot.utilities.Reflect;

public class SQLiteDriver implements SqlDriver
{

    private static final Map<Class<?>, SQLTriConsumer<PreparedStatement, Integer, Object>> setValueMappings
        = Map.of(
        Boolean.class, (p, i, o) -> p.setBoolean(i, (boolean)o),
        Integer.class,     (p, i, o) -> p.setInt(i, (int) o),
        Double.class,  (p, i, o) -> p.setDouble(i, (double)o),
        String.class,  (p, i, o) -> p.setString(i, (String)o));

    private final List<SQLConsumer<Connection>> loadInitialisations = new ArrayList<>();
    private final String database;
    private final long duration;
    private final TimeUnit timeUnit;

    public SQLiteDriver(String database, long duration, TimeUnit timeUnit) {
        this.database = database;
        this.duration = duration;
        this.timeUnit = timeUnit;
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
    public PreparedStatement createStatement(Connection connection, String sql, Object... parameters)
        throws SQLException {

        PreparedStatement statement = connection.prepareStatement(sql);
        for (int i = 0; i < parameters.length; i++) {
            Object parameter = parameters[i];
            setValueMappings.get(Reflect.wrapPrimative(parameter.getClass()))
                .accept(statement, i + 1, parameter);
        }
        return statement;
    }

    @Override
    public void registerLoadInitialision(SQLConsumer<Connection> consumer) {
        this.loadInitialisations.add(consumer);

        try (Connection connection = this.createConnection()) {
            consumer.accept(connection);
        } catch (SQLException e) {
            ErrorManager.handle(e);
        }
    }
}
