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

package net.pumbas.halpbot.utilities;

import net.pumbas.halpbot.commands.actioninvokable.context.CommandInvocationContext;
import net.pumbas.halpbot.commands.exceptions.IllegalFormatException;

import org.dockbox.hartshorn.core.domain.Exceptional;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public interface StringTraverser
{
    String content();

    int currentIndex();

    void currentIndex(int index);

    /**
     * Increments the current index by one.
     */
    void incrementIndex();

    /**
     * @return If there is any more content left
     */
    default boolean hasNext() {
        return this.currentIndex() < this.content().length();
    }

    /**
     * @return An {@link Exceptional} containing the next {@link String word} from the current index or {@link
     *     Exceptional#empty()} if there are no more words
     * @see CommandInvocationContext#next()
     */
    default Exceptional<String> nextSafe() {
        if (!this.hasNext())
            return Exceptional.of(new IllegalFormatException("No string next in " + this.content()));

        int endIndex = this.content().indexOf(" ", this.currentIndex());

        if (endIndex == -1) endIndex = this.content().length();

        String match = this.content().substring(this.currentIndex(), endIndex);
        this.currentIndex(endIndex + 1);
        this.skipPastWhitespaces();

        return Exceptional.of(match);
    }

    /**
     * @return The next {@link String word} from the current index or an empty {@link String} if there are no more words
     * @see CommandInvocationContext#nextSafe()
     */
    default String next() {
        return this.nextSafe().or("");
    }

    /**
     * Gets the next string up until (exclusive) the specified string. If the ending string is not contained in the
     * remainder of the content, an {@link Exceptional#empty()} is returned. This will automatically move the current
     * index past the until string and any whitespace.
     *
     * @param until
     *     The {@link String} to stop at
     *
     * @return an {@link Exceptional} containing the next string up until the specified string
     * @see CommandInvocationContext#next(String, boolean)
     * @see CommandInvocationContext#next(Pattern)
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
     * @see CommandInvocationContext#next(String)
     */
    default Exceptional<String> next(String until, boolean stepPast) {
        if (!this.hasNext())
            return Exceptional.of(new IllegalFormatException("There is nothing left to retrieve"));

        int endIndex = this.content().indexOf(until, this.currentIndex());
        if (endIndex == -1)
            return Exceptional.of(new IllegalFormatException("Missing the ending " + until));

        String match = this.content().substring(this.currentIndex(), endIndex);
        this.currentIndex(endIndex);

        if (stepPast) {
            this.currentIndex(this.currentIndex() + until.length());
            this.skipPastWhitespaces();
        }
        return Exceptional.of(match);
    }

    /**
     * Gets the next string that matches the specified {@link Pattern}. If there is no matching string or the matching
     * string is not next then an {@link Exceptional} containing an {@link IllegalFormatException} is returned.
     *
     * @param pattern
     *     The {@link Pattern} for the next string
     *
     * @return An {@link Exceptional} containing the matched {@link String pattern}
     * @see CommandInvocationContext#next(String)
     */
    default Exceptional<String> next(Pattern pattern) {
        if (this.hasNext()) {
            String originalSubstring = this.content().substring(this.currentIndex());
            Matcher matcher = pattern.matcher(originalSubstring);
            if (matcher.lookingAt()) { // Will return true if the start of the string matches the Regex
                String match = originalSubstring.substring(0, matcher.end());
                this.currentIndex(this.currentIndex() + matcher.end());

                // If there's a space right after the match, skip past it
                if (this.hasNext() && ' ' == this.content().charAt(this.currentIndex()))
                    this.incrementIndex();

                this.skipPastWhitespaces();
                return Exceptional.of(match);
            }
        }
        return Exceptional.of(
            new IllegalFormatException("The start of " + this.next() + " doesn't match the expected format"));
    }

    static Exceptional<String> next(String content, Pattern pattern) {
        Matcher matcher = pattern.matcher(content);
        if (matcher.lookingAt()) { // Will return true if the start of the string matches the Regex
            String match = content.substring(0, matcher.end());
            return Exceptional.of(match);
        }
        return Exceptional.empty();
    }

    /**
     * Returns the next string which is contained between the start and stop specified while respecting any nested start
     * and stops too. This will automatically step past the stop characters and any whitespace.
     * <p>
     * E.g: for {@code [#Block[1 2 3] #Block[2 3 4]]}, {@code nextSurrounded("[", "]")} will return {@code #Block[1 2 3]
     * #Block[2 3 4]}.
     *
     * @param start
     *     The {@link String} defining the starting characters
     * @param stop
     *     The {@link String} defining the stopping characters
     *
     * @return An {@link Exceptional} containing the string between the start and stop
     * @see CommandInvocationContext#nextSurrounded(String, String, boolean)
     */
    default Exceptional<String> nextSurrounded(String start, String stop) {
        return this.nextSurrounded(start, stop, true);
    }

    /**
     * Returns the next string which is contained between the start and stop specified while respecting any nested start
     * and stops too.
     * <p>
     * E.g: for {@code [#Block[1 2 3] #Block[2 3 4]]}, {@code #nextSurrounded("[", "]", false)} will return {@code
     * #Block[1 2 3] #Block[2 3 4]}.
     *
     * @param start
     *     The {@link String} defining the starting characters
     * @param stop
     *     The {@link String} defining the stopping characters
     * @param stepPast
     *     Should it move the current index past the stop characters and any whitespace, or remain on it
     *
     * @return An {@link Exceptional} containing the string between the start and stop
     * @see CommandInvocationContext#nextSurrounded(String, String)
     */
    default Exceptional<String> nextSurrounded(String start, String stop, boolean stepPast) {
        if (!this.hasNext() || this.content().indexOf(start, this.currentIndex()) != this.currentIndex())
            return Exceptional.of(new IllegalFormatException("Missing the starting " + start));

        int startCount = 1;
        int startIndex = this.currentIndex() + 1;
        int endIndex = this.currentIndex();
        do {
            endIndex = this.content().indexOf(stop, endIndex + 1);
            if (endIndex == -1)
                return Exceptional.of(new IllegalFormatException("Missing the ending " + stop));

            startCount--;

            int tempStartIndex;
            while ((tempStartIndex = this.content().indexOf(start, startIndex)) < endIndex && -1 != tempStartIndex) {
                startCount++;
                startIndex = tempStartIndex + 1;
            }
        } while (0 != startCount);

        String match = this.content().substring(this.currentIndex() + start.length(), endIndex);
        this.currentIndex(endIndex);

        if (stepPast) {
            this.currentIndex(this.currentIndex() + stop.length());
            this.skipPastWhitespaces();
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
    default boolean nextMatches(String next) {
        int endIndex = this.currentIndex() + next.length();
        if (endIndex < this.content().length() && this.content().substring(this.currentIndex(), endIndex).equalsIgnoreCase(next)) {
            this.currentIndex(endIndex);
            if (this.currentlyOnWhitespace())
                this.incrementIndex();
            return true;
        }
        return false;
    }

    /**
     * @return The remaining strings
     */
    default String remaining() {
        if (!this.hasNext())
            return "";

        String remaining = this.content().substring(this.currentIndex());
        this.currentIndex(this.content().length());
        return remaining;
    }

    /**
     * @return If the current character is whitespace
     */
    default boolean currentlyOnWhitespace() {
        return this.hasNext() && Character.isWhitespace(this.content().charAt(this.currentIndex()));
    }

    /**
     * While the current index is on a whitespace, it will continue incrementing the current index.
     */
    default void skipPastWhitespaces() {
        while (this.currentlyOnWhitespace())
            this.incrementIndex();
    }

    /**
     * Returns if the character at the current index is the one specified. If it is, then it steps past this character
     * automatically, along with any whitespace.
     *
     * @param character
     *     The character to check
     *
     * @return If the character is at the current index
     * @see CommandInvocationContext#isNext(char, boolean)
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
     * @see CommandInvocationContext#isNext(char)
     */
    default boolean isNext(char character, boolean stepPast) {
        if (!this.hasNext())
            return false;

        boolean isNext = this.content().charAt(this.currentIndex()) == character;

        if (isNext && stepPast) {
            this.incrementIndex();
            this.skipPastWhitespaces();
        }
        return isNext;
    }

    /**
     * Assets that the next character is the one specified. If it's not, then it throws an {@link
     * IllegalFormatException}.
     *
     * @param character
     *     The character to assert is next
     *
     * @see CommandInvocationContext#isNext(char)
     */
    default void assertNext(char character) {
        if (!this.isNext(character)) {
            String found = this.hasNext() ? "" + this.content().charAt(this.currentIndex()) : "End of content";
            throw new IllegalFormatException("Expected the character '%s' but found '%s'".formatted(character, found));
        }
    }
}
