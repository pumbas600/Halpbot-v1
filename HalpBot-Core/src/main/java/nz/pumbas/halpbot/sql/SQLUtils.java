package nz.pumbas.halpbot.sql;

import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.Set;

import nz.pumbas.halpbot.objects.Exceptional;
import nz.pumbas.halpbot.sql.functionalinterfaces.SQLBiFunction;
import nz.pumbas.halpbot.sql.functionalinterfaces.SQLTriConsumer;
import nz.pumbas.halpbot.sql.table.Table;
import nz.pumbas.halpbot.sql.table.TableRow;
import nz.pumbas.halpbot.sql.table.annotations.Property;
import nz.pumbas.halpbot.sql.table.column.ColumnIdentifier;
import nz.pumbas.halpbot.sql.table.exceptions.IdentifierMismatchException;
import nz.pumbas.halpbot.utilities.ErrorManager;
import nz.pumbas.halpbot.utilities.Reflect;

public final class SQLUtils
{
    private static final Map<Class<?>, SQLTriConsumer<PreparedStatement, Integer, Object>> setValueMappings
        = Map.of(
        Boolean.class, (p, i, o) -> p.setBoolean(i, (boolean)o),
        Integer.class, (p, i, o) -> p.setInt(i, (int) o),
        Double.class,  (p, i, o) -> p.setDouble(i, (double)o),
        String.class,  (p, i, o) -> p.setString(i, (String)o),
        Long.class,    (p, i, o) -> p.setLong(i, (long)o));

    private static final Map<Class<?>, SQLBiFunction<ResultSet, String, Object>> getValueMappings
        = Map.of(
        Boolean.class, ResultSet::getBoolean,
        Integer.class, ResultSet::getInt,
        Double.class,  ResultSet::getDouble,
        String.class,  ResultSet::getString,
        Long.class,    ResultSet::getLong);

    private SQLUtils() {}

    /**
     * Retrieves a {@link SQLTriConsumer} that's used to set the value of a {@link ResultSet} based on its type.
     *
     * @param type
     *      The type of the value being set to the result set
     *
     * @return The {@link SQLTriConsumer} to set the resultset value with
     */
    public static SQLTriConsumer<PreparedStatement, Integer, Object> getResultSetter(Class<?> type) {
        return setValueMappings.getOrDefault(Reflect.wrapPrimative(type), PreparedStatement::setObject);
    }

    /**
     * Automatically populates a {@link Table} from the {@link ResultSet} and the specified columns.
     *
     * @param resultSet
     *      The {@link ResultSet} to populate the table from
     * @param columns
     *      The columns in the resultset to populate the table with
     *
     * @return The populated {@link Table}
     */
    public static Table asTable(ResultSet resultSet, ColumnIdentifier<?>... columns) {
        Table table = new Table(columns);
        try {
            while (resultSet.next()) {
                TableRow row = new TableRow();
                for (ColumnIdentifier<?> identifier : columns) {
                    Object value = getValueMappings.get(identifier.type()).accept(resultSet, identifier.name());
                    row.add(identifier, value);
                }
                table.addRow(row);
            }
        } catch (SQLException | IdentifierMismatchException e) {
            ErrorManager.handle(e);
        }
        return table;
    }

    public static <T> T asModel(Class<T> type, ResultSet resultSet) {
        return null;
    }

    public static <T> T asModel(Class<T> type, TableRow row) {
        T model = Reflect.createInstance(type);

        for (Field field : type.getDeclaredFields()) {
            field.setAccessible(true);
            final Exceptional<Property> annotation = Reflect.annotation(field, Property.class);
            if (annotation.isEmpty() || !annotation.get().ignore()) {
                try {
                    String fieldName;
                    if (annotation.present() && !annotation.get().value().isEmpty())
                        fieldName = annotation.get().value();
                    else {
                        fieldName = field.getName();
                    }

                    ColumnIdentifier<?> column = identifier(row.columns(), fieldName);
                    field.set(model, row.value(column).get());
                }
                catch (IllegalAccessError | IllegalAccessException e) {
                    throw new IllegalArgumentException(e);
                }
            }
        }

        return model;
    }

    @Nullable
    public static ColumnIdentifier<?> identifier(Set<ColumnIdentifier<?>> columns,
                                                 @NonNls String fieldName) throws ClassCastException {
        for (ColumnIdentifier<?> columnIdentifier : columns) {
            if (columnIdentifier.name().equalsIgnoreCase(fieldName)) {
                return columnIdentifier;
            }
        }
        return null;
    }
}
