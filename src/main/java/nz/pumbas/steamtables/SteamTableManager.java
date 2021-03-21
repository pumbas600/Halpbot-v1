package nz.pumbas.steamtables;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import javax.xml.transform.Result;

import nz.pumbas.commands.ErrorManager;
import nz.pumbas.commands.Exceptions.ErrorMessageException;
import nz.pumbas.steamtables.models.ModelHelper;
import nz.pumbas.steamtables.models.SaturatedSteamModel;
import nz.pumbas.utilities.Utilities;
import nz.pumbas.utilities.functionalinterfaces.SQLFunction;

public class SteamTableManager
{
    public Connection connect() {
        Connection connection = null;
        try {
            connection = DriverManager.getConnection("jdbc:sqlite:steamtables.sqlite");
        } catch (SQLException e) {
            ErrorManager.handle(e, "There was an error establishing a connection to the steamtables");
        }
        return connection;
    }

    public void insertRecord(SteamInserts insertStatement, List<Double> records) {
        if (null == records) return;

        try {
            Connection connection = this.connect();
            PreparedStatement statement = connection.prepareStatement(insertStatement.getSql());
            statement.setDouble(1, records.get(0));
            statement.setDouble(2, records.get(1));
            statement.setDouble(3, records.get(2));
            statement.setDouble(4, records.get(3));
            statement.setDouble(5, records.get(4));
            statement.setDouble(6, records.get(5));
            statement.setDouble(7, records.get(6));
            statement.setDouble(8, records.get(8));
            statement.setDouble(9, records.get(9));
            statement.setDouble(10, records.get(11));
            statement.executeUpdate();
        } catch (SQLException e) {
            records.forEach(System.out::println);
            ErrorManager.handle(e);
        }
    }

    public Optional<ResultSet> selectSaturatedRecord(String selectColumn, String whereColumn, double value) {
        selectColumn = selectColumn.toLowerCase();
        whereColumn = whereColumn.toLowerCase();

        String sql = "SELECT * FROM saturated WHERE " + whereColumn + " = ?";

        try (Connection connection = this.connect()) {
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setDouble(1, value);

            ResultSet result = statement.executeQuery();

            return Optional.of(result);
        } catch (SQLException e) {
            ErrorManager.handle(e);
        }
        return Optional.empty();
    }

    private String selectColumn(Connection connection, String columnAlias) throws SQLException
    {
        String sql = "SELECT columnname FROM columns WHERE columnalias = ?";

        PreparedStatement statement = connection.prepareStatement(sql);
        statement.setString(1, columnAlias.toLowerCase());
        ResultSet result = statement.executeQuery();

        if (result.next())
            return result.getString("columnname");
        else throw new ErrorMessageException(
            String.format("The column name '%s' is not valid", columnAlias));
    }

    public double selectRecord(Class<?> model, String selectColumn, String whereColumn, double value) {
        try (Connection connection = this.connect()) {
            selectColumn = this.selectColumn(connection, selectColumn);
            whereColumn = this.selectColumn(connection, whereColumn);

            String sql = "SELECT " + selectColumn + " FROM " + ModelHelper.getTableName(model) +
                " WHERE " + whereColumn + " = ? LIMIT 1";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setDouble(1, value);

            ResultSet result = statement.executeQuery();

            if (result.next())
                return result.getDouble(selectColumn);
            else {
                //Find between
            }


        } catch (SQLException e) {
            ErrorManager.handle(e);
        }
        return -1;
    }

    private <T> Optional<T> selectRecordBetween(Class<T> model, String selectColumn, String whereColumn, double min,
                                         double max)
    {
        try (Connection connection = this.connect()) {
            selectColumn = this.selectColumn(connection, selectColumn);
            whereColumn = this.selectColumn(connection, whereColumn);

            String sql = "SELECT * FROM " + ModelHelper.getTableName(model) +
                " WHERE " + whereColumn + " BETWEEN ? AND ?";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setDouble(1, min);
            statement.setDouble(2, max);

            ResultSet result = statement.executeQuery();

            List<T> results = new ArrayList<>();
            while (result.next())
                results.add(this.parseModel(model, result));

            if (!results.isEmpty()) {
                //Find closest
            }
        } catch (SQLException e) {
            ErrorManager.handle(e);
        }

        return Optional.empty();
    }

    private <T> T parseModel(Class<T> clazz, ResultSet result) {
        T model = Utilities.createInstance(clazz);

        ModelHelper.getColumnNames(clazz).forEach(n -> {
            try {
                Field field = model.getClass().getField(n);
                field.setAccessible(true);
                field.set(model, Utilities.TypeParsers.get(field.getType()).apply(result.getString(n)));
            } catch (NoSuchFieldException | SQLException | IllegalAccessException e) {
                e.printStackTrace();
            }
        });
        return model;
    }
}
