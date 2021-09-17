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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import nz.pumbas.halpbot.sql.functionalinterfaces.SQLConsumer;
import nz.pumbas.halpbot.utilities.HalpbotUtils;

public interface SQLDriver
{
    static SQLDriver of(String database) {
        return HalpbotUtils.context().get(SQLManager.class)
            .getDriver(database);
    }

    static SQLDriver of(String database, SQLConsumer<SQLDriver> creationListener) {
        return HalpbotUtils.context().get(SQLManager.class)
            .getDriver(database, creationListener);
    }

    @Nullable
    Connection createConnection();

    default PreparedStatement createStatement(
            Connection connection,
            String sql,
            Object... parameters
    ) throws SQLException
    {
        PreparedStatement statement = connection.prepareStatement(sql);
        this.populateStatement(statement, parameters);
        return statement;
    }

    default void populateStatement(PreparedStatement statement, List<Object> parameters) throws SQLException {
        for (int i = 0; i < parameters.size(); i++) {
            Object parameter = parameters.get(i);
            SQLUtils.getResultSetter(parameter)
                .accept(statement, i + 1, parameter);
        }
    }

    default void populateStatement(PreparedStatement statement, Object... parameters) throws SQLException {
        this.populateStatement(statement, List.of(parameters));
    }



    void onLoad(SQLConsumer<Connection> consumer);

    void reload();

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
