package nz.pumbas.halpbot.commands.context;

import org.dockbox.hartshorn.core.context.ContextCarrier;
import org.dockbox.hartshorn.core.context.element.ParameterContext;
import org.dockbox.hartshorn.core.context.element.TypeContext;
import org.dockbox.hartshorn.core.domain.Exceptional;
import org.jetbrains.annotations.NotNull;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import javax.annotation.Nullable;

import nz.pumbas.halpbot.commands.exceptions.IllegalFormatException;
import nz.pumbas.halpbot.events.HalpbotEvent;

public interface InvocationContext extends ContextCarrier
{
    String content();

    @Nullable
    HalpbotEvent halpbotEvent();

    Set<TypeContext<?>> reflections();

    int currentIndex();

    int currentAnnotationIndex();

    TypeContext<?> currentType();

    ParameterContext<?> parameterContext();

    List<TypeContext<? extends Annotation>> sortedAnnotations();

    Set<Annotation> annotations();

    InvocationContext currentIndex(int index);

    InvocationContext currentAnnotationIndex(int index);

    InvocationContext currentType(TypeContext<?> typeContext);

    InvocationContext sortedAnnotations(List<TypeContext<? extends Annotation>> sortedAnnotations);

    InvocationContext annotations(Set<Annotation> annotations);

    InvocationContext parameterContext(@Nullable ParameterContext<?> parameterContext);

    /**
     * @return If there is any more content left
     */
    default boolean hasNext() {
        return this.currentIndex() < this.content().length();
    }

    /**
     * @return An {@link Exceptional} containing the next {@link String word} from the current index or
     *         {@link Exceptional#empty()} if there are no more words
     * @see InvocationContext#next()
     */
    Exceptional<String> nextSafe();

    /**
     * @return The next {@link String word} from the current index or an empty {@link String} if there are no more words
     * @see InvocationContext#nextSafe()
     */
    default String next() {
        return this.nextSafe().or("");
    }

    /**
     * Gets the next string up until (exclusive) the specified string. If the ending string is not contained in the
     * remainder of the content, an {@link Exceptional#empty()} is returned. This will automatically move the
     * current index past the until string and any whitespace.
     *
     * @param until
     *     The {@link String} to stop at
     *
     * @return an {@link Exceptional} containing the next string up until the specified string
     * @see InvocationContext#next(String, boolean) 
     * @see InvocationContext#next(Pattern) 
     */
    default Exceptional<String> next(String until) {
        return this.next(until, true);
    }

    /**
     * Gets the next string up until (exclusive) the specified string. If the ending string is not contained in the
     * remainder of the content, an {@link Exceptional#empty()} is returned.
     *
     * @param until
     *     The {@link String} to stop at
     * @param stepPast
     *     Should it automatically move the current index past the until string and any whitespace, or remain on it
     *
     * @return an {@link Exceptional} containing the next string up until the specified string
     * @see InvocationContext#next(String)
     */
    Exceptional<String> next(String until, boolean stepPast);

    /**
     * Gets the next string that matches the specified {@link Pattern}. If there is no matching string or the
     * matching string is not next then an {@link Exceptional} containing an {@link IllegalFormatException} is returned.
     *
     * @param pattern
     *     The {@link Pattern} for the next string
     *
     * @return An {@link Exceptional} containing the matched {@link String pattern}
     * @see InvocationContext#next(String) 
     */
    Exceptional<String> next(Pattern pattern);

    /**
     * Returns the next string which is contained between the start and stop specified while respecting any nested
     * start and stops too. This will automatically step past the stop characters and any whitespace.
     * <p>
     * E.g: for {@code [#Block[1 2 3] #Block[2 3 4]]}, {@code nextSurrounded("[", "]")} will return
     * {@code #Block[1 2 3] #Block[2 3 4]}.
     *
     * @param start
     *     The {@link String} defining the starting characters
     * @param stop
     *     The {@link String} defining the stopping characters
     *
     * @return An {@link Exceptional} containing the string between the start and stop
     * @see InvocationContext#nextSurrounded(String, String, boolean)
     */
    default Exceptional<String> nextSurrounded(String start, String stop) {
        return this.nextSurrounded(start, stop, true);
    }

    /**
     * Returns the next string which is contained between the start and stop specified while respecting any nested
     * start and stops too.
     * <p>
     * E.g: for {@code [#Block[1 2 3] #Block[2 3 4]]}, {@code #nextSurrounded("[", "]", false)} will return
     * {@code #Block[1 2 3] #Block[2 3 4]}.
     *
     * @param start
     *     The {@link String} defining the starting characters
     * @param stop
     *     The {@link String} defining the stopping characters
     * @param stepPast
     *     Should it move the current index past the stop characters and any whitespace, or remain on it
     *
     * @return An {@link Exceptional} containing the string between the start and stop
     * @see InvocationContext#nextSurrounded(String, String)
     */
    Exceptional<String> nextSurrounded(String start, String stop, boolean stepPast);

