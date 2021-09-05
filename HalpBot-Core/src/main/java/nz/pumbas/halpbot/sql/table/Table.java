/*
 * Copyright (C) 2020 Guus Lieben
 *
 * This framework is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 2.1 of the
 * License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See
 * the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library. If not, see {@literal<http://www.gnu.org/licenses/>}.
 */

package nz.pumbas.halpbot.sql.table;

import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

import nz.pumbas.halpbot.objects.Exceptional;
import nz.pumbas.halpbot.sql.functionalinterfaces.SQLBiFunction;
import nz.pumbas.halpbot.sql.functionalinterfaces.SQLTriConsumer;
import nz.pumbas.halpbot.sql.table.annotations.Property;
import nz.pumbas.halpbot.sql.table.behavior.Merge;
import nz.pumbas.halpbot.sql.table.behavior.Order;
import nz.pumbas.halpbot.sql.table.column.ColumnIdentifier;
import nz.pumbas.halpbot.sql.table.exceptions.EmptyEntryException;
import nz.pumbas.halpbot.sql.table.exceptions.IdentifierMismatchException;
import nz.pumbas.halpbot.sql.table.exceptions.UnknownIdentifierException;
import nz.pumbas.halpbot.sql.table.exceptions.ValueMismatchException;
import nz.pumbas.halpbot.utilities.ErrorManager;
import nz.pumbas.halpbot.utilities.Reflect;

