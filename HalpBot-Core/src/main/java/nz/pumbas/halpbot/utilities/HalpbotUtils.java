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

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.interactions.components.Button;
import net.dv8tion.jda.api.interactions.components.Component;
import net.dv8tion.jda.api.interactions.components.selections.SelectionMenu;

import org.dockbox.hartshorn.core.domain.Exceptional;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Array;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Properties;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import nz.pumbas.halpbot.objects.AsyncDuration;
import nz.pumbas.halpbot.utilities.functionalinterfaces.IOFunction;

public final class HalpbotUtils
{
    public static final Color Blurple = new Color(85, 57, 204);

    public static final float fullRotation = 360F;
    public static final float threeQuarterRotation = 270F;
    public static final float halfRotation = 180F;
    public static final float quarterRotation = 90F;
    public static final Object IGNORE_RESULT = new Object();

    private HalpbotUtils() {}

    /**
     * Converts the {@link Duration} annotation to the built-in {@link java.time.Duration}.
     *
     * @param duration
     *     The duration annotation to represent as a {@link java.time.Duration}
     *
     * @return The converted duration
     */
    public static java.time.Duration asDuration(Duration duration) {
        return java.time.Duration.of(duration.value(), duration.unit());
    }

    public static AsyncDuration asAsyncDuration(Duration duration) {
        return new AsyncDuration(duration.value(), TimeUnit.of(duration.unit()));
    }

    /**
     * When you set a {@link net.dv8tion.jda.api.entities.MessageEmbed} description that is larger than {@link
     * MessageEmbed#DESCRIPTION_MAX_LENGTH} it will cause an error to be thrown. This method simply checks if the
     * description exceeds this limit and if it does, then it creates a substring of the maximum displayable message.
     *
     * @param description
     *     The description to make sure doesn't exceed the character count
     *
     * @return The checked description
     */
    public static String limitEmbedDescriptionLength(String description) {
        return limitLength(description, MessageEmbed.DESCRIPTION_MAX_LENGTH);
    }

    public static String limitMessageLength(String message) {
        return limitLength(message, Message.MAX_CONTENT_LENGTH);
    }

