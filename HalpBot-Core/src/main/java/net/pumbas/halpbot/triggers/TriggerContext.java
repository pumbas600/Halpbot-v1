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

package net.pumbas.halpbot.triggers;

import net.pumbas.halpbot.actions.DisplayableResult;
import net.pumbas.halpbot.actions.invokable.SourceContext;
import net.pumbas.halpbot.actions.invokable.SourceInvocationContext;
import net.pumbas.halpbot.utilities.Require;

import java.util.List;
import java.util.stream.Stream;

public interface TriggerContext extends SourceContext<SourceInvocationContext>, DisplayableResult
{
    List<String> triggers();

    String description();

    TriggerStrategy strategy();

    Require require();

    default boolean matches(String message) {
        Stream<String> stream = this.triggers().stream();

        return switch (this.require()) {
            case ALL -> stream.allMatch(trigger -> this.strategy().contains(message, trigger));
            case ANY -> stream.anyMatch(trigger -> this.strategy().contains(message, trigger));
        };
    }
}