/**
 * A relational table type which can easily create weak relations to other tables. Relations are
 * non-strict references so either table can be disposed of without affecting the origin.
 *
 * <p>Each table has a final set of column identifiers indicating their structure. Tables contain
 * {@link TableRow}s which can only be added if the row has the same identifiers as the table. If a
 * row has more, less, or mismatching column idenfitiers it cannot be added to the table.
 *
 * <p>Column identifiers are unique and should be implementations of {@link ColumnIdentifier} with a
 * generic type indicating the data type.
 *
 * @author Simbolduc, GuusLieben
 * @since feature/S124
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
public class Table {

    private final List<TableRow> rows;
    private final ColumnIdentifier<?>[] identifiers;

    public ColumnIdentifier<?>[] identifiers() {
        return this.identifiers;
    }

    /**
     * Instantiates a new Table with a given set of column identifiers. These identifiers cannot be
     * modified later unless a select action is performed, creating a split copy of this table
     * instance.
     *
     * @param columns
     *         The column identifiers
     */
    public Table(ColumnIdentifier<?>... columns) {
        this.identifiers = columns;
        this.rows = new CopyOnWriteArrayList<>();
    }

    public Table(Collection<ColumnIdentifier<?>> columns) {
        this.identifiers = columns.toArray(new ColumnIdentifier[0]);
        this.rows = new CopyOnWriteArrayList<>();
    }

    private static <T> List<TableRow> matching(TableRow row, Table otherTable, ColumnIdentifier<T> column) {
        Exceptional<?> exceptionalValue = row.value(column);
        // No way to join on value if it is not present. Technically this should not be possible as a
        // NPE is typically thrown if a null value is added to a row.
        if (exceptionalValue.absent())
            throw new IndexOutOfBoundsException("No value present for " + column.name());
        T expectedValue = (T) exceptionalValue.get();

        return otherTable.where(column, expectedValue).rows();
    }

    private static void populateAtColumn(
            Merge merge,
            boolean populateEmptyEntries,
            Iterable<TableRow> matchingRows,
            TableRow joinedRow,
            ColumnIdentifier<?> identifier
    ) throws EmptyEntryException {
        for (TableRow matchingRow : matchingRows) {
            /*
             * If there is already a value present on this row, look up if we want to keep the existing,
             * or use the new value.
             */
            if (!joinedRow.value(identifier).present() || Merge.PREFER_FOREIGN == merge)
                joinedRow.add(identifier, matchingRow.value(identifier).get());
        }

        /*
         * If there was no value filled by either this table instance, or the foreign table, try to
         *  populate it with null. If that is not allowed throw a exception.
         */
        if (!joinedRow.value(identifier).present()) {
            if (populateEmptyEntries) joinedRow.add(identifier, null);
            else
                throw new EmptyEntryException(
                        "Could not populate empty entry for column " + identifier.name());
        }
    }

    /**
     * Generates a {@link TableRow} from a given object based on the objects {@link Field}s. By
     * default the field name is used to look up a matching column identifier which is present inside
     * the table. If the field is decorated with {@link Property} the contained {@link
     * ColumnIdentifier} is used instead.
     *
     * <p>If the field is decorated with {@link Property} with {@link Property#ignore()} set to {@code true}
     * the field will not be converted to a column entry in the row. One attempt will be made to make the
     * field accessible if it is not already.
     *
     * @param object
     *         Object to "try to" add as a row to the table
     *
     * @throws IllegalArgumentException
     *         When a field cannot be accessed or cast correctly. Contains
     *         the causing {@link Exception} as the cause.
     * @throws UnknownIdentifierException
     *         When no column identifier could be found or generated, or
     *         when there are not enough fields present to satiate the column identifiers present in this
     *         table.
     */
    public void addRow(Object object) {
        TableRow row = new TableRow();

        for (Field field : object.getClass().getDeclaredFields()) {
            field.setAccessible(true);
            final Exceptional<Property> annotation = Reflect.annotation(field, Property.class);
            if (!(annotation.present() && annotation.get().ignore())) {
                try {
                    ColumnIdentifier columnIdentifier = null;

                    // Try to grab the column identifier from the Identifier annotation of the field (if
                    // present)
                    Exceptional<Property> identifier = Reflect.annotation(field, Property.class);
                    if (identifier.present() && !"".equals(identifier.get().value())) columnIdentifier = this.identifier(identifier.get().value());

                    // If no Identifier annotation was present, try to grab it using the field name
                    if (null == columnIdentifier) columnIdentifier = this.identifier(field.getName());

                    // No column identifier was found
                    if (null == columnIdentifier)
                        throw new UnknownIdentifierException(
                                "Unknown column identifier for field named : " + field.getName());

                    row.add(columnIdentifier, field.get(object));
                }
                catch (IllegalAccessError | ClassCastException | IllegalAccessException e) {
                    throw new IllegalArgumentException(e);
                }
            }
        }

        if (row.columns().size() != this.identifiers.length) {
            throw new UnknownIdentifierException("Missing Columns!");
        }
        this.rows.add(row);
    }

    @Nullable
    public ColumnIdentifier identifier(@NonNls String fieldName) throws ClassCastException {
        for (ColumnIdentifier columnIdentifier : this.identifiers) {
            if (columnIdentifier.name().equalsIgnoreCase(fieldName)) {
                return columnIdentifier;
            }
        }
        return null;
    }

    /**
     * Generates a {@link TableRow} from a given set of objects. The objects should be in the same
     * order as the table's {@link Table#identifiers()}. If the data type of a object does not
     * match up with its expected {@link ColumnIdentifier} a exception is thrown and the row is not
     * inserted into the table.
     *
     * @param values
     *         Objects to "try to" add as rows to the table
     *
     * @throws IllegalArgumentException
     *         When the amount of values does not meet the amount of column
     *         headers, or when the data type of the object does not match the expected type.
     */
    public void addRow(Object... values) {
        if (values.length != this.identifiers.length)
            throw new IllegalArgumentException(
                    "Amount of given values does not meet amount of column headers");

        TableRow row = new TableRow();
        for (int i = 0; i < this.identifiers.length; i++) {
            ColumnIdentifier identifier = this.identifiers[i];
            Object value = values[i];
            row.add(identifier, value);
        }
        this.rows.add(row);
    }

    /**
     * Filters the table's rows based on their value for a given {@link ColumnIdentifier}. If the
     * value of the row at the given column matches the expected filter value the row is kept,
     * otherwise it is ignored.
     *
     * <p>Returns a new table with only the filtered rows. The origin table is not modified. Either
     * table can be disposed of without affecting the other. Row references are shared between both
     * tables.
     *
     * @param <T>
     *         Indicates the class that both the Identifier and the Filter must have
     * @param column
     *         Indicates which column is used to search through the table
     * @param filter
     *         Indicates what value to search for
     *
     * @return Returns the new table with the filter applied
     * @throws IllegalArgumentException
     *         When a row causes a {@link IdentifierMismatchException}.
     *         Typically this is never thrown unless changes were made from another thread.
     */
    public <T> Table where(ColumnIdentifier<T> column, T filter) {
        if (!this.hasColumn(column))
            throw new UnknownIdentifierException("Cannot look up a column that does not exist");

        Collection<TableRow> filteredRows = new ArrayList<>();
        for (TableRow row : this.rows) {
            Exceptional<T> value = row.value(column);
            if (!value.present()) continue;
            if (value.get() == filter || value.get().equals(filter)) {
                filteredRows.add(row);
            }
        }
        Table lookupTable = new Table(this.identifiers);
        for (TableRow filteredRow : filteredRows) {
            try {
                lookupTable.addRow(filteredRow);
            }
            catch (IdentifierMismatchException e) {
                throw new IllegalArgumentException(e);
            }
        }
        return lookupTable;
    }


    /**
     * Joins the two tables to create a new table, by appending the rows from the other table to this one based on if
     * they share the same value in the specified column. If there is a value in this table that
     * doesn't have a matching value in the column of the other table, this will cause a
     * {@link ValueMismatchException}. These rows can instead be skipped by passing {@code skipMismatched} as true
     * which will also prevent this error from being thrown.
     * <p>
     * This is equivalent to doing the following SQL statement:
     * <p>
     * {@code
     *      SELECT * FROM thisTable INNER JOIN otherTable ON thisTable.column == otherTable.column
     * }
     * <p>
     * Note that the order of the rows in the original tables will be changed, however, nothing else.
     *
     * @param otherTable
     *      The other table to join
     * @param column
     *      The column to join both tables on
     * @param skipMismatched
     *      If values that don't exist in both tables should be skipped, or if a {@link ValueMismatchException}
     *      should be thrown instead
     * @param <T>
     *      The type of the column values
     *
     * @return The new joined table
     * @throws ValueMismatchException
     *         If there is a value in this table's column which doesn't exist in the other table's column and they're
     *         being skipped
     * @throws IdentifierMismatchException
     *         If the specified column isn't contained within both tables
     */
    public <T extends Comparable<T>> Table joinOn(@NotNull Table otherTable, ColumnIdentifier<T> column,
                                                  boolean skipMismatched)
        throws ValueMismatchException, IdentifierMismatchException {
        return this.joinOn(otherTable, column, column, skipMismatched, false);
    }


    /**
     * Joins the two tables to create a new table, by appending the rows from the other table to this one based on if
     * they share the same value in the respective columns for each table. If there is a value in this table that
     * doesn't have a matching value in the column of the other table, this will cause a
     * {@link ValueMismatchException}. These rows can instead be skipped by passing {@code skipMismatched} as true
     * which will also prevent this error from being thrown.
     * <p>
     * This is equivalent to doing the following SQL statement:
     * <p>
     * {@code
     *      SELECT * FROM thisTable INNER JOIN otherTable ON thisTable.thisColumn == otherTable.otherColumn
     * }
     * <p>
     * Note that the order of the rows in the original tables will be changed, however, nothing else.
     *
     * @param otherTable
     *      The other table to join
     * @param thisColumn
     *      The column to join on for this table
     * @param otherColumn
     *      The column to join on for the other table
     * @param skipMismatched
     *      If values that don't exist in both tables should be skipped, or if a {@link ValueMismatchException}
     *      should be thrown instead
     * @param keepOtherColumn
     *      Whether the other column should be kept in the new table (The column from this table is kept)
     * @param <T>
     *      The type of the column values
     *
     * @return The new joined table
     * @throws ValueMismatchException
     *         If there is a value in this table's column which doesn't exist in the other table's column and they're
     *         being skipped
     * @throws IdentifierMismatchException
     *         If the specified columns aren't contained within their respective tables
     */
    public <T extends Comparable<T>> Table joinOn(@NotNull Table otherTable, ColumnIdentifier<T> thisColumn,
                                                  ColumnIdentifier<T> otherColumn, boolean skipMismatched,
                                                  boolean keepOtherColumn)
        throws ValueMismatchException, IdentifierMismatchException
    {
        if (this.hasColumn(thisColumn) && otherTable.hasColumn(otherColumn)) {

            Set<ColumnIdentifier<?>> mergedIdentifiers = new HashSet<>();
            mergedIdentifiers.addAll(List.of(this.identifiers()));
            mergedIdentifiers.addAll(List.of(otherTable.identifiers()));
            mergedIdentifiers.remove(thisColumn);
            if (!keepOtherColumn)
                mergedIdentifiers.remove(otherColumn);

            this.orderBy(thisColumn, Order.ASC);
            otherTable.orderBy(otherColumn, Order.ASC);

            int otherRowIndex = 0;
            List<TableRow> otherRows = otherTable.rows();

            Table joinedTable = new Table(mergedIdentifiers.toArray(new ColumnIdentifier<?>[0]));
            for (TableRow row : this.rows())
            {
                //Find the matching row. Note: As both tables are sorted based on the same column, if a match is
                // found, then none of the previous rows will be matches.
                T thisValue = row.value(thisColumn).orNull();
                int index;
                for (index = otherRowIndex; index < otherRows.size(); index++)
                {
                    T otherValue = otherRows.get(index).value(otherColumn).orNull();
                    if (Objects.equals(thisValue, otherValue)) {
                        otherRowIndex = index;
                    }
                }
                if (otherRowIndex != index) {
                    if (skipMismatched) continue;
                    else
                        throw new ValueMismatchException(
                            "There was a value in the '" + thisColumn + "' column which weren't in both tables");
                }
                TableRow joinedRow = new TableRow();
                TableRow matchedRow = otherRows.get(index);

                for (ColumnIdentifier<?> mergedIdentifier : mergedIdentifiers) {
                    row.value(mergedIdentifier)
                        .present(value -> joinedRow.add(mergedIdentifier, value)
                    ).absent(
                        () -> joinedRow.add(mergedIdentifier, matchedRow.value(mergedIdentifier).orNull()));
                }
            }

            return joinedTable;
        }
        throw new IdentifierMismatchException(
            "Either column '" + thisColumn + "' or column '" + otherColumn + "' does not exist in their respective tables");
    }

    /**
     * Overloaded method for {@link #join(Table, ColumnIdentifier, Merge, boolean)}. Empty entries
     * will not be populated using this method.
     *
     * @param <T>
     *         The data type of the column
     * @param otherTable
     *         The other/foreign table
     * @param column
     *         The column to join on
     * @param merge
     *         The merge behavior
     *
     * @return A new table with the joined rows
     * @throws EmptyEntryException
     *         Thrown if a entry is empty and cannot be populated
     * @throws IdentifierMismatchException
     *         When a identifier does not exist across both tables
     */
    public <T> Table join(@NotNull Table otherTable, ColumnIdentifier<T> column, Merge merge)
            throws EmptyEntryException, IdentifierMismatchException {
        return this.join(otherTable, column, merge, false);
    }

    private void tryPopulateMissingEntry(
            @NotNull Table otherTable,
            boolean populateEmptyEntries,
            Iterable<ColumnIdentifier<?>> mergedIdentifiers,
            Table joinedTable,
            TableRow row
    ) throws IdentifierMismatchException {
        if (!Arrays.equals(this.identifiers(), otherTable.identifiers())) {
            if (populateEmptyEntries) {
                for (ColumnIdentifier<?> identifier : mergedIdentifiers) {
                    Exceptional<?> exceptionalValue = row.value(identifier);
                    exceptionalValue
                            .present(value -> row.add(identifier, value))
                            .absent(() -> row.add(identifier, null));
                }
            }
            else {
                throw new IdentifierMismatchException(
                        "Remaining rows were found in the foreign table, but identifiers are not equal. Cannot insert null values!");
            }
        }
        joinedTable.addRow(row);
    }

    /**
     * Joins two tables based on a given {@link ColumnIdentifier}. Rows are joined together based on
     * their value on the given column, if the values match the rows are joined together.
     *
     * <p>Rows from the origin and the foreign table are merged into single rows. If a column exists
     * in both tables, the {@link Merge} behavior indicates which to keep.
     *
     * <p>If a row does not have a matching row in the other table while new columns are created,
     * {@code populateEmptyEntries} indicates whether to treat this is a illegal state, or populate
     * the entry with null.
     *
     * @param <T>
     *         The data type of the column
     * @param otherTable
     *         The other/foreign table
     * @param column
     *         The column to join on
     * @param merge
     *         The merge behavior
     * @param populateEmptyEntries
     *         Whether or not empty entries should be populated (with null)
     *
     * @return A new table with the joined rows
     * @throws EmptyEntryException
     *         Thrown if a entry is empty and cannot be populated
     * @throws IdentifierMismatchException
     *         When a identifier does not exist across both tables
     */
    public <T> Table join(
            @NotNull Table otherTable,
            ColumnIdentifier<T> column,
            Merge merge,
            boolean populateEmptyEntries
    ) throws EmptyEntryException, IdentifierMismatchException {
        if (this.hasColumn(column) && otherTable.hasColumn(column)) {

            Set<ColumnIdentifier<?>> mergedIdentifiers = new HashSet<>();
            mergedIdentifiers.addAll(List.of(this.identifiers()));
            mergedIdentifiers.addAll(List.of(otherTable.identifiers()));

            Table joinedTable = new Table(mergedIdentifiers.toArray(new ColumnIdentifier<?>[0]));
            for (TableRow row : this.rows()) {
                try {
                    this.populateMatchingRows(
                            otherTable, column, merge, populateEmptyEntries, joinedTable, row);
                }
                catch (IllegalArgumentException e) {
                    continue;
                }
            }

            /*
            It is possible not all foreign rows had a matching value, if that is the case we will add them here if
            possible (if the foreign table has no additional identifiers which we cannot populate here.
            */
            for (TableRow row : otherTable.rows())
                this.populateMissingEntries(otherTable, column, populateEmptyEntries, mergedIdentifiers, joinedTable, row);

            return joinedTable;
        }
        throw new IdentifierMismatchException("Column '" + column + "' does not exist in both tables");
    }

    private <T> void populateMatchingRows(
            @NotNull Table otherTable,
            ColumnIdentifier<T> column,
            Merge merge,
            boolean populateEmptyEntries,
            Table joinedTable,
            TableRow row
    ) throws EmptyEntryException, IdentifierMismatchException {
        List<TableRow> matchingRows = Table.matching(row, otherTable, column);

        TableRow joinedRow = new TableRow();
        for (ColumnIdentifier<?> identifier : this.identifiers())
            joinedRow.add(identifier, row.value(identifier).get());

        for (ColumnIdentifier<?> identifier : otherTable.identifiers())
            Table.populateAtColumn(merge, populateEmptyEntries, matchingRows, joinedRow, identifier);

        joinedTable.addRow(joinedRow);
    }

    private <T> void populateMissingEntries(
            @NotNull Table otherTable,
            ColumnIdentifier<T> column,
            boolean populateEmptyEntries,
            Iterable<ColumnIdentifier<?>> mergedIdentifiers,
            Table joinedTable,
            TableRow row
    ) {
        try {
            List<TableRow> matchingRows = Table.matching(row, joinedTable, column);
            if (matchingRows.isEmpty())
                this.tryPopulateMissingEntry(
                        otherTable, populateEmptyEntries, mergedIdentifiers, joinedTable, row);
        }
        catch (IdentifierMismatchException ignored) {
        }
    }

    /**
     * Selects only the given columns of a table. Returns a new table populated with all the rows in
     * the origin table, but only with the columns provided.
     *
     * @param columns
     *         Indicates the columns to select
     *
     * @return Return the new table with only the selected columns
     */
    public Table select(ColumnIdentifier<?>... columns) {
        Table table = new Table(columns);

        this.rows.forEach(row -> {
            TableRow tmpRow = new TableRow();
            for (ColumnIdentifier<?> column : columns) {
                row.columns().stream()
                        .filter(column::equals)
                        .forEach(tCol -> tmpRow.add(column, row.value(column).get()));
            }
            try {
                table.addRow(tmpRow);
            }
            catch (IdentifierMismatchException e) {
                throw new IllegalArgumentException(e);
            }
        });

        return table;
    }

    /**
     * Adds a row to the table if the column identifiers are equal in length and type. If the row has
     * more or less column identifiers it is not accepted into the table. If a row has a column which
     * is not contained in this table it is not accepted into the table.
     *
     * @param row
     *         The row object to add to the table
     *
     * @throws IdentifierMismatchException
     *         When there is a column mismatch between the row and the
     *         table
     */
    public void addRow(TableRow row) throws IdentifierMismatchException {
        // Check if the row has the same amount of column as this table
        if (row.columns().size() != this.identifiers.length)
            throw new IdentifierMismatchException(
                    "The row does not have the same amount of columns as the table");

        // Check if the row has the same columns with the same order
        for (ColumnIdentifier<?> column : row.columns()) {
            if (!this.hasColumn(column)) {
                throw new IdentifierMismatchException(
                        "Column '" + column.name() + "' is not contained in table");
            }
        }

        this.rows.add(row);
    }

    /**
     * Returns whether or not the table contains the given {@link ColumnIdentifier}.
     *
     * @param column
     *         The column
     *
     * @return Whether or not the column is present
     */
    public boolean hasColumn(ColumnIdentifier<?> column) {
        for (ColumnIdentifier<?> identifier : this.identifiers) {
            if (identifier == column || identifier.equals(column)) return true;
        }
        return false;
    }

    /**
     * Attempts to get the first row in the table. If there is no value present, a {@link Exceptional}
     * holding a {@link IndexOutOfBoundsException} will be returned.
     *
     * @return Return the first row of the table
     */
    public Exceptional<TableRow> first() {
        return Exceptional.of(() -> this.rows.get(0));
    }

    /**
     * Attempts to get the last row in the table. If there is no value present, a {@link Exceptional}
     * holding a {@link IndexOutOfBoundsException} will be returned.
     *
     * @return Return the last row of the table
     */
    public Exceptional<TableRow> last() {
        return Exceptional.of(() -> this.rows.get(this.count() - 1));
    }

    /**
     * Gets the amount of rows present in the table.
     *
     * @return Return the table's row count
     */
    public int count() {
        return this.rows.size();
    }

    /**
     * Orders (sorts) a table based on a given column. Requires the data type of the {@link
     * ColumnIdentifier} to be a implementation of {@link Comparable}. This modifies the existing
     * table.
     *
     * @param <T>
     *         The type constraint indicating this method only accepts implementations of {@link
     *         Comparable}
     * @param column
     *         Indicates the column to order by
     * @param order
     *         Indicates what way to order the table by
     *
     * @throws IllegalArgumentException
     *         When the table does not contain the given column, or the data
     *         type is not a {@link Comparable}
     */
    public <T extends Comparable> void orderBy(ColumnIdentifier<T> column, Order order) {
        if (!this.hasColumn(column))
            throw new IllegalArgumentException(
                    "Table does not contains column named : " + column.name());

        if (!Comparable.class.isAssignableFrom(column.type()))
            throw new IllegalArgumentException(
                    "Column does not contain a comparable data type : " + column.name());

        this.rows.sort((r1, r2) -> {
            Comparable c1 = r1.value(column).get();
            Comparable c2 = r2.value(column).get();
            return Order.ASC == order ? c1.compareTo(c2) : c2.compareTo(c1);
        });
    }

    /**
     * Returns whether or not the table contains the given {@link TableRow}
     *
     * @param row
     *         The row
     *
     * @return Whether or not the row is present
     */
    public boolean hasRow(TableRow row) {
        for (TableRow tableRow : this.rows()) {
            if (tableRow == row) {
                return true;
            }
        }
        return false;
    }

    /**
     * Gets all rows in the table.
     *
     * @return Return the table's rows
     */
    public List<TableRow> rows() {
        return Collections.unmodifiableList(this.rows);
    }

    public void forEach(Consumer<TableRow> consumer) {
        this.rows().forEach(consumer);
    }

    @Override
    public String toString() {
        List<List<String>> rows = new ArrayList<>();
        List<String> headers = new ArrayList<>();
        for (ColumnIdentifier<?> identifier : this.identifiers) {
            headers.add(identifier.name());
        }
        rows.add(headers);
        for (TableRow row : this.rows) {
            List<String> rowValues = new ArrayList<>();
            // In order of identifiers to ensure values are ordered
            for (ColumnIdentifier<?> identifier : this.identifiers) {
                rowValues.add(String.valueOf(row.value(identifier).orNull()));
            }
            rows.add(rowValues);
        }
        final int[] maxLengths = new int[rows.get(0).size()];
        for (final List<String> row : rows) {
            for (int i = 0; i < row.size(); i++) {
                maxLengths[i] = Math.max(maxLengths[i], row.get(i).length());
            }
        }

        final StringBuilder formatBuilder = new StringBuilder();
        for (final int maxLength : maxLengths) {
            formatBuilder.append("%-").append(maxLength + 2).append("s");
        }
        final String format = formatBuilder.toString();

        final StringBuilder result = new StringBuilder();
        for (final List<String> row : rows) {
            result.append(String.format(format, row.toArray(new Object[0]))).append("\n");
        }
        return result.toString();
    }
}