    private static String limitLength(String string, int length) {
        if (string.length() > length)
            return string.substring(0, length - 3) + "...";
        return string;
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
     * @return A {@link List<String>} with all the lines from the file in it
     */
    public static List<String> getAllLinesFromFile(InputStream inputStream) {
        return parseFile(
            inputStream,
            br -> br.lines().collect(Collectors.toList()),
            Collections.emptyList());
    }

    /**
     * Retrieves the first line from a file.
     *
     * @return The first line, or an empty {@link String} if there was an error reading the line
     */
    public static String getFirstLine(InputStream inputStream) {
        return parseFile(inputStream, BufferedReader::readLine, "");
    }

    public static String firstLine(String filename) {
        return Objects.requireNonNull(retrieveReader(filename)
            .map(in -> parseFile(in, BufferedReader::readLine, ""))
            .or(""));
    }

    /**
     * Parses a file using the {@link IOFunction fileParser} provided.
     *
     * @param fileParser
     *     The {@link IOFunction} to parse the file
     * @param defaultValue
     *     The value to be returned if there is an error opening the file
     * @param <T>
     *     The type of the parsed file
     *
     * @return The parsed file
     */
    public static <T> T parseFile(InputStream inputStream, IOFunction<BufferedReader, T> fileParser,
                                  T defaultValue) {
        try (InputStreamReader inputReader = new InputStreamReader(inputStream)) {
            BufferedReader reader = new BufferedReader(inputReader);
            T parsedFile = fileParser.apply(reader);
            reader.close();

            return parsedFile;

        } catch (IOException e) {
            ErrorManager.handle(e);
            return defaultValue;
        }
    }

    /**
     * Returns the resource {@link File} or null if there is an error while trying to get it.
     *
     * @param filename
     *     The name of the file in the resource folder
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
    public static Exceptional<InputStream> retrieveReader(String filename) {
        return Exceptional.of(ClassLoader.getSystemClassLoader().getResourceAsStream(filename));
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
        return Objects.requireNonNull(retrieveReader(filename)
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

            }).or(propertyMap));
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
        if (str.isEmpty()) return str;

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
    public static String capitaliseWords(@NotNull String str) {
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

    /**
     * Removes the first element in the collection that passes the filter wrapped in an {@link Exceptional}. If no
     * elements in the collection pass the filter than nothing is removed and an empty exceptional is returned.
     *
     * @param collection
     *     The collection to remove the element from
     * @param filter
     *     The filter which returns true if the element is to be removed
     * @param <T>
     *     The type of the elements in the collection
     *
     * @return The removed element wrapped in an {@link Exceptional}.
     */
    public static <T> Exceptional<T> removeFirst(Collection<T> collection, Predicate<T> filter) {
        for (T next : collection) {
            if (filter.test(next)) {
                collection.remove(next);
                return Exceptional.of(next);
            }
        }

        return Exceptional.empty();
    }

    /**
     * Converts the {@link Throwable}'s stack trace into a string.
     *
     * @param throwable
     *     The {@link Throwable} to get the stacktrace from
     *
     * @return The string representation of the stack trace
     */
    public static String getStackTrace(final Throwable throwable) {
        final StringWriter sw = new StringWriter();
        final PrintWriter pw = new PrintWriter(sw, true);
        throwable.printStackTrace(pw);
        return sw.getBuffer().toString();
    }

    /**
     * Creates an array containing both elements, with a being first, then b. If a is empty b is returned and if b is
     * empty then a is returned. Otherwise, a new array of length {@code a.length + b.length} is created.
     *
     * @param a
     *     The first array
     * @param b
     *     The second array
     *
     * @return The combined array
     */
    public static Object[] combine(Object[] a, Object... b) {
        if (0 == b.length) return a;
        if (0 == a.length) return b;

        Object[] combinedArray = new Object[a.length + b.length];
        System.arraycopy(a, 0, combinedArray, 0, a.length);
        System.arraycopy(b, 0, combinedArray, a.length, b.length);
        return combinedArray;
    }

    public static int longestLength(String... strings) {
        return longestLength(List.of(strings));
    }

    /**
     * Returns the length of the longest string in the strings passed in. If no strings are passed, then it will return
     * 0.
     *
     * @param strings
     *     The strings to find the longest length from
     *
     * @return The length of the longest string
     */
    public static int longestLength(List<String> strings) {
        if (strings.isEmpty())
            return 0;

        int longest = strings.get(0).length();
        for (int i = 1; i < strings.size(); i++) {
            int length = strings.get(i).length();
            if (length > longest) {
                longest = length;
            }
        }
        return longest;
    }

    /**
     * Converts camelCase and PascalCase to split lowercase. For example camelCase would become 'camel case'.
     *
     * @param text
     *     The camelCase or PascalCase text to convert
     *
     * @return the converted text
     */
    public static String variableNameToSplitLowercase(String text) {
        return splitVariableName(text).toLowerCase(Locale.ROOT);
    }


    /**
     * Splits camelCase and PascalCase. For example camelCase would become 'camel Case'.
     *
     * @param text
     *     The camelCase or PascalCase text to convert
     *
     * @return the converted text
     */
    public static String splitVariableName(String text) {
        return text.replaceAll("([a-z])([A-Z])", "$1 $2");
    }

    /**
     * returns the disabled version of a {@link Component} by checking if it's either a {@link Button} or a {@link
     * SelectionMenu} and calling their appropriate {@code #asDisabled} method. If the component is neither of those
     * then it's returned as is.
     *
     * @param component
     *     The {@link Component} to disable
     *
     * @return The disabled component
     */
    public static Component disable(Component component) {
        if (component instanceof Button button)
            return button.asDisabled();
        else if (component instanceof SelectionMenu selectionMenu)
            return selectionMenu.asDisabled();
        return component;
    }
}
