package nz.pumbas.utilities;

import org.jetbrains.annotations.Nullable;

import java.lang.annotation.Annotation;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import nz.pumbas.commands.ErrorManager;

public final class Reflect {

    private Reflect() {}

    /**
     * Returns the first {@link Method} in a class with the specified name. If the method cannot be
     * found, an {@link IllegalArgumentException} is thrown.
     *
     * @param clazz
     *     The {@link Class} to check, for the {@link Method} with the specified name
     * @param name
     *     The name of the {@link Method} to find
     *
     * @return The {@link Method} with the specified name
     */
    public static Method getMethod(Class<?> clazz, String name)
    {
        for (Method method : clazz.getDeclaredMethods()) {
            if (method.getName().equals(name)) {
                method.setAccessible(true);
                return method;
            }
        }
        throw new IllegalArgumentException(
                String.format("There is no method with the name %s in the class %s", name, clazz.getSimpleName()));
    }

    /**
     * Returns the first {@link Method} in a class with the specified name. If the method cannot be
     * found, an {@link IllegalArgumentException} is thrown.
     *
     * @param instance
     *     The {@link Object} to check for the {@link Method} with the specified name
     * @param name
     *     The name of the {@link Method} to find
     *
     * @return The {@link Method} with the specified name
     */
    public static Method getMethod(Object instance, String name)
    {
        return getMethod(instance.getClass(), name);
    }

    /**
     * Retrieves all the methods of a class with the specified annotation.
     *
     * @param target
     *     The class to search for methods with the specified annotation
     * @param annotation
     *     The annotation to check if methods have
     * @param getSuperMethods
     *     Whether it should check for annotated methods in super classes
     *
     * @return A {@link List} containing all the methods with the specified annotation
     */
    public static List<Method> getAnnotatedMethods(Class<?> target, Class<? extends Annotation> annotation, boolean getSuperMethods)
    {
        List<Method> methods = new ArrayList<>();

        for (Method method : target.getDeclaredMethods()) {
            if (method.isAnnotationPresent(annotation))
                methods.add(method);
        }

        if (getSuperMethods) {
            Class<?> superclass = target.getSuperclass();
            if (Object.class != superclass)
                methods.addAll(getAnnotatedMethods(superclass, annotation, getSuperMethods));
        }
        return methods;
    }

    /**
     * Retrieves all the methods of a class with the specified annotation which pass all the specified filters.
     *
     * @param target
     *     The class to search for methods with the specified annotation
     * @param annotation
     *     The annotation to check if methods have
     * @param getSuperMethods
     *     Whether it should check for annotated methods in super classes
     * @param filters
     *     A varargs of {@link Predicate filters} to check the methods against
     *
     * @return A {@link List} containing all the matching methods
     */
    @SafeVarargs
    public static List<Method> getAnnotatedMethods(Class<?> target, Class<? extends Annotation> annotation,
                                                   boolean getSuperMethods, Predicate<Method>... filters)
    {
        return getAnnotatedMethods(target, annotation, getSuperMethods)
            .stream()
            .filter(m ->
                Arrays.stream(filters).allMatch(filter -> filter.test(m)))
            .collect(Collectors.toList());
    }

    /**
     * Retrieves all the methods of a class with the specified annotation which have the specified modifiers.
     * You can specify modifiers as follows: {@code Modifier::isPublic, Modifier::isStatic}, etc.
     *
     * @param target
     *     The class to search for methods with the specified annotation
     * @param annotation
     *     The annotation to check if methods have
     * @param getSuperMethods
     *     Whether it should check for annotated methods in super classes
     * @param modifierFilters
     *     A varargs of {@link Predicate modifiers} to check if the methods have
     *
     * @return A {@link List} containing all the matching methods
     */
    @SafeVarargs
    public static List<Method> getAnnotatedMethodsWithModifiers(Class<?> target, Class<? extends Annotation> annotation,
                                                                boolean getSuperMethods, Predicate<Integer>... modifierFilters)
    {
        return getAnnotatedMethods(target, annotation, getSuperMethods)
            .stream()
            .filter(m ->
                Arrays.stream(modifierFilters).allMatch(modifier -> modifier.test(m.getModifiers())))
            .collect(Collectors.toList());
    }

