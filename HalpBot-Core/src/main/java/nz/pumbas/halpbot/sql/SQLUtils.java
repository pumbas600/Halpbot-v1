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

import org.dockbox.hartshorn.core.annotations.Property;
import org.dockbox.hartshorn.core.domain.Exceptional;
import org.dockbox.hartshorn.persistence.exceptions.IdentifierMismatchException;
import org.dockbox.hartshorn.persistence.table.ColumnIdentifier;
import org.dockbox.hartshorn.persistence.table.Table;
import org.dockbox.hartshorn.persistence.table.TableRow;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.Set;

import nz.pumbas.halpbot.sql.functionalinterfaces.SQLBiFunction;
import nz.pumbas.halpbot.sql.functionalinterfaces.SQLTriConsumer;
import nz.pumbas.halpbot.utilities.ErrorManager;
import nz.pumbas.halpbot.utilities.Reflect;

//TODO: Remove this
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
     * Retrieves a {@link SQLTriConsumer} that's used to set the value of a {@link ResultSet} based on the type of
     * the object specified. If the object is null, then it will return {@link PreparedStatement#setObject(int, Object)}.
     *
     * @param object
     *      The object to get the result setter for
     *
     * @return The {@link SQLTriConsumer} to set the resultset value with
     */
    public static SQLTriConsumer<PreparedStatement, Integer, Object> getResultSetter(@Nullable Object object) {
        if (null == object)
            return PreparedStatement::setObject;

        return getResultSetter(object.getClass());
    }

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
                    Object value = getValueMappings.get(identifier.type().type()).accept(resultSet, identifier.name());
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
            if (annotation.absent() || !annotation.get().ignore()) {
                try {
                    String fieldName;
                    if (annotation.present() && !annotation.get().value().isEmpty())
                        fieldName = annotation.get().value();
                    else {
                        fieldName = field.getName();
                    }

                    ColumnIdentifier<?> column = identifier(row.columns(), fieldName);
                    field.set(model, row.value(column).orNull());
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
