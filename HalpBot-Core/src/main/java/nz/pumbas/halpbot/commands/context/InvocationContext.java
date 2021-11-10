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

package nz.pumbas.halpbot.commands.context;

import org.dockbox.hartshorn.core.HartshornUtils;
import org.dockbox.hartshorn.core.context.ApplicationContext;
import org.dockbox.hartshorn.core.context.ContextCarrier;
import org.dockbox.hartshorn.core.context.element.ParameterContext;
import org.dockbox.hartshorn.core.context.element.TypeContext;
import org.dockbox.hartshorn.core.domain.Exceptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import nz.pumbas.halpbot.commands.exceptions.IllegalFormatException;
import nz.pumbas.halpbot.events.HalpbotEvent;

@Getter
@RequiredArgsConstructor
public class InvocationContext implements ContextCarrier
{
    @NotNull private final ApplicationContext applicationContext;

    @Nullable private final HalpbotEvent halpbotEvent;

    @NotNull private final Set<TypeContext<?>> reflections;

    @NotNull private final String content;

    @Setter private int currentIndex;

    @Setter
    @Nullable private ParameterContext<?> currentParameter;

    public InvocationContext(@NotNull ApplicationContext applicationContext, @NotNull String content) {
        this(applicationContext, null, HartshornUtils.emptySet(), content);
    }

    /**
     * @return If there are any more tokens left.
     */
    public boolean hasNext() {
        return this.currentIndex < this.content.length();
    }

    /**
     * @return An {@link Exceptional} containing the next {@link String token} (Split on spaces) from the current
     *     index or {@link Exceptional#empty()} if there are no more tokens;
     */
    public Exceptional<String> nextSafe() {
        if (!this.hasNext())
            return Exceptional.of(new IllegalFormatException("No string next in " + this.content));

        int endIndex = this.content.indexOf(" ", this.currentIndex);

        if (-1 == endIndex) endIndex = this.content.length();

        String match = this.content.substring(this.currentIndex, endIndex);
        this.currentIndex = endIndex + 1;
        this.skipPastSpaces();

        return Exceptional.of(match);
    }

    /**
     * @return The next {@link String token} (Split on spaces) from the current index or an empty
     *     {@link String} if there are no more tokens;
     */
    @NotNull
    public String next() {
        return this.nextSafe().or("");
    }

    /**
     * Gets the next string up until (exclusive) the specified string. If the ending string is not contained in the
     * remainder of the invocationToken, an empty optional is returned. This will automatically step past the until
     * characters.
     *
     * @param until
     *     The {@link String} to stop at
     *
     * @return an {@link Optional} containing the next string up until the specified string
     */
    @NotNull
    public Exceptional<String> next(@NotNull String until) {
        return this.next(until, true);
    }

    /**
     * Gets the next string up until (exclusive) the specified string. If the ending string is not contained in the
     * remainder of the invocationToken, an empty optional is returned.
     *
     * @param until
     *     The {@link String} to stop at
     * @param stepPast
     *     Should it move the current index past the until characters, or remain on it
     *
     * @return an {@link Optional} containing the next string up until the specified string
     */
    @NotNull
    public Exceptional<String> next(@NotNull String until, boolean stepPast) {
        if (!this.hasNext())
            return Exceptional.of(new IllegalFormatException("There is nothing left to retrieve"));

        int endIndex = this.content.indexOf(until, this.currentIndex);
        if (-1 == endIndex)
            return Exceptional.of(new IllegalFormatException("Missing the ending " + until));

        String match = this.content.substring(this.currentIndex, endIndex);
        this.currentIndex = endIndex;

        if (stepPast) {
            this.currentIndex += until.length();
            this.skipPastSpaces();
        }
        return Exceptional.of(match);
    }

