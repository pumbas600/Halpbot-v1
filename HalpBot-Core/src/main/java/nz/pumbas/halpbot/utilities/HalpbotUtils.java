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

import net.dv8tion.jda.api.JDA;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Properties;
import java.util.Queue;
import java.util.Random;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import nz.pumbas.halpbot.objects.Exceptional;
import nz.pumbas.halpbot.utilities.context.ContextHandler;
import nz.pumbas.halpbot.utilities.context.ContextHandlerImpl;
import nz.pumbas.halpbot.utilities.context.DefaultContext;
import nz.pumbas.halpbot.utilities.functionalinterfaces.IOFunction;

public final class HalpbotUtils
{

    //A constant 'empty' UUID
    public static final UUID emptyUUID = UUID.fromString("00000000-0000-0000-0000-000000000000");

    public static final float fullRotation = 360F;
    public static final float threeQuarterRotation = 270F;
    public static final float halfRotation = 180F;
    public static final float quarterRotation = 90F;

    private static final ContextHandler contextHandler = new ContextHandlerImpl();
    private static final Logger logger = LogManager.getLogger("HalpBot-Core-Bot");

    private static JDA jda;

    private HalpbotUtils() {}

    static {
        DefaultContext.addAll();
    }

    public static ContextHandler context() {
        return contextHandler;
    }

    public static Logger logger() {
        return logger;
    }

    public static JDA getJDA() {
        return jda;
    }

