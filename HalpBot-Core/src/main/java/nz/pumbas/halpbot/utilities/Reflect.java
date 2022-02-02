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

package nz.pumbas.halpbot.utilities;

import org.dockbox.hartshorn.core.context.element.TypeContext;
import org.dockbox.hartshorn.core.domain.Exceptional;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.annotation.Annotation;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

public final class Reflect
{

    private Reflect() {}

    private static final Pattern IntegerPattern = Pattern.compile("[-|+]?\\d+");
    private static final Pattern DecimalNumberPattern = Pattern.compile("[-|+]?\\d+\\.?\\d*");

    /**
     * An {@link Map} of the built-in {@link Class classes} and their regex syntax.
     */
    private static final Map<Class<?>, Pattern> TypeParsers = Map.of(
        String.class, Pattern.compile("[^\\s]+"),
        Byte.class, IntegerPattern,
        Short.class, IntegerPattern,
        Integer.class, IntegerPattern,
        Long.class, IntegerPattern,
        Float.class, DecimalNumberPattern,
        Double.class, DecimalNumberPattern,
        Character.class, Pattern.compile("."),
        Boolean.class, Pattern.compile("true|yes|false|no|t|f|y|n|1|0", Pattern.CASE_INSENSITIVE)
    );

    /**
     * An {@link Map} of the wrapper {@link Class classes} and their respective primitive {@link Class}.
     */
    private static final Map<TypeContext<?>, TypeContext<?>> PrimativeWrappers = Map.of(
            TypeContext.of(byte.class), TypeContext.of(Byte.class),
            TypeContext.of(short.class), TypeContext.of(Short.class),
            TypeContext.of(int.class), TypeContext.of(Integer.class),
            TypeContext.of(long.class), TypeContext.of(Long.class),
            TypeContext.of(float.class), TypeContext.of(Float.class),
            TypeContext.of(double.class), TypeContext.of(Double.class),
            TypeContext.of(char.class), TypeContext.of(Character.class),
            TypeContext.of(boolean.class), TypeContext.of(Boolean.class)
    );

    private static final Map<Class<?>, Object> DefaultValues = Map.of(
        Byte.class, 0,
        Short.class, 0,
        Integer.class, 0,
        Long.class, 0L,
        Float.class, 0F,
        Double.class, 0D,
        Character.class,'\u0000',
        Boolean.class, false
    );

    /**
     * Checks if the string matches the required syntax of the specified type.
     *
     * @param s
     *     The {@link String} to check
     * @param type
     *     The {@link Class type} to check the syntax against
     *
     * @return If the string matches the syntax of the type
     */
    public static boolean matches(String s, Class<?> type) {
        Class<?> wrappedType = Void.class; //wrapPrimative(type);

        if (!TypeParsers.containsKey(wrappedType))
            throw new IllegalArgumentException("You can only match strings of simple types");

        return TypeParsers.get(wrappedType)
            .matcher(s)
            .matches();
    }

    /**
     * Returns the {@link Pattern syntax} of the specified type.
     *
     * @param type
     *     The {@link Class type} to get the syntax of
     *
     * @return The {@link Pattern syntax} of the specified type
     */
    public static Pattern getSyntax(Class<?> type) {
        if (!TypeParsers.containsKey(type))
            throw new IllegalArgumentException("You can only match simple build in types");

        return TypeParsers.get(type);
    }

    /**
     * Returns the wrapper for a primative, or the passed in type if it has no primative type.
     *
     * @param type
     *     The {@link TypeContext} to get the wrapper type of
     *
     * @return the wrapper for a primative, or the passed in type if it has no primative type
     */
    public static TypeContext<?> wrapPrimative(TypeContext<?> type) {
        return PrimativeWrappers.getOrDefault(type, type);
    }

    /**
     * Retrieves the default value for the specified {@link Class type}.
     *
     * @param type
     *      The {@link Class} to get the default value for
     *
     * @return The types default value
     */
    @Nullable
    public static Object getDefaultValue(Class<?> type) { //TODO: Make this accept TypeContext
        return DefaultValues.getOrDefault(wrapPrimative(TypeContext.of(type)).type(), null);
    }

    /**
     * Retrieves the specified annotation on the {@link Field} if present. If that annotation is not present, then an
     * {@link Exceptional#empty()} will be returned instead.
     *
     * @param field
     *      The field to check for the annotation
     * @param annotationType
     *      The class of the annotation
     * @param <T>
     *      The type of the annotation
     *
     * @return An exceptional containing the annotation if present
     */
    public static <T extends Annotation> Exceptional<T> annotation(Field field, Class<T> annotationType) {
        if (field.isAnnotationPresent(annotationType)) {
            return Exceptional.of(field.getAnnotation(annotationType));
        }
        return Exceptional.empty();
    }


