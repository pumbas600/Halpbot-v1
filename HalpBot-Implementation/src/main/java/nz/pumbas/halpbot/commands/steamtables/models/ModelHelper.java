package nz.pumbas.halpbot.commands.steamtables.models;

import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import nz.pumbas.commands.ErrorManager;
import nz.pumbas.halpbot.commands.steamtables.annotations.IgnoreColumn;
import nz.pumbas.halpbot.commands.steamtables.annotations.ModelObject;
import nz.pumbas.utilities.Reflect;
import nz.pumbas.utilities.Utils;

public final class ModelHelper
{
    private ModelHelper() {}

    public static List<String> getColumnNames(Class<? extends Model> clazz)
    {
        return Reflect.getFieldsNotAnnotatedWith(clazz, IgnoreColumn.class)
            .stream().map(Field::getName)
            .collect(Collectors.toList());
    }

    public static String getTableName(Class<? extends Model> clazz)
    {
        return Reflect.getAnnotationFieldElse(
            clazz, ModelObject.class, ModelObject::tableName,
            clazz.getSimpleName().toLowerCase());
    }

    public static <T extends Model> T parseModel(Class<T> clazz, ResultSet result)
    {
        T model = Reflect.createInstance(clazz);

        getColumnNames(clazz).forEach(n -> {
            try {
                Field field = model.getClass().getField(n);
                field.setAccessible(true);
                String value = result.getString(n);
                if (null != value)
                    field.set(model, Utils.TypeParsers.get(field.getType()).apply(value));
            } catch (NoSuchFieldException | SQLException | IllegalAccessException e) {
                ErrorManager.handle(e);
            }
        });
        return model;
    }

    public static <T extends Model> List<T> parseModels(Class<T> clazz, ResultSet result)
    {
        List<T> models = new ArrayList<>();

        try {
            while (result.next()) {
                models.add(parseModel(clazz, result));
            }
        } catch (SQLException e) {
            ErrorManager.handle(e);
        }
        return models;
    }
}