    public static void setJDA(JDA jdaInstance) {
        jda = jdaInstance;
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
    public static <T> void dequeueForEach(Queue<T> queue, Consumer<T> consumer) {
        while (!queue.isEmpty()) {
            consumer.accept(queue.remove());
        }
    }

    /**
     * Retrieves all the lines from a resource file as a {@link List<String>}.
     *
     * @param filename
     *     The {@link String name} of the resource file
     *
     * @return A {@link List<String>} with all the lines from the file in it
     */
    public static List<String> getAllLinesFromFile(String filename) {
        try {
            return Files.readAllLines(
                Paths.get(ClassLoader.getSystemClassLoader()
                    .getResource(filename)
                    .toURI()));
        } catch (IOException | URISyntaxException e) {
            ErrorManager.handle(e);
            return new ArrayList<>();
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
    public static String getFirstLineFromFile(String filename) {
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
    public static <T> T parseFile(String filename, IOFunction<BufferedReader, T> fileParser, T defaultValue) {

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
     * Returns the resource {@link File} or null if there is an error while trying to get it.
     *
     * @param filename
     *      The name of the file in the resource folder
     *
     * @return The {@link File} for the resource
     */
    @Nullable
    public static File getResourceFile(String filename) {
        try {
            return new File(
                ClassLoader.getSystemClassLoader()
                    .getResource(filename)
                    .toURI());
        } catch (URISyntaxException e) {
            ErrorManager.handle(e);
        }
        return null;
    }

    /**
     * Retrieves the InputStream for a file in the resources folder.
     *
     * @param filename
     *     The name of the file to retrieve
     *
     * @return The InputStream, if there were no errors retrieving it
     */
    private static Optional<InputStream> retrieveReader(String filename) {
        InputStream in = ClassLoader.getSystemClassLoader().getResourceAsStream(filename);

        if (null == in) {
            return Optional.empty();
        }
        return Optional.of(in);
    }

    /**
     * A simple property file parser.
     *
     * @param filename
     *     The name of the property file
     *
     * @return A {@link Map} of the parsed properties
     */
    public static Map<String, String> parsePropertyFile(String filename) {
        if (!filename.endsWith(".properties"))
            throw new IllegalArgumentException(
                String.format("The filename, %s does not end with .properties", filename));

        Map<String, String> propertyMap = new HashMap<>();
        return retrieveReader(filename)
            .map(inputStream -> {
                Properties properties = new Properties();

                try {
                    properties.load(inputStream);
                    for (Entry<Object, Object> entry : properties.entrySet()) {
                        propertyMap.put((String) entry.getKey(), (String) entry.getValue());
                    }
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return propertyMap;

            }).orElse(propertyMap);
    }

    /**
     * Gets the full path of a resource if its present.
     *
     * @param filename
     *     The filename of the resource
     *
     * @return The full path to the resource
     */
    public static Exceptional<String> retrieveResourcePath(String filename) {
        URL url = ClassLoader.getSystemClassLoader().getResource(filename);
        if (null == url)
            return Exceptional.empty();
        return Exceptional.of(url.getPath());
    }

    /**
     * A simple csv parser, which automatically splits the rows.
     *
     * @param filename
     *     The name of the csv to parse
     *
     * @return A {@link List} of split rows.
     */
    public static List<String[]> parseCSVFile(String filename) {
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
    public static String replaceFirst(@NotNull String str, @NotNull String target, @NotNull String replacement) {
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
    public static String capitalise(String str) {
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
    public static String capitaliseEachWord(@NotNull String str) {
        String[] words = str.split(" ");
        return Arrays.stream(words)
            .map(HalpbotUtils::capitalise)
            .collect(Collectors.joining(" "));
    }

    /**
     * Combines 4, 256 bit ints for alpha, red, green and blue into a single int that can be used in images.
     *
     * @param a
     *     A 256 bit number describing the alpha
     * @param r
     *     A 256 bit number describing the red
     * @param g
     *     A 256 bit number describing the green
     * @param b
     *     A 256 bit number describing the blue
     *
     * @return A single int, containing the alpha, red, green and blue information
     */
    @SuppressWarnings({"OverlyComplexBooleanExpression", "MagicNumber"})
    public static int argbToInt(int a, int r, int g, int b) {
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    /**
     * Determines if two values are approximately equal, by checking if the difference between them is less than the
     * specified tolerance.
     *
     * @param a
     *     The first value
     * @param b
     *     The second value
     * @param tolerance
     *     How similar they need to be, to be considered approximately equal
     *
     * @return if they're approximately equal
     */
    public static boolean approximatelyEqual(double a, double b, double tolerance) {
        return Math.abs(a - b) < tolerance;
    }

    /**
     * Converts a {@link Collection} to an array.
     *
     * @param arrayElementClass
     *     The {@link Class} of the elements in the array
     * @param collection
     *     The {@link Collection} to convert to an array
     * @param <T>
     *     The type of the elements in the array
     *
     * @return An array of the elements in the {@link Collection}
     */
    @SuppressWarnings({"unchecked", "SuspiciousToArrayCall"})
    public static <T> T[] toArray(Class<T> arrayElementClass, Collection<?> collection) {
        T[] array = collection.toArray((T[]) Array.newInstance(arrayElementClass, collection.size()));
        Iterator<?> iterator = collection.iterator();
        int index = 0;
        while (iterator.hasNext()) {
            Array.set(array, index++, iterator.next());
        }
        return array;
    }

    /**
     * Randomly chooses an element in the {@link List}.
     *
     * @param list
     *     The {@link List} to choose the element from
     * @param <T>
     *     The type of the elements in the list
     *
     * @return The randomly choosen element
     */
    public static <T> T randomChoice(List<T> list) {
        if (list.isEmpty())
            throw new IllegalArgumentException("Can't randomly choose an element from an empty list");

        Random random = new Random();
        return list.get(random.nextInt(list.size()));
    }

    /**
     * Randomly chooses an element in the array.
     *
     * @param array
     *     The array to choose the element from
     * @param <T>
     *     The type of the elements in the array
     *
     * @return The randomly choosen element
     */
    public static <T> T randomChoice(T[] array) {
        if (0 == array.length)
            throw new IllegalArgumentException("Can't randomly choose an element from an empty array");

        Random random = new Random();
        return array[random.nextInt(array.length)];
    }

    /**
     * Adds all of a {@link Collection} to another {@link Collection} and returns the collection.
     *
     * @param collection
     *     The {@link Collection} to have the other collection added to
     * @param toAdd
     *     The {@link Collection} to add to the collection
     * @param <TV>
     *     The type of the values in the collection
     * @param <TC>
     *     The type of the collection
     *
     * @return The {@link Collection}
     */
    public static <TV, TC extends Collection<TV>> TC addAll(TC collection, TC toAdd) {
        collection.addAll(toAdd);
        return collection;
    }
}
