package nz.pumbas.steamtables;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import nz.pumbas.commands.ErrorManager;
import nz.pumbas.commands.Exceptions.ErrorMessageException;

public class SteamTableManager
{
    public static final List<String> Columns = List.of("temperature", "pressure", "volumeliquid", "volumevapour",
        "internalenergyliquid", "internalenergyvapour", "enthalpyliquid", "enthalpyvapour", "entropyliquid",
        "entropyvapour");

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

    public Optional<ResultSet> selectRecord(String selectColumn, String whereColumn, double value) {
        selectColumn = selectColumn.toLowerCase();
        whereColumn = whereColumn.toLowerCase();

        if (!"All".equalsIgnoreCase(selectColumn) && !Columns.contains(selectColumn) || !Columns.contains(whereColumn))
            throw new ErrorMessageException(
                String.format("One of the column names '%s' or '%s' is not valid", selectColumn, whereColumn));

        String sql = "SELECT * FROM saturated WHERE " + whereColumn + " = ?";

        try {
            Connection connection = this.connect();
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setDouble(1, value);

            ResultSet result = statement.executeQuery();

            return Optional.of(result);
        } catch (SQLException e) {
            ErrorManager.handle(e);
        }
        return Optional.empty();
    }
}
