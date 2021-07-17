package nz.pumbas.commands.tokens.context;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import nz.pumbas.utilities.Exceptional;

public class InvocationContext
{
    protected final String original;
    protected int currentIndex;
    protected final Map<Object, Integer> savedIndices = new HashMap<>();

    protected InvocationContext(@NotNull String context)
    {
        this.original = context;
    }

    /**
     * Creates an {@link InvocationContext} from the passed {@link String invocation token}.
     *
     * @param context
     *      The {@link String invocation token} to create this {@link InvocationContext} from
     *
     * @return A new {@link InvocationContext}
     */
    public static InvocationContext of(String context)
    {
        return new InvocationContext(context);
    }

    /**
     * @return If there are any more tokens left.
     */
    public boolean hasNext()
    {
        return this.currentIndex < this.original.length();
    }

    /**
     * @return The next {@link String token} (Split on spaces) from the current index or an empty
     * {@link String} if there are no more tokens;
     */
    @NotNull
    public String getNext()
    {
        if (!this.hasNext())
            return "";

        int endIndex = this.original.indexOf(" ", this.currentIndex);

        if (-1 == endIndex) endIndex = this.original.length();

        String match = this.original.substring(this.currentIndex, endIndex);
        this.currentIndex = endIndex + 1;

        return match;
    }

    /**
     * Gets the next string up until (exclusive) the specified string. If the ending string is not contained in the
     * remainder of the invocationToken, an empty optional is returned. This will automatically step past the until
     * characters.
     *
     * @param until
     *      The {@link String} to stop at
     *
     * @return an {@link Optional} containing the next string up until the specified string
     */
    public Optional<String> getNext(String until)
    {
        return this.getNext(until, true);
    }

    /**
     * Gets the next string up until (exclusive) the specified string. If the ending string is not contained in the
     * remainder of the invocationToken, an empty optional is returned.
     *
     * @param until
     *      The {@link String} to stop at
     * @param stepPast
     *      Should it move the current index past the until characters, or remain on it
     *
     * @return an {@link Optional} containing the next string up until the specified string
     */
    public Optional<String> getNext(String until, boolean stepPast)
    {
        if (!this.hasNext())
            return Optional.empty();

        int endIndex = this.original.indexOf(until, this.currentIndex);
        if (-1 == endIndex)
            return Optional.empty();

        String match = this.original.substring(this.currentIndex, endIndex);
        this.currentIndex = endIndex;

        if (stepPast) {
            this.currentIndex += until.length();
            if (this.currentlyOnSpace())
                this.currentIndex++;
        }
        return Optional.of(match);
    }

    /**
     * Returns the next string which is contained between the start and stop specified. This will automatically step
     * past the stop characters. It will respect nested start and stops too.
     * <p>
     * E.g: for {@code [#Block[1 2 3] #Block[2 3 4]]}, {@code #getNextSurrounded("[", "]")} will return
     * {@code #Block[1 2 3] #Block[2 3 4]}.
     *
     * @param start
     *      The {@link String} defining the starting characters
     * @param stop
     *      The {@link String} defining the stopping characters
     *
     * @return The {@link String} between the start and stop
     */
    public Optional<String> getNextSurrounded(String start, String stop)
    {
        return this.getNextSurrounded(start, stop, true);
    }

    /**
     * Returns the next string which is contained between the start and stop specified. If the next character after
     * the stop is a space, it will automatically move to the next character. It will respect nested start and stops
     * too.
     * <p>
     * E.g: for {@code [#Block[1 2 3] #Block[2 3 4]]}, {@code #getNextSurrounded("[", "]")} will return
     * {@code #Block[1 2 3] #Block[2 3 4]}.
     *
     * @param start
     *      The {@link String} defining the starting characters
     * @param stop
     *      The {@link String} defining the stopping characters
     * @param stepPast
     *      Should it move the current index past the stop characters, or remain on it
     *
     * @return The {@link String} between the start and stop
     */
    public Optional<String> getNextSurrounded(String start, String stop, boolean stepPast)
    {
        if (!this.hasNext() || this.original.indexOf(start, this.currentIndex) != this.currentIndex)
            return Optional.empty();

        int startCount = 1;
        int startIndex = this.currentIndex + 1;
        int endIndex = this.currentIndex;
        do {
            endIndex = this.original.indexOf(stop, endIndex + 1);
            if (-1 == endIndex)
                return Optional.empty();

            startCount--;

            int tempStartIndex;
            while ((tempStartIndex = this.original.indexOf(start, startIndex)) < endIndex && -1 != tempStartIndex) {
                startCount++;
                startIndex = tempStartIndex + 1;
            }
        } while (0 != startCount);

        String match = this.original.substring(this.currentIndex + 1, endIndex);
        this.currentIndex = endIndex;

        if (stepPast) {
            this.currentIndex += stop.length();
            if (this.currentlyOnSpace())
                this.currentIndex++;
        }

        return Optional.of(match);
    }

    /**
     * Determine if the specified string is next. If it is, then it steps past.
     *
     * @param next
     *      The string to check if is next
     *
     * @return If the specified string is next
     */
    public boolean nextMatches(@NotNull String next) {
        int endIndex = this.currentIndex + next.length();
        if (this.original.substring(this.currentIndex, endIndex).equalsIgnoreCase(next)) {
            this.currentIndex = endIndex;
            if (this.currentlyOnSpace())
                this.incrementIndex();
            return true;
        }
        return false;
    }

    /**
     * Gets the next {@link String token} that matches the specified {@link Pattern}. If the next token doesn't match
     * the {@link Pattern} then an empty {@link Optional} is returned.
     *
     * @param pattern
     *      The {@link Pattern} for the next token
     *
     * @return An {@link Optional} containing the matched {@link String pattern} if present
     */
    public Exceptional<String> getNext(@NotNull Pattern pattern)
    {
        if (this.hasNext()) {
            String originalSubstring = this.original.substring(this.currentIndex);
            Matcher matcher = pattern.matcher(originalSubstring);
            if (matcher.lookingAt()) { //Will return true if the start of the string matches the Regex
                String match = originalSubstring.substring(0, matcher.end());
                this.currentIndex += matcher.end();

                //If there's a space right after the match, skip past it
                if (this.hasNext() && ' ' == this.original.charAt(this.currentIndex))
                    this.currentIndex++;

                return Exceptional.of(match);
            }
        }
        return Exceptional.empty();
    }

    /**
     * @return The original string
     */
    public String getOriginal()
    {
        return this.original;
    }

    /**
     * @return The current index
     */
    public int getCurrentIndex()
    {
        return this.currentIndex;
    }

    /**
     * Saves the current index so that it can be restored later if necessary.
     *
     * @param reference
     *      The {@link Object} to store the index against
     *
     * @return Itself
     */
    public InvocationContext saveState(Object reference)
    {
        this.savedIndices.put(reference, this.currentIndex);
        return this;
    }

    /**
     * Restores the current index to the last saved index.
     *
     * @param reference
     *      The {@link Object} to store the index against
     *
     * @return Itself
     */
    public InvocationContext restoreState(Object reference)
    {
        this.currentIndex = this.savedIndices.getOrDefault(reference, this.currentIndex);
        return this;
    }

    /**
     * @return If the current character is a space
     */
    private boolean currentlyOnSpace()
    {
        return this.hasNext() && ' ' == this.original.charAt(this.currentIndex);
    }

    /**
     * Sets the current index.
     *
     * @param index
     *      Set the current index to the specified index
     */
    public void setCurrentIndex(int index)
    {
        this.currentIndex = index;
    }

    public void incrementIndex() {
        this.currentIndex++;
    }
}
