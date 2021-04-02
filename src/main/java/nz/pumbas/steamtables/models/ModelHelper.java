package nz.pumbas.steamtables.models;

import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import nz.pumbas.commands.ErrorManager;
import nz.pumbas.steamtables.annotations.IgnoreColumn;
import nz.pumbas.steamtables.annotations.Model;
import nz.pumbas.utilities.Utilities;

public final class ModelHelper
{
    private ModelHelper() {}

    public static List<String> getColumnNames(Class<? extends IModel> clazz)
    {
        return Utilities.getFieldsNotAnnotatedWith(clazz, IgnoreColumn.class)
            .stream().map(Field::getName)
            .collect(Collectors.toList());
    }

    public static String getTableName(Class<? extends IModel> clazz)
    {
        return Utilities.getAnnotationFieldElse(
            clazz, Model.class, Model::tableName, null);
    }

    public static <T extends IModel> T parseModel(Class<T> clazz, ResultSet result)
    {
        T model = Utilities.createInstance(clazz);

        getColumnNames(clazz).forEach(n -> {
            try {
                Field field = model.getClass().getField(n);
                field.setAccessible(true);
                field.set(model, Utilities.TypeParsers.get(field.getType()).apply(result.getString(n)));
            } catch (NoSuchFieldException | SQLException | IllegalAccessException e) {
                ErrorManager.handle(e);
            }
        });
        return model;
    }

    public static <T extends IModel> List<T> parseModels(Class<T> clazz, ResultSet result)
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
