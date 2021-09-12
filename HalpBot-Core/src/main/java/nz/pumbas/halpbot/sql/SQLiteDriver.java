/*
 * MIT License
 *
 * Copyright (c) 2021 pumbas600
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

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
