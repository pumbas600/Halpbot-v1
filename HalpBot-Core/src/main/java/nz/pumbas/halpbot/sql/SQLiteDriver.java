package nz.pumbas.halpbot.sql;

import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import nz.pumbas.halpbot.sql.functionalinterfaces.SQLConsumer;
import nz.pumbas.halpbot.utilities.ErrorManager;
import nz.pumbas.halpbot.utilities.context.LateInit;

public class SQLiteDriver implements SQLDriver, LateInit
{
    private final List<SQLConsumer<Connection>> reloadListeners = new ArrayList<>();
    private final SQLConsumer<SQLDriver> creationListener;
    private final String databaseFilename;

    public SQLiteDriver(String database) {
        this(database, null);
    }

    public SQLiteDriver(String database, SQLConsumer<SQLDriver> creationListener) {
        this.creationListener = creationListener;
        this.databaseFilename = database + ".sqlite";
    }

    /**
     * A late initialisation function that is called after the object has been first constructed.
     */
    @Override
    public void lateInitialisation() {
        try {
            File file = new File(this.databaseFilename);
            if (file.createNewFile() && null != this.creationListener) {
                this.creationListener.accept(this);
            }
        } catch (IOException | SQLException e) {
            ErrorManager.handle(e);
        }
    }

    @Override
    public @Nullable Connection createConnection() {
        Connection connection = null;
        try {
            connection = DriverManager.getConnection(String.format("jdbc:sqlite:%s", this.databaseFilename));
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
            SQLUtils.getResultSetter(parameter.getClass())
                .accept(statement, i + 1, parameter);
        }
        return statement;
    }

    @Override
    public void onLoad(SQLConsumer<Connection> consumer) {
        this.reloadListeners.add(consumer);

        try (Connection connection = this.createConnection()) {
            consumer.accept(connection);
        } catch (SQLException e) {
            ErrorManager.handle(e);
        }
    }

    @Override
    public void reload() {
        try (Connection connection = this.createConnection()) {
            for (SQLConsumer<Connection> reloadListener : this.reloadListeners) {
                reloadListener.accept(connection);
            }
        } catch (SQLException e) {
            ErrorManager.handle(e);
        }
    }
}