    /**
     * Determine if the specified string is next. If it is, then it steps past.
     *
     * @param next
     *     The string to check if is next
     *
     * @return If the specified string is next
     */
    default boolean nextMatches(String next) {
        int endIndex = this.currentIndex() + next.length();
        if (this.content().substring(this.currentIndex(), endIndex).equalsIgnoreCase(next)) {
            this.currentIndex(endIndex);
            if (this.currentlyOnSpace())
                this.incrementIndex();
            return true;
        }
        return false;
    }

    /**
     * @return The remaining strings
     * @throws IllegalFormatException If there are no remaining strings
     */
    default String remaining() {
        if (!this.hasNext())
            throw new IllegalFormatException("There are no remaining strings");

        String remaining = this.content().substring(this.currentIndex());
        this.currentIndex(this.content().length());
        return remaining;
    }

    /**
     * @return If the current character is a space
     */
    default boolean currentlyOnSpace() {
        return this.hasNext() && ' ' == this.content().charAt(this.currentIndex());
    }

    /**
     * Increments the current index by one.
     */
    void incrementIndex();

    /**
     * While the current index is on a space, it will continue incrementing the current index.
     */
    default void skipPastSpaces() {
        while (this.currentlyOnSpace())
            this.incrementIndex();
    }

    /**
     * Returns if the character at the current index is the one specified. If it is, then it steps past this
     * character automatically, along with any whitespace.
     *
     * @param character
     *     The character to check
     *
     * @return If the character is at the current index
     * @see InvocationContext#isNext(char, boolean)
     */
    default boolean isNext(char character) {
        return this.isNext(character, true);
    }

    /**
     * Returns if the character at the current index is the one specified. If you want to step past the character, it
     * will also step past any whitespace.
     *
     * @param character
     *     The character to check
     * @param stepPast
     *     If the character should be stepped past
     *
     * @return If the character is at the current index
     * @see InvocationContext#isNext(char)
     */
    default boolean isNext(char character, boolean stepPast) {
        if (!this.hasNext())
            return false;

        boolean isNext = this.content().charAt(this.currentIndex()) == character;

        if (isNext && stepPast) {
            this.incrementIndex();
            this.skipPastSpaces();
        }
        return isNext;
    }

    /**
     * Assets that the next character is the one specified. If it's not, then it throws an
     * {@link IllegalFormatException}.
     *
     * @param character
     *     The character to assert is next
     *
     * @see InvocationContext#isNext(char)
     */
    default void assertNext(char character) {
        if (!this.isNext(character)) {
            String found = this.hasNext() ? "" + this.content().charAt(this.currentIndex()) : "End of content";
            throw new IllegalFormatException("Expected the character '%s' but found '%s'".formatted(character, found));
        }
    }

    /**
     * Updates the current type, sorted annotations, parameter annotations and resets the current annotation index
     * using the {@link ParameterContext} and provided sorted annotations.
     *
     * @param parameterContext
     *      The {@link ParameterContext} to get the current type and annotations from
     * @param sortedAnnotations
     *      The new sorted annotations to use
     */
    default void update(ParameterContext<?> parameterContext,
                        List<TypeContext<? extends Annotation>> sortedAnnotations)
    {
        this.parameterContext(parameterContext);
        this.sortedAnnotations(sortedAnnotations);
        this.currentAnnotationIndex(0);
    }

    /**
     * Increments the {@link InvocationContext#currentAnnotationIndex()} by one.
     */
    void incrementAnnotationIndex();

    /**
     * Retrieves an {@link Exceptional} containing the first annotation with the specified type, or
     * {@link Exceptional#empty()} if there are no matching annotations.
     *
     * @param annotationType
     *      The {@link Class} of the annotation
     * @param <T>
     *      The type of the annotation
     *
     * @return An {@link Exceptional} containing the annotation, or nothing if no matching annotations were found
     */
    @SuppressWarnings("unchecked")
    default  <T extends Annotation> Exceptional<T> annotation(Class<T> annotationType) {
        return Exceptional.of(this.annotations().stream()
                .filter(annotation -> annotationType.isAssignableFrom(annotation.annotationType()))
                .findFirst()
                .map(annotation -> (T) annotation));
    }
}
