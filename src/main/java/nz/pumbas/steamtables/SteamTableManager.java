package nz.pumbas.steamtables;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import nz.pumbas.commands.ErrorManager;
import nz.pumbas.commands.Exceptions.ErrorMessageException;
import nz.pumbas.steamtables.models.IModel;
import nz.pumbas.steamtables.models.ModelHelper;

public class SteamTableManager
{
    private Map<String, String> columnMappings;

    public SteamTableManager() {
        this.retrieveColumnMappings();
    }

    private void retrieveColumnMappings() {
        this.columnMappings = new HashMap<>();
        String sql = "SELECT * FROM columns";

        try (Connection connection = this.connect()){
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(sql);

            while (resultSet.next()) {
                this.columnMappings.put(resultSet.getString("columnalias"), resultSet.getString("columnname"));
            }

        } catch (SQLException e) {
            ErrorManager.handle(e);
        }
    }

    public Connection connect() {
        Connection connection = null;
        try {
            connection = DriverManager.getConnection("jdbc:sqlite:steamtables.sqlite");
        } catch (SQLException e) {
            ErrorManager.handle(e, "There was an error establishing a connection to the steamtables");
        }
        return connection;
    }

    public Map<String, String> getColumnMappings() {
        return Collections.unmodifiableMap(this.columnMappings);
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

    public String selectColumn(String columnAlias)
    {
        if (this.columnMappings.containsKey(columnAlias))
            return this.columnMappings.get(columnAlias);
        else throw new ErrorMessageException(
            String.format("The column name '%s' is not valid", columnAlias));
    }

    public <T extends IModel> Optional<T> selectRecord(Class<T> clazz, String selectColumn, String whereColumn,
                                  double target) {
        selectColumn = this.selectColumn(selectColumn);
        whereColumn = this.selectColumn(whereColumn);

        try (Connection connection = this.connect()) {
            String sql = "SELECT * FROM " + ModelHelper.getTableName(clazz) +
                " WHERE " + whereColumn + " = ? LIMIT 1";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setDouble(1, target);

            ResultSet result = statement.executeQuery();

            if (result.next())
                return Optional.of(ModelHelper.parseModel(clazz, result));
            else {
                return this.selectRecordBetween(connection, clazz, whereColumn,
                    0.95D * target, 1.05D * target, target);
            }
        } catch (SQLException e) {
            ErrorManager.handle(e);
        }
        return Optional.empty();
    }

    private <T extends IModel> Optional<T> selectRecordBetween(Connection connection, Class<T> clazz,
                                                               String whereColumn, double min,
                                                                double max, double target)
    {
        try {
            String sql = "SELECT * FROM " + ModelHelper.getTableName(clazz) +
                " WHERE " + whereColumn + " BETWEEN ? AND ?";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setDouble(1, min);
            statement.setDouble(2, max);

            ResultSet result = statement.executeQuery();

            List<T> models = ModelHelper.parseModels(clazz, result);

            if (!models.isEmpty()) {
                double smallestDifference = Math.abs(target - models.get(0).getDouble(whereColumn));
                int closestIndex = 0;
                for (int i = 1; i < models.size(); i++) {
                    T model = models.get(i);

                    double difference = Math.abs(target - model.getDouble(whereColumn));
                    if (difference < smallestDifference) {
                        smallestDifference = difference;
                        closestIndex = i;
                    }
                }
                return Optional.of(models.get(closestIndex));
            }
        } catch (SQLException e) {
            ErrorManager.handle(e);
        }

        return Optional.empty();
    }
}
