package nz.pumbas.steamtables;

import java.util.HashMap;
import java.util.Map;

import nz.pumbas.steamtables.annotations.Column;
import nz.pumbas.utilities.Utilities;

public final class ModelHelper
{
    private static final Map<Class<?>, Map<String, Column>> mappedColumns = new HashMap<>();

    private ModelHelper() {}

    public static Column getAnnotationFrom(Class<?> clazz, String column) {
        return getMappedColumns(clazz).get(column.toLowerCase());
    }

    private static Map<String, Column> getMappedColumns(Class<?> clazz) {
        if (!mappedColumns.containsKey(clazz)) {
            Map<String, Column> map = new HashMap<>();

            Utilities.getAnnotatedFields(clazz, Column.class).forEach(f ->
                map.put(f.getName().toLowerCase(), f.getAnnotation(Column.class)));

            mappedColumns.put(clazz,map);
        }
        return mappedColumns.get(clazz);
    }
}
