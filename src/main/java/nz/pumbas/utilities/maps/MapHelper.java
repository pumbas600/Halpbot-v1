package nz.pumbas.utilities.maps;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import nz.pumbas.utilities.Utilities;

public final class MapHelper
{
    private static final Map<Class<?>, Map<String, Row>> parsedClassMaps = new HashMap<>();

    private MapHelper() {}

    public static Map<String, Row> getClassMap(Class<?> clazz)
    {
        if (!parsedClassMaps.containsKey(clazz)) {
            Map<String, Row> parsedClassMap = clazz.isAnnotationPresent(ClassMap.class)
                ? parseClassMap(clazz.getAnnotation(ClassMap.class), clazz)
                : parseFieldMaps(clazz);
            parsedClassMaps.put(clazz, parsedClassMap);
        }
        return parsedClassMaps.get(clazz);
    }

    public static Row getFieldMap(Class<?> clazz, String field)
    {
        Map<String, Row> parsedClassMap = getClassMap(clazz);

        if (!parsedClassMap.containsKey(field))
            throw new IllegalArgumentException(
                String.format("The field %s is not mapped to the class %s", field, clazz.getSimpleName()));

        return parsedClassMap.get(field);
    }

    public static <T> T getValue(Class<?> clazz, String field, String key)
    {
        return getFieldMap(clazz, field).getValue(key);
    }

    private static Map<String, Row> parseClassMap(ClassMap classMap, Class<?> clazz)
    {
        Map<String, Row> parsedClassMap = new HashMap<>();

        boolean allStrings = isAllStrings(classMap.value());
        if (!allStrings && classMap.value().length != classMap.keys().length)
            return parsedClassMap;

        for (Field field : clazz.getFields()) {
            if (!field.isAnnotationPresent(FieldMap.class))
                continue;

            FieldMap fieldMap = field.getAnnotation(FieldMap.class);
            if (fieldMap.value().length != classMap.keys().length)
                continue;

            Map<String, Object> parsedFieldMap = new HashMap<>();
            for (int i = 0; i < fieldMap.value().length; i++) {
                String key = classMap.keys()[i];
                String value = fieldMap.value()[i];

                parsedFieldMap.put(key,
                    allStrings ? value : Utilities.TypeParsers.get(classMap.value()[i]).apply(value));
            }
            parsedClassMap.put(field.getName(), Row.of(parsedFieldMap));
        }

        return parsedClassMap;
    }

    private static Map<String, Row> parseFieldMaps(Class<?> clazz)
    {
        Map<String, Row> parsedClassMap = new HashMap<>();

        for (Field field : clazz.getFields()) {
            if (!field.isAnnotationPresent(FieldMap.class))
                continue;

            FieldMap fieldMap = field.getAnnotation(FieldMap.class);
            boolean allStrings = isAllStrings(fieldMap.types());

            if (!allStrings && fieldMap.value().length != fieldMap.types().length)
                continue;

            Map<String, Object> parsedFieldMap = new HashMap<>();
            for (int i = 0; i < fieldMap.value().length; i++) {
                String[] pair = fieldMap.value()[i].split("=", 2);
                if (2 != pair.length) break;

                String key = pair[0];
                String value = pair[1];

                parsedFieldMap.put(key,
                    allStrings ? value : Utilities.TypeParsers.get(fieldMap.types()[i]).apply(value));
            }
            parsedClassMap.put(field.getName(), Row.of(parsedFieldMap));
        }

        return parsedClassMap;
    }

    private static boolean isAllStrings(Class<?>[] types)
    {
        return 1 == types.length && types[0].isAssignableFrom(Void.class);
    }
}
