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

package net.pumbas.halpbot.commands.actioninvokable.context;

import net.pumbas.halpbot.converters.tokens.Token;
import net.pumbas.halpbot.events.HalpbotEvent;

import org.dockbox.hartshorn.inject.binding.Bound;
import org.dockbox.hartshorn.inject.binding.ComponentBinding;
import org.dockbox.hartshorn.application.context.ApplicationContext;
import org.dockbox.hartshorn.util.reflect.TypeContext;
import org.jetbrains.annotations.Nullable;

import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import jakarta.inject.Inject;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@ComponentBinding(CommandInvocationContext.class)
@Accessors(chain = false)
public class HalpbotCommandInvocationContext implements CommandInvocationContext
{
    @Inject
    private ApplicationContext applicationContext;
    private final String content;
    private final @Nullable HalpbotEvent halpbotEvent;

    @Setter
    private Set<TypeContext<?>> reflections = Collections.emptySet();
    @Setter
    private int currentIndex;
    @Setter
    private int currentAnnotationIndex;
    @Setter
    private TypeContext<?> currentType = TypeContext.VOID;
    @Setter
    private List<TypeContext<? extends Annotation>> sortedAnnotations = Collections.emptyList();
    @Setter
    private Set<Annotation> annotations = Collections.emptySet();
    @Setter
    private boolean canHaveContextLeft;
    @Setter
    private List<Token> tokens = Collections.emptyList();

    @Bound
    public HalpbotCommandInvocationContext(String content, @Nullable HalpbotEvent halpbotEvent) {
        this.content = content;
        this.halpbotEvent = halpbotEvent;
    }

    @Override
    public void incrementCurrentAnnotationIndex() {
        this.currentAnnotationIndex++;
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