    /**
     * Builds an {@link Predicate} to test a {@link Member} for the passed modifiers. Modifiers can be specified
     * using {@code Modifier::isPublic, Modifier::isStatic}, etc.
     *
     * @param modifierFilters
     *      The modifiers to test the {@link Member} for
     *
     * @return An {@link Predicate} which can test a {@link Member} for the passed modifiers.
     */
    @SafeVarargs
    public static Predicate<? extends Member> buildModifiersPredicate(Predicate<Integer>... modifierFilters)
    {
        return m -> Arrays.stream(modifierFilters).allMatch(modifier -> modifier.test(m.getModifiers()));
    }

    /**
     * Filters an array of {@link AccessibleObject reflections} by the passed {@link Predicate filters}.
     *
     * @param reflections
     *      The array of {@link AccessibleObject} to filter through
     * @param filters
     *      The {@link Predicate}s to filter by
     * @param <T>
     *      The type of the {@link AccessibleObject}
     *
     * @return A {@link List} of {@link AccessibleObject}s that passed all the filters
     */
    @SafeVarargs
    public static <T extends AccessibleObject & Member> List<T> filterReflections(T[] reflections, Predicate<T>... filters)
    {
        return Arrays.stream(reflections)
            .filter(m ->
                Arrays.stream(filters).allMatch(filter -> filter.test(m)))
            .collect(Collectors.toList());
    }

    /**
     * If an object has the specified annotation, call the {@link Consumer} with the annotation.
     *
     * @param object
     *     The object being checked for the annotation
     * @param annotationClass
     *     The class of the annotation
     * @param consumer
     *     The {@link Consumer} to be called if the object has the annotation
     * @param <T>
     *     The type of the annotation
     */
    public static <T extends Annotation> void ifAnnotationPresent(Object object, Class<T> annotationClass, Consumer<T> consumer)
    {
        if (object.getClass().isAnnotationPresent(annotationClass)) {
            consumer.accept(object.getClass().getAnnotation(annotationClass));
        }
    }

    /**
     * If an {@link AnnotatedElement} has the specific annotation, retrieve a specific field using the {@link Function}
     * otherwise return the default value.
     *
     * @param target
     *     The {@link AnnotatedElement} being checked for the annotation
     * @param annotationClass
     *     The {@link Class} of the annotation
     * @param function
     *     The {@link Function} to retrieve the annotation field
     * @param defaultValue
     *     The default value to return if the object doesn't have the annotation
     * @param <T>
     *     The type of the annotation
     * @param <R>
     *     The type of the annotation's field being retreived
     *
     * @return The annotation's field value or the default value if it doesn't have the annotation
     */
    public static <T extends Annotation, R> R getAnnotationFieldElse(AnnotatedElement target, Class<T> annotationClass,
                                                                     Function<T, R> function, R defaultValue)
    {
        if (target.isAnnotationPresent(annotationClass)) {
            return function.apply(target.getAnnotation(annotationClass));
        }
        return defaultValue;
    }

    /**
     * Retrieves all the fields of a class with the specified annotation.
     *
     * @param target
     *     The class to search for fields with the specified annotation
     * @param annotation
     *     The annotation to check if fields have
     *
     * @return A {@link List} containing all the fields with the specified annotation
     */
    public static List<Field> getAnnotatedFields(Class<?> target, Class<? extends Annotation> annotation)
    {
        List<Field> fields = new ArrayList<>();

        for (Field field : target.getDeclaredFields()) {
            if (field.isAnnotationPresent(annotation))
                fields.add(field);
        }

        return fields;
    }

