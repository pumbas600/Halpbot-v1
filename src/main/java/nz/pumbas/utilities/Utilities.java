package nz.pumbas.utilities;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.annotation.Annotation;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.UUID;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.imageio.ImageIO;

import nz.pumbas.commands.ErrorManager;
import nz.pumbas.utilities.functionalinterfaces.IOFunction;
import nz.pumbas.utilities.io.ImageType;

public final class Utilities
{

    //A constant 'empty' UUID
    public static final UUID emptyUUID = UUID.fromString("00000000-0000-0000-0000-000000000000");

    public static final float fullRotation = 360F;
    public static final float threeQuarterRotation = 270F;
    public static final float halfRotation = 180F;
    public static final float quarterRotation = 90F;

    public static final String line = "-----------------------------------------------------------";

    public static final Map<Class<?>, Function<String, Object>> TypeParsers = Map.of(
        String.class, s -> s,
        int.class, Integer::parseInt,
        float.class, Float::parseFloat,
        double.class, Double::parseDouble,
        char.class, s -> s.charAt(0)
    );

    private Utilities() {}

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
    public static List<Method> getAnnotatedMethods(Class<?> target, Class<? extends Annotation> annotation, boolean getSuperMethods, Predicate<Method>... filters)
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
    public static List<Method> getAnnotatedMethodsWithModifiers(Class<?> target, Class<? extends Annotation> annotation, boolean getSuperMethods, Predicate<Integer>... modifierFilters)
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
     * If a class has the specific annotation, retrieve a field using the {@link Function} otherwise return the
     * default value.
     *
     * @param target
     *     The class being checked for the annotation
     * @param annotationClass
     *     The class of the annotation
     * @param function
     *     The function to retrieve the annotation field
     * @param defaultValue
     *     The default value to return if the object doesn't have the annotation
     * @param <T>
     *     The type of the annotation
     * @param <R>
     *     The type of the annotation's field being retreived
     *
     * @return The annotation's field value
     */
    public static <T extends Annotation, R> R getAnnotationFieldElse(Class<?> target, Class<T> annotationClass,
                                                                     Function<T, R> function, R defaultValue)
    {
        if (target.isAnnotationPresent(annotationClass)) {
            return function.apply(target.getAnnotation(annotationClass));
        }
        return defaultValue;
    }