    /**
     * Returns the {@link Class type} of an array. If its not an array, it'll just return the passed in {@link Class}.
     * For example the array type of int[] is int.
     *
     * @param clazz
     *     The {@link Class} to get the array type of
     *
     * @return The {@link Class type} of the array, or the passed in {@link Class} if its not an array
     */
    public static Class<?> getArrayType(Class<?> clazz) {
        return clazz.isArray() ? clazz.getComponentType() : clazz;
    }

    /**
     * Retrieves the generic type of a {@link Type}. If the type is an array, it will return the type of the elements
     * in the array.
     *
     * @param type
     *     The {@link Type} to find the generic type of
     *
     * @return The {@link Type generic type}
     */
    public static Type getGenericType(Type type) {
        if (type instanceof Class<?>) {
            return Reflect.getArrayType((Class<?>) type);
        } else if (type instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) type;
            Type[] genericTypes = parameterizedType.getActualTypeArguments();
            if (0 < genericTypes.length)
                return genericTypes[0];
        }
        return Void.class;
    }

    /**
     * Retrieves the {@link Type} as a raw {@link Class}. If the type is not an instance of Class, then it returns
     * {@code ((ParameterizedType) type).getRawType()}. Note: If the type is null then {@code Void.class} is returned.
     *
     * @param type
     *     The {@link Type}
     *
     * @return The type as a {@link Class}
     */
    public static Class<?> asClass(@Nullable Type type) {
        if (null == type) return Void.class;

        return (Class<?>) (type instanceof Class<?>
            ? type : ((ParameterizedType) type).getRawType());
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
    public static <T> T createInstance(Class<T> clazz, Object... parameters) {
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

    /**
     * Converts a {@link Collection} to an array. Unlike {@link HalpbotUtils#toArray(Class, Collection)}, this doesn't
     * cast the result to the type the array, which can be useful in situations where the collection elements might
     * be {@link Object} objects rather than the actual type of the array.
     *
     * @param arrayElementClass
     *     The {@link Class type} of the elements in the array
     * @param collection
     *     The {@link Collection} to convert to an array
     *
     * @return An array as an {@link Object} containing the elemts in the {@link Collection}
     */
    public static Object toArray(Class<?> arrayElementClass, Collection<?> collection) {
        Object array = Array.newInstance(arrayElementClass, collection.size());
        Iterator<?> iterator = collection.iterator();
        int index = 0;
        while (iterator.hasNext()) {
            Array.set(array, index++, iterator.next());
        }

        return array;
    }

    /**
     * Returns if the specified {@link Class} is assignable to any of the classes in the {@link Collection}
     *
     * @param clazz
     *     The {@link Class} that you want to check if its assignable to
     * @param to
     *     The {@link Collection} of classes that the class could be assignable to
     *
     * @return If the specified {@link Class} is assignable to any of the classes in the {@link Collection}
     */
    public static boolean isAssignableTo(Class<?> clazz, Collection<Class<?>> to) {
        if (to.contains(clazz))
            return true;

        for (Class<?> toElement : to) {
            if (toElement.isAssignableFrom(clazz))
                return true;
        }
        return false;
    }

    /**
     * Returns an {@link Optional} containing the {@link Class} which the specified class is assignable to
     *
     * @param clazz
     *     The {@link Class} that you want to check if its assignable to
     * @param to
     *     The {@link Collection} of classes that the class could be assignable to
     *
     * @return An {@link Optional} containing the {@link Class} which the specified class is assignable to
     */
    public static Optional<Class<?>> getAssignableTo(Class<?> clazz, Collection<Class<?>> to) {
        if (to.contains(clazz))
            return Optional.of(clazz);

        for (Class<?> toElement : to) {
            if (toElement.isAssignableFrom(clazz))
                return Optional.of(toElement);
        }
        return Optional.empty();
    }

    /**
     * Returns if the passed in {@link String value} is a valid value of the {@link Enum}.
     * Note: This method is case-insensitive.
     *
     * @param enumType
     *     The {@link TypeContext} of the {@link Enum}
     * @param value
     *     The {@link String value} to test
     *
     * @return if the {@link String value} is a valid value of the {@link Enum}
     */
    public static Exceptional<Enum<?>> parseEnumValue(TypeContext<Enum<?>> enumType,
                                                      String value)
    {
        return Exceptional.of(
            enumType.enumConstants().stream()
                .filter(e -> e.name().equalsIgnoreCase(value))
                .findFirst())
            .orElse(() -> {
                throw new IllegalArgumentException(
                    "%s doesn't seem to be a valid value for the enum %s".formatted(value, enumType.name()));
            });
    }

    /**
     * Casts the specified object to the return type without checking.
     *
     * @param value
     *     The object to cast
     * @param <R>
     *     The return type
     * @param <T>
     *     The type of the value
     *
     * @return The casted value
     */
    @SuppressWarnings("unchecked")
    public static <R, T> R cast(T value) {
        return (R) value;
    }
}
