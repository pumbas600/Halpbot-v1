package nz.pumbas.commands.tokens.tokensyntax;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class InvocationTokenInfo
{
    private final String original;
    private int currentIndex;
    private int savedIndex;

    protected InvocationTokenInfo(String invocationToken)
    {
        this.original = invocationToken;
    }

    /**
     * Creates an {@link InvocationTokenInfo} from the passed {@link String invocation token}.
     *
     * @param invocationToken
     *      The {@link String invocation token} to create this {@link InvocationTokenInfo} from
     *
     * @return A new {@link InvocationTokenInfo}
     */
    public static InvocationTokenInfo of(String invocationToken)
    {
        return new InvocationTokenInfo(invocationToken);
    }

    /**
     * @return If there are any more tokens left.
     */
    public boolean hasNext()
    {
        return this.currentIndex < this.original.length() - 1;
    }

    /**
     * @return The next {@link String token} (Split on spaces) from the current index or null if there are no more
     * tokens;
     */
    @Nullable
    public String getNext()
    {
        if (!this.hasNext())
            return null;

        int endIndex = this.original.indexOf(" ", this.currentIndex);

        if (-1 == endIndex) endIndex = this.original.length();

        String match = this.original.substring(this.currentIndex, endIndex);
        this.currentIndex = endIndex + 1;

        return match;
    }

    /**
     * Gets the next string up until (exclusive) the specified string. If the ending string is not contained in the
     * remainder of the invocationToken, an empty optional is returned.
     *
     * @param until
     *      The {@link String} to stop at
     *
     * @return an {@link Optional} containing the next string up until the specified string
     */
    public Optional<String> getNext(String until)
    {
        if (!this.hasNext())
            return Optional.empty();

        int endIndex = this.original.indexOf(until, this.currentIndex);
        if (-1 == endIndex)
            return Optional.empty();

        String match = this.original.substring(this.currentIndex, endIndex);
        this.currentIndex = endIndex;

        if (this.currentlyOnSpace())
            this.currentIndex++;

        return Optional.of(match);
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
     *
     * @return The {@link String} between the start and stop
     */
    public Optional<String> getNextSurrounded(String start, String stop)
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
        this.currentIndex = endIndex + 1;

        if (this.currentlyOnSpace())
            this.currentIndex++;

        return Optional.of(match);
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
    public Optional<String> getNext(@NotNull Pattern pattern)
    {
        if (!this.hasNext())
            return Optional.empty();

        String originalSubstring = this.original.substring(this.currentIndex);
        Matcher matcher = pattern.matcher(originalSubstring);
        if (matcher.lookingAt()) { //Will return true if the start of the string matches the Regex
            String match = originalSubstring.substring(0, matcher.end());
            this.currentIndex += matcher.end() + 1;

            //If there's a space right after the match, skip past it
            if (this.hasNext() && ' ' == this.original.charAt(this.currentIndex))
                this.currentIndex++;

            return Optional.of(match);
        }

        return Optional.empty();
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
     */
    public void saveState()
    {
        this.savedIndex = this.currentIndex;
    }

    /**
     * Restores the current index to the last saved index.
     */
    public void restoreState()
    {
        this.currentIndex = this.savedIndex;
    }

    /**
     * @return If the current character is a space
     */
    private boolean currentlyOnSpace()
    {
        return this.currentIndex < this.original.length() && ' ' == this.original.charAt(this.currentIndex);
    }
}