    /**
     * Returns the next string which is contained between the start and stop specified. This will automatically step
     * past the stop characters. It will respect nested start and stops too.
     * <p>
     * E.g: for {@code [#Block[1 2 3] #Block[2 3 4]]}, {@code #getNextSurrounded("[", "]")} will return
     * {@code #Block[1 2 3] #Block[2 3 4]}.
     *
     * @param start
     *     The {@link String} defining the starting characters
     * @param stop
     *     The {@link String} defining the stopping characters
     *
     * @return The {@link String} between the start and stop
     */
    @NotNull
    public Exceptional<String> nextSurrounded(@NotNull String start, @NotNull String stop) {
        return this.nextSurrounded(start, stop, true);
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
     *     The {@link String} defining the starting characters
     * @param stop
     *     The {@link String} defining the stopping characters
     * @param stepPast
     *     Should it move the current index past the stop characters, or remain on it
     *
     * @return The {@link String} between the start and stop
     */
    @NotNull
    public Exceptional<String> nextSurrounded(@NotNull String start, @NotNull String stop, boolean stepPast) {
        if (!this.hasNext() || this.content.indexOf(start, this.currentIndex) != this.currentIndex)
            return Exceptional.of(new IllegalFormatException("Missing the starting " + start));

        int startCount = 1;
        int startIndex = this.currentIndex + 1;
        int endIndex = this.currentIndex;
        do {
            endIndex = this.content.indexOf(stop, endIndex + 1);
            if (-1 == endIndex)
                return Exceptional.of(new IllegalFormatException("Missing the ending " + stop));

            startCount--;

            int tempStartIndex;
            while ((tempStartIndex = this.content.indexOf(start, startIndex)) < endIndex && -1 != tempStartIndex) {
                startCount++;
                startIndex = tempStartIndex + 1;
            }
        } while (0 != startCount);

        String match = this.content.substring(this.currentIndex + start.length(), endIndex);
        this.currentIndex = endIndex;

        if (stepPast) {
            this.currentIndex += stop.length();
            this.skipPastSpaces();
        }

        return Exceptional.of(match);
    }

    /**
     * Determine if the specified string is next. If it is, then it steps past.
     *
     * @param next
     *     The string to check if is next
     *
     * @return If the specified string is next
     */
    public boolean nextMatches(@NotNull String next) {
        int endIndex = this.currentIndex + next.length();
        if (this.content.substring(this.currentIndex, endIndex).equalsIgnoreCase(next)) {
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
     *     The {@link Pattern} for the next token
     *
     * @return An {@link Optional} containing the matched {@link String pattern} if present
     */
    @NotNull
    public Exceptional<String> next(@NotNull Pattern pattern) {
        if (this.hasNext()) {
            String originalSubstring = this.content.substring(this.currentIndex);
            Matcher matcher = pattern.matcher(originalSubstring);
            if (matcher.lookingAt()) { //Will return true if the start of the string matches the Regex
                String match = originalSubstring.substring(0, matcher.end());
                this.currentIndex += matcher.end();

                //If there's a space right after the match, skip past it
                if (this.hasNext() && ' ' == this.content.charAt(this.currentIndex))
                    this.currentIndex++;

                this.skipPastSpaces();

                return Exceptional.of(match);
            }
        }
        return Exceptional.of(
            new IllegalFormatException("The start of " + this.next() + " doesn't match the expected format"));
    }

    /**
     * @return The remaining strings
     */
    @NotNull
    public String remaining() {
        if (!this.hasNext())
            throw new IllegalFormatException("There are no remaining strings");

        String remaining = this.content.substring(this.currentIndex);
        this.currentIndex = this.content.length();
        return remaining;
    }

    /**
     * @return If the current character is a space
     */
    private boolean currentlyOnSpace() {
        return this.hasNext() && ' ' == this.content.charAt(this.currentIndex);
    }

    /**
     * Increments the current index by one.
     */
    public void incrementIndex() {
        this.currentIndex++;
    }

    /**
     * While the current index is on a space, it will continue incrementing the current index.
     */
    public void skipPastSpaces() {
        while (this.currentlyOnSpace())
            this.currentIndex++;
    }

    /**
     * Returns if the character at the current index is the one specified. If it is, then it steps past this
     * character automatically.
     *
     * @param character
     *     The character to check
     *
     * @return If the character is at the current index
     */
    public boolean isNext(char character) {
        return this.isNext(character, true);
    }

    /**
     * Returns if the character at the current index is the one specified. If you want to step past the character, it
     * will also step past any spaces.
     *
     * @param character
     *     The character to check
     * @param stepPast
     *     If the character should be stepped past
     *
     * @return If the character is at the current index
     */
    public boolean isNext(char character, boolean stepPast) {
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
     * If {@link InvocationContext#isNext(char)} returns false, then it throws an {@link IllegalFormatException}.
     *
     * @param character
     *     The character to assert is next.
     */
    public void assertNext(char character) {
        if (!this.isNext(character, true)) {
            String found = this.hasNext() ? "" + this.content.charAt(this.currentIndex) : "End of content";
            throw new IllegalFormatException("Expected the character '%s' but found '%s'".formatted(character, found));
        }
    }
}
