package nz.pumbas.utilities;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.annotation.Annotation;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.AnnotatedElement;
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

    /**
     * Returns if the passed in {@link String value} is a valid value of the {@link Enum}.
     * Note: This method is case sensitive.
     *
     * @param enumClass
     *      The {@link Class} of the {@link Enum}
     * @param value
     *      The {@link String value} to test
     *
     * @return if the {@link String value} is a valid value of the {@link Enum}
     */
    @SuppressWarnings("unchecked")
    public static boolean isValidValue(Class<?> enumClass, String value) {
        if (!enumClass.isEnum())
            throw new IllegalArgumentException(
                String.format("The type %s must be an enum.", enumClass.getSimpleName()));

        try {
            Enum.valueOf((Class<? extends Enum>) enumClass, value);
            return true;
        } catch (IllegalArgumentException exception) {
            return false;
        }
    }
}
