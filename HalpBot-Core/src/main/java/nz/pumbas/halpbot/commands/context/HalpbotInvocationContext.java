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

import org.dockbox.hartshorn.core.annotations.inject.Binds;
import org.dockbox.hartshorn.core.annotations.inject.Bound;
import org.dockbox.hartshorn.core.context.ApplicationContext;
import org.dockbox.hartshorn.core.context.element.TypeContext;
import org.dockbox.hartshorn.core.domain.Exceptional;
import org.jetbrains.annotations.Nullable;

import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import nz.pumbas.halpbot.commands.exceptions.IllegalFormatException;
import nz.pumbas.halpbot.events.HalpbotEvent;

@Getter
@Binds(InvocationContext.class)
@RequiredArgsConstructor(onConstructor_ = @Bound)
public class HalpbotInvocationContext implements InvocationContext
{
    @Inject private ApplicationContext applicationContext;
    private final String content;
    private final @Nullable HalpbotEvent halpbotEvent;
    private final Set<TypeContext<?>> reflections;

    @Setter private int currentIndex;
    @Setter private int currentAnnotationIndex;
    @Setter private TypeContext<?> currentType = TypeContext.VOID;
    @Setter private List<TypeContext<? extends Annotation>> sortedAnnotations = Collections.emptyList();
    @Setter private Set<Annotation> annotations = Collections.emptySet();

    @Override
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

    @Override
    public Exceptional<String> next(String until, boolean stepPast) {
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

    @Override
    public Exceptional<String> nextSurrounded(String start, String stop, boolean stepPast) {
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

    @Override
    public Exceptional<String> next(Pattern pattern) {
        if (this.hasNext()) {
            String originalSubstring = this.content.substring(this.currentIndex);
            Matcher matcher = pattern.matcher(originalSubstring);
            if (matcher.lookingAt()) { // Will return true if the start of the string matches the Regex
                String match = originalSubstring.substring(0, matcher.end());
                this.currentIndex += matcher.end();

                // If there's a space right after the match, skip past it
                if (this.hasNext() && ' ' == this.content.charAt(this.currentIndex))
                    this.currentIndex++;

                this.skipPastSpaces();
                return Exceptional.of(match);
            }
        }
        return Exceptional.of(
            new IllegalFormatException("The start of " + this.next() + " doesn't match the expected format"));
    }

    @Override
    public void incrementIndex() {
        this.currentIndex++;
    }

    @Override
    public void incrementAnnotationIndex() {
        this.currentAnnotationIndex++;
    }
}