    /**
     * Returns the first {@link Method} in a class with the specified name.
     *
     * @param clazz
     *     The class to check, for the method with the specified name
     * @param name
     *     The name of the method to find
     *
     * @return An {@link Optional} containing the {@link Method}
     */
    public static Optional<Method> getMethod(Class<?> clazz, String name)
    {
        for (Method method : clazz.getDeclaredMethods()) {
            if (method.getName().equals(name)) {
                method.setAccessible(true);
                return Optional.of(method);
            }
        }
        return Optional.empty();
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
     * Returns true if the {@link Class} has the specified annotation.
     *
     * @param clazz
     *      The {@link Class} to check for the annotation
     * @param annotationType
     *      The type of the annotation
     *
     * @return whether the {@link Class} has the annotation
     */
    public static boolean hasAnnotation(Class<?> clazz, Class<? extends Annotation> annotationType)
    {
        return null != clazz.getAnnotation(annotationType);
    }

    /**
     * Returns true if the {@link Method} has the specified annotation.
     *
     * @param method
     *      The {@link Method} to check for the annotation
     * @param annotationType
     *      The type of the annotation
     *
     * @return whether the {@link Method} has the annotation
     */
    public static boolean hasAnnotation(Method method, Class<? extends Annotation> annotationType)
    {
        return null != method.getAnnotation(annotationType);
    }

    /**
     * Returns true if the {@link Field} has the specified annotation.
     *
     * @param field
     *      The {@link Field} to check for the annotation
     * @param annotationType
     *      The type of the annotation
     *
     * @return whether the {@link Field} has the annotation
     */
    public static boolean hasAnnotation(Field field, Class<? extends Annotation> annotationType)
    {
        return null != field.getAnnotation(annotationType);
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

    /**
     * Calls a method on each element in a queue as they're dequeued.
     *
     * @param queue
     *     The queue to loop through
     * @param consumer
     *     The method to apply each element in the queue
     * @param <T>
     *     The type of the elements in the queue
     */
    public static <T> void dequeueForEach(Queue<T> queue, Consumer<T> consumer)
    {
        while (!queue.isEmpty()) {
            consumer.accept(queue.remove());
        }
    }

    /**
     * Maps the values of a list to the passed in objects.
     *
     * @param list
     *     The {@link List} to get the values from
     * @param objects
     *     The objects to set the values for
     * @param <T>
     *     The type of the elements in the list
     */
    @SafeVarargs
    public static <T> void mapListToObjects(List<T> list, T... objects)
    {
        for (int i = 0; i < objects.length; i++) {
            if (i >= objects.length || i >= list.size()) break;

            objects[i] = list.get(i);
        }
    }

    /**
     * Retrieves the first line from a file.
     *
     * @param filename
     *     The name of the file to open
     *
     * @return The first line, or an empty {@link String} if there was an error reading the line
     */
    public static String getFirstLineFromFile(String filename)
    {
        return parseFile(filename, BufferedReader::readLine, "");
    }

    /**
     * Parses a file using the {@link IOFunction fileParser} provided.
     *
     * @param filename
     *     The name of the file to open
     * @param fileParser
     *     The {@link IOFunction} to parse the file
     * @param defaultValue
     *     The value to be returned if there is an error opening the file
     * @param <T>
     *     The type of the parsed file
     *
     * @return The parsed file
     */
    public static <T> T parseFile(String filename, IOFunction<BufferedReader, T> fileParser, T defaultValue)
    {

        Optional<InputStream> oIn = retrieveReader(filename);

        if (oIn.isPresent()) {
            InputStream in = oIn.get();
            try (InputStreamReader inputReader = new InputStreamReader(in)) {

                BufferedReader reader = new BufferedReader(inputReader);
                T parsedFile = fileParser.apply(reader);
                reader.close();

                return parsedFile;

            } catch (IOException e) {
                System.out.printf("There was an error trying to access the file %s%n", filename);
                e.printStackTrace();
            }
        }
        return defaultValue;
    }

    /**
     * Retrieves the InputStream for a file in the resources folder.
     *
     * @param filename
     *     The name of the file to retrieve
     *
     * @return The InputStream, if there were no errors retrieving it
     */
    private static Optional<InputStream> retrieveReader(String filename)
    {
        InputStream in = ClassLoader.getSystemClassLoader().getResourceAsStream(filename);

        if (null == in) {
            return Optional.empty();
        }
        return Optional.of(in);
    }

    /**
     * Gets the full path of a resource if its present.
     *
     * @param filename
     *      The filename of the resource
     *
     * @return The full path to the resource
     */
    public static Optional<String> retrieveResourcePath(String filename) {
        URL url = ClassLoader.getSystemClassLoader().getResource(filename);
        if (null == url)
            return Optional.empty();
        return Optional.of(url.getPath());
    }

    /**
     * A simple csv parser, which automatically splits the rows.
     *
     * @param filename
     *     The name of the csv to parse
     *
     * @return A {@link List} of split rows.
     */
    public static List<String[]> parseCSVFile(String filename)
    {
        if (!filename.endsWith(".csv"))
            throw new IllegalArgumentException(
                String.format("The filename, %s does not end with .csv", filename));

        return parseFile(filename, bufferedReader -> {
            List<String[]> lines = new ArrayList<>();

            String line;
            while (null != (line = bufferedReader.readLine())) {
                lines.add(line.split(","));
            }
            return lines;

        }, new ArrayList<>());
    }

    /**
     * Replaces the first occurance of the target in the passed str with the replacement without using regex.
     *
     * @param str
     *     The string being searched for the target
     * @param target
     *     The string to replace
     * @param replacement
     *     What to replace the target with
     *
     * @return The string with the first occurance of the target replaced with the replacement
     */
    public static String replaceFirst(@NotNull String str, @NotNull String target, @NotNull String replacement)
    {
        int targetIndex = str.indexOf(target);
        int endIndex = targetIndex + target.length();

        if (-1 != targetIndex) {
            String before = str.substring(0, targetIndex);
            String after = endIndex < str.length() ? str.substring(endIndex) : "";

            return before + replacement + after;
        }

        return str;
    }

    /**
     * Capitalises the first letter of the {@link String} and sets the rest of the word to lowercase.
     *
     * @param str
     *     The {@link String} to capitalise
     *
     * @return The capitalised {@link String}
     */
    public static String capitalise(String str)
    {
        if (null == str || str.isEmpty())
            return str;

        return str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
    }

    /**
     * Capitalises the first letter of each word in the {@link String} and sets the rest of the word to lowercase.
     *
     * @param str
     *     The {@link String sentence} to have each word capitalised
     *
     * @return The capitalised sentence
     */
    public static String capitaliseEachWord(@NotNull String str)
    {
        String[] words = str.split(" ");
        return Arrays.stream(words)
            .map(Utilities::capitalise)
            .collect(Collectors.joining(" "));
    }

    /**
     * Combines 4, 256 bit ints for alpha, red, green and blue into a single int that can be used in images.
     *
     * @param a
     *      A 256 bit number describing the alpha
     * @param r
     *      A 256 bit number describing the red
     * @param g
     *      A 256 bit number describing the green
     * @param b
     *      A 256 bit number describing the blue
     *
     * @return A single int, containing the alpha, red, green and blue information
     */
    @SuppressWarnings({"OverlyComplexBooleanExpression", "MagicNumber"})
    public static int argbToInt(int a, int r, int g, int b)
    {
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    /**
     * Generates a ARGB {@link BufferedImage} of the specified size, using an {@link BiFunction} which takes in a pixels x
     * and y position respectively and returns a integer specifying the colour for that pixel.
     *
     * @param width
     *      The width of the image in pixels
     * @param height
     *      The height of the image in pixels
     * @param positionFunction
     *      The {@link BiFunction} which specifies a pixels colour based on its x and y position respectively
     *
     * @return The generated {@link BufferedImage}
     */
    public static BufferedImage generateImageByPosition(int width, int height,
                                                        @NotNull BiFunction<Integer, Integer, Integer> positionFunction)
    {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                int colour = positionFunction.apply(x, y);
                image.setRGB(x, y, colour);
            }
        }

        return image;
    }

    /**
     * Converts an {@link BufferedImage} to an array of bytes.
     *
     * @param image
     *      The {@link BufferedImage} to convert to bytes
     * @param imageType
     *      The {@link ImageType} of the {@link BufferedImage}
     *
     * @return An array of bytes
     */
    public static byte[] toByteArray(BufferedImage image, ImageType imageType)
    {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ImageIO.write(image, imageType.getType(), outputStream);
            return outputStream.toByteArray();

        } catch (IOException e) {
            ErrorManager.handle(e);
        }

        return new byte[0];
    }

    /**
     * Determines if two values are approximately equal, by checking if the difference between them is less than the
     * specified tolerance.
     *
     * @param a
     *      The first value
     * @param b
     *      The second value
     * @param tolerance
     *      How similar they need to be, to be considered approximately equal
     *
     * @return if they're approximately equal
     */
    public static boolean approximatelyEqual(double a, double b, double tolerance)
    {
        return Math.abs(a - b) < tolerance;
    }
}