    /**
     * Retrieves all the fields of a class without the specified annotation.
     *
     * @param target
     *     The class to search for fields without the specified annotation
     * @param annotation
     *     The annotation to check that fields don't have
     *
     * @return A {@link List} containing all the fields without the specified annotation
     */
    public static List<Field> getFieldsNotAnnotatedWith(Class<?> target, Class<? extends Annotation> annotation)
    {
        List<Field> fields = new ArrayList<>();

        for (Field field : target.getDeclaredFields()) {
            if (!field.isAnnotationPresent(annotation))
                fields.add(field);
        }

        return fields;
    }

    /**
     * Gets a field from an object by its name and casts it to the appropriate type. If that field doesn't exist, it
     * instead returns the passed default value.
     *
     * @param object
     *     The object to get the field from
     * @param name
     *     The name of the field to get
     * @param defaultValue
     *     The default value to return if the field doesn't exist
     * @param <T>
     *     The type of the field
     *
     * @return The field's value or the default value if the field doesn't exist
     */
    @SuppressWarnings("unchecked")
    public static <T> T getField(Object object, String name, T defaultValue)
    {
        try {
            Field field = object.getClass().getField(name);
            field.setAccessible(true);
            return (T) field.get(object);
        } catch (IllegalAccessException | NoSuchFieldException e) {
            ErrorManager.handle(e);
        }
        return defaultValue;
    }

    /**
     * Checks an array of annotations for if it contains an annotation of the specified type. If it does, the first
     * instance of this annotation is returned.
     *
     * @param annotations
     *     The array of annotations to search through
     * @param annotationType
     *     The type of the annotation to search for
     * @param <T>
     *     The type of the annotation
     *
     * @return An {@link Optional} containing the annotation if its present
     */
    @SuppressWarnings("unchecked")
    public static <T extends Annotation> Optional<T> retrieveAnnotation(Annotation[] annotations,
                                                                        Class<T> annotationType)
    {
        for (Annotation annotation : annotations) {
            if (annotation.annotationType().isAssignableFrom(annotationType)) {
                return Optional.of((T) annotation);
            }
        }
        return Optional.empty();
    }

    /**
     * Returns an {@link Optional} containing the annotation if present.
     *
     * @param clazz
     *      The class to check for the annotation
     * @param annotationType
     *      The type of the annotation
     * @param <T>
     *      The type of the annotation
     *
     * @return An {@link Optional} containing the annotation if present
     */
    public static <T extends Annotation> Optional<T> getAnnotation(Class<?> clazz, Class<T> annotationType)
    {
        return Optional.ofNullable(clazz.getAnnotation(annotationType));
    }

    /**
     * Returns the {@link Class type} of an array. If its not an array, it'll just return the passed in {@link Class}.
     * For example the array type of int[] is int.
     *
     * @param clazz
     *      The {@link Class} to get the array type of
     *
     * @return The {@link Class type} of the array, or the passed in {@link Class} if its not an array
     */
    public static Class<?> getArrayType(Class<?> clazz)
    {
        return clazz.isArray() ? clazz.getComponentType() : clazz;
    }

    /**
     * Creates an instance of an {@link Class} using its first constructor.
     *
     * @param clazz
     *     The {@link Class} of the object to create an instance of
     * @param parameters
     *     The parameters to pass to the constructor
     * @param <T>
     *     The type of the object to create
     *
     * @return The instantiated object
     */
    @Nullable
    @SuppressWarnings("unchecked")
    public static <T> T createInstance(Class<T> clazz, Object... parameters)
    {
        try {
            if (0 == clazz.getDeclaredConstructors().length)
                throw new IllegalArgumentException(
                    String.format("The class %s, needs to have at least 1 constructor", clazz.getSimpleName()));

            Constructor<?> constructor = clazz.getDeclaredConstructors()[0];
            constructor.setAccessible(true);
            return (T) constructor.newInstance(parameters);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            ErrorManager.handle(e);
        }
        return null;
    }
}
