package nz.pumbas.halpbot.sql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import nz.pumbas.halpbot.objects.Exceptional;
import nz.pumbas.halpbot.sql.table.Table;
import nz.pumbas.halpbot.sql.table.TableRow;
import nz.pumbas.halpbot.sql.table.column.ColumnIdentifier;
import nz.pumbas.halpbot.sql.table.exceptions.IdentifierMismatchException;
import nz.pumbas.halpbot.utilities.ErrorManager;
import nz.pumbas.halpbot.utilities.Reflect;

public class DatabaseTable extends Table
{
    //TODO: Automatically create column identifiers by looking at columns in table
    //TODO: Configure rows that can be ignored when being entered into database. E.g: Id (As this is assigned a
    //      temporary one)

    protected SQLDriver driver;

    protected ColumnIdentifier<?> column;
    protected boolean isUpdated;
    protected List<Object> updatedRows = new ArrayList<>();

    protected String tableName;

    private String insertSql;

    /**
     * Instantiates a new Table with a given set of column identifiers. These identifiers cannot be
     * modified later unless a select action is performed, creating a split copy of this table
     * instance.
     *
     * @param columns
     *     The column identifiers
     */
    public DatabaseTable(ColumnIdentifier<?>... columns) {
        super(columns);
    }

    /**
     * Adds a row to the table if the column identifiers are equal in length and type. If the row has
     * more or less column identifiers it is not accepted into the table. If a row has a column which
     * is not contained in this table it is not accepted into the table.
     *
     * @param row
     *     The row object to add to the table
     *
     * @throws IdentifierMismatchException
     *     When there is a column mismatch between the row and the
     *     table
     */
    @Override
    public void addRow(TableRow row) throws IdentifierMismatchException {
        super.addRow(row);
        this.isUpdated = true;
        this.updatedRows.add(row.value(this.column));
    }

    /**
     * Generates a {@link TableRow} from a given set of objects. The objects should be in the same
     * order as the table's {@link Table#identifiers()}. If the data type of a object does not
     * match up with its expected {@link ColumnIdentifier} a exception is thrown and the row is not
     * inserted into the table.
     *
     * @param values
     *     Objects to "try to" add as rows to the table
     *
     * @throws IllegalArgumentException
     *     When the amount of values does not meet the amount of column
     *     headers, or when the data type of the object does not match the expected type.
     */
    @Override
    public void addRow(Object... values) {
        super.addRow(values);
        this.isUpdated = true;
        int columnIndex = List.of(super.identifiers()).indexOf(this.column);
        this.updatedRows.add(values[columnIndex]);
    }

    public boolean isUnsynced() {
        final String sql = "SELECT count(*) FROM " + this.tableName;
        try (Connection connection = this.driver.createConnection()) {
            int count = this.driver.executeQuery(connection, sql).getInt("count(*)");
            return super.count() != count;
        }
        catch (SQLException e) {
            ErrorManager.handle(e);
        }
        return false;
    }

    private String getInsertSql() {
        if (null == this.insertSql) {
            StringBuilder queryBuilder = new StringBuilder();
            queryBuilder
                .append("INSERT INTO ")
                .append(this.tableName)
                .append(" (");

            if (0 < super.identifiers().length)
                queryBuilder.append(super.identifiers()[0].name());
            for (int i = 1; i < super.identifiers().length; i++) {
                queryBuilder
                    .append(", ")
                    .append(super.identifiers()[i].name());
            }

            queryBuilder.append(") VALUES (");

            if (0 < super.identifiers().length)
                queryBuilder.append("?");
            queryBuilder
                .append(", ?".repeat(Math.max(0, super.identifiers().length - 1)))
                .append(")");

            this.insertSql = queryBuilder.toString();
        }
        return this.insertSql;
    }

    public void addUpdatedColumns() {
        try (Connection connection = this.driver.createConnection();
             PreparedStatement statement = this.driver.createStatement(connection, this.getInsertSql()))
        {
            for (Object identifierValue : this.updatedRows) {
                Exceptional<TableRow> updatedRow = super
                    .where(this.column, Reflect.cast(identifierValue))
                    .first();

                if (updatedRow.present()) {
                    this.driver.populateStatement(statement, updatedRow.get().values());
                    statement.addBatch();
                }
            }
            statement.executeBatch();
        }
        catch (SQLException e) {
            ErrorManager.handle(e);
        }
    }

    public void loadDatabase() {
        final String sql = "SELECT * FROM " + this.tableName;
        try (Connection connection = this.driver.createConnection()) {
            ResultSet resultSet = this.driver.executeQuery(connection, sql);
            Table databaseTable = SQLUtils.asTable(resultSet, super.identifiers());
            List<Object> tableIdentifiers = super.rows()
                .stream()
                .map(row -> row.value(this.column).get())
                .collect(Collectors.toList());

            //Update this table with any rows that it doesn't contain
            for (TableRow row : databaseTable.rows()) {
                Object newRowIdentifier = row.value(this.column).get();
                if (!tableIdentifiers.contains(newRowIdentifier)) {
                    super.addRow(row);
                }
            }
        }
        catch (SQLException | IdentifierMismatchException e) {
            ErrorManager.handle(e);
        }
    }
}
