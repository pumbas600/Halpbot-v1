package nz.pumbas.halpbot.commands.steamtables;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import nz.pumbas.commands.ErrorManager;
import nz.pumbas.commands.exceptions.ErrorMessageException;
import nz.pumbas.halpbot.commands.steamtables.models.ColumnModel;
import nz.pumbas.halpbot.commands.steamtables.models.Model;
import nz.pumbas.halpbot.commands.steamtables.models.ModelHelper;
import nz.pumbas.utilities.enums.Flags;

public class SteamTableManager
{
    private static final double Tolerance = 0.05D;
    private Map<String, ColumnModel> columnMappings;

    public SteamTableManager()
    {
        this.retrieveColumnMappings();
    }

    private void retrieveColumnMappings()
    {
        this.columnMappings = new HashMap<>();
        String sql = "SELECT * FROM columns";

        try (Connection connection = this.connect()) {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(sql);

            ModelHelper.parseModels(ColumnModel.class, resultSet)
                .forEach(columnModel ->
                    this.columnMappings.put(columnModel.columnalias, columnModel)
                );

        } catch (SQLException e) {
            ErrorManager.handle(e);
        }
    }

    public Connection connect()
    {
        Connection connection = null;
        try {
            connection = DriverManager.getConnection("jdbc:sqlite:steamtables.sqlite");
        } catch (SQLException e) {
            ErrorManager.handle(e, "There was an error establishing a connection to the steamtables");
        }
        return connection;
    }

    public Collection<ColumnModel> getColumnMappings()
    {
        return this.columnMappings.values();
    }

    public void insertRecord(SteamInserts insertStatement, List<Double> records)
    {
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

    public String selectColumn(String columnAlias, SteamTable steamTable)
    {
        if (this.columnMappings.containsKey(columnAlias)) {
            ColumnModel columnModel = this.columnMappings.get(columnAlias);
            if (Flags.hasFlag(columnModel.tables, steamTable))
                return columnModel.columnname;
        }
        throw new ErrorMessageException(
            String.format("The column name '%s' is not valid", columnAlias));
    }

    public <T extends Model> Optional<T> selectRecord(Class<T> clazz, String selectColumn, String whereColumn,
                                                      double target)
    {
        SteamTable steamTable = SteamTable.of(clazz);
        selectColumn = this.selectColumn(selectColumn, steamTable);
        whereColumn = this.selectColumn(whereColumn, steamTable);

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
                    (1 - Tolerance) * target, (1 + Tolerance) * target, target);
            }
        } catch (SQLException e) {
            ErrorManager.handle(e);
        }
        return Optional.empty();
    }

    private <T extends Model> Optional<T> selectRecordBetween(Connection connection, Class<T> clazz,
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
