package nz.pumbas.halpbot.sql.table;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import nz.pumbas.halpbot.sql.SQLDriver;
import nz.pumbas.halpbot.sql.SQLManager;
import nz.pumbas.halpbot.sql.SQLUtils;
import nz.pumbas.halpbot.sql.table.column.ColumnIdentifier;
import nz.pumbas.halpbot.sql.table.exceptions.IdentifierMismatchException;
import nz.pumbas.halpbot.utilities.ErrorManager;
import nz.pumbas.halpbot.utilities.HalpbotUtils;
import nz.pumbas.halpbot.utilities.Reflect;

public class DatabaseTable extends Table
{
    //TODO: Automatically create column identifiers by looking at columns in table

    protected final SQLDriver driver;
    protected final String tableName;
    protected final ColumnIdentifier<?> column;
    protected final List<ColumnIdentifier<?>> ignoredInsertColumns;
    protected final List<Object> updatedRows = new ArrayList<>();
    protected final String insertSql;
    protected boolean isUpdated;

    /**
     * Instantiates a new DatabaseTable with a given set of column identifiers. These identifiers cannot be
     * modified later unless a select action is performed, creating a split copy of this table
     * instance.
     *
     * @param driver
     *      The {@link SQLDriver} for the database this table belongs to
     * @param tableName
     *      The name of the table
     * @param column
     *      The column to identify added rows by
     * @param columns
     *      The column identifiers
     * @param ignoredInsertColumns
     *      The columns to ignore when inserting rows into the database
     */
    protected DatabaseTable(SQLDriver driver,
                            String tableName,
                            ColumnIdentifier<?> column,
                            ColumnIdentifier<?>[] columns,
                            List<ColumnIdentifier<?>> ignoredInsertColumns) {
        super(columns);
        this.driver = driver;
        this.tableName = tableName;
        this.column = column;
        this.ignoredInsertColumns = ignoredInsertColumns;
        this.insertSql = this.getInsertSql();
        this.driver.onLoad(this::sync);
    }

    public static DatabaseTableBuilder builder(String tableName, ColumnIdentifier<?>... columns) {
        return new DatabaseTableBuilder(tableName, columns);
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

    public void sync(Connection connection) throws SQLException {
        if (!this.updatedRows.isEmpty()) {
            this.insertUpdatedRows(connection);
        }
        if (this.isUnsynced()) {
            this.loadDatabase(connection);
        }
    }

    private String getInsertSql() {
        List<ColumnIdentifier<?>> columnIdentifiers = Arrays.stream(super.identifiers())
            .filter(identifier -> !this.ignoredInsertColumns.contains(identifier))
            .collect(Collectors.toList());

        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder
            .append("INSERT INTO ")
            .append(this.tableName)
            .append(" (");

        if (!columnIdentifiers.isEmpty())
            queryBuilder.append(columnIdentifiers.get(0).name());
        for (int i = 1; i < columnIdentifiers.size(); i++) {
            queryBuilder
                .append(", ")
                .append(columnIdentifiers.get(i).name());
        }

        queryBuilder.append(") VALUES (");

        if (0 < super.identifiers().length)
            queryBuilder.append("?");
        queryBuilder
            .append(", ?".repeat(Math.max(0, super.identifiers().length - 1)))
            .append(")");

        return queryBuilder.toString();
    }

    public void insertUpdatedRows(Connection connection) throws SQLException {
        PreparedStatement statement = this.driver.createStatement(connection, this.getInsertSql());
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

    public void loadDatabase(Connection connection) throws SQLException {
        final String sql = "SELECT * FROM " + this.tableName;
        try {
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
        catch (IdentifierMismatchException e) {
            ErrorManager.handle(e);
        }
    }

    public static class DatabaseTableBuilder {

        private final ColumnIdentifier<?>[] columns;
        private ColumnIdentifier<?> column;
        private SQLDriver driver;
        private final String tableName;
        private final List<ColumnIdentifier<?>> ignoredUpdateColumns = new ArrayList<>();

        public DatabaseTableBuilder(String tableName, ColumnIdentifier<?>... columns) {
            this.columns = columns;
            this.tableName = tableName;
            if (0 == columns.length)
                throw new IllegalArgumentException("You need to specify at least one column");
        }

        public DatabaseTableBuilder driver(String databaseName) {
            this.driver = HalpbotUtils.context()
                .get(SQLManager.class)
                .getDriver(databaseName);
            return this;
        }

        public DatabaseTableBuilder driver(SQLDriver driver) {
            this.driver = driver;
            return this;
        }

        public DatabaseTableBuilder addIgnoredColumns(ColumnIdentifier<?>... columns) {
            this.ignoredUpdateColumns.addAll(List.of(columns));
            return this;
        }

        public DatabaseTable build() {
            if (null == this.column) {
                this.column = this.columns[0];
            }
            return new DatabaseTable(this.driver, this.tableName, this.column, this.columns, this.ignoredUpdateColumns);
        }
    }
}
