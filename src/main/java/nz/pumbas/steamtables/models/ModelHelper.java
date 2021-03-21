package nz.pumbas.steamtables.models;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import nz.pumbas.steamtables.annotations.Column;
import nz.pumbas.steamtables.annotations.Model;
import nz.pumbas.utilities.Utilities;

public final class ModelHelper
{
    private static final Map<Class<?>, ModelInfo> mappedModels = new HashMap<>();

    private ModelHelper() {}

    public static Column getAnnotationFrom(Class<?> clazz, String column) {
        return getModelData(clazz).getMappedColumns().get(column.toLowerCase());
    }

    public static double getDouble(Object object, String fieldName) {
        try {
            return (double) object.getClass().getField(fieldName).get(object);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            return -1;
        }
    }

    public static Set<String> getColumnNames(Class<?> clazz) {
        return getModelData(clazz).getMappedColumns().keySet();
    }

    public static String getTableName(Class<?> clazz) {
        return getModelData(clazz).getTableName();
    }

    private static ModelInfo getModelData(Class<?> clazz) {
        if (!mappedModels.containsKey(clazz)) {
            String tableName = Utilities.getAnnotationFieldElse(
                clazz, Model.class, Model::tableName, "");

            if (tableName.isEmpty())
                throw new IllegalArgumentException(
                    String.format("The class %s doesn't have the @Model annotation.", clazz.getSimpleName()));

            Map<String, Column> mappedColumns = new HashMap<>();

            Utilities.getAnnotatedFields(clazz, Column.class).forEach(f ->
                mappedColumns.put(f.getName().toLowerCase(), f.getAnnotation(Column.class)));

            mappedModels.put(clazz,new ModelInfo(tableName, mappedColumns));
        }
        return mappedModels.get(clazz);
    }
}
