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

import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Set;

import nz.pumbas.halpbot.adapters.HalpbotCore;
import nz.pumbas.halpbot.commands.commandadapters.AbstractCommandAdapter;
import nz.pumbas.halpbot.commands.commandmethods.SimpleCommand;
import nz.pumbas.halpbot.commands.events.HalpbotEvent;
import nz.pumbas.halpbot.commands.tokens.ParsingToken;
import nz.pumbas.halpbot.objects.Exceptional;

public class MethodContext extends InvocationContext
{
    private @NotNull ContextState contextState;
    private @Nullable final HalpbotEvent event;
    private @NotNull final HalpbotCore halpbotCore;
    private @NotNull final Set<Class<?>> reflections;

    protected MethodContext(@NotNull String context,
                            @NotNull ContextState contextState,
                            @Nullable HalpbotCore halpbotCore,
                            @Nullable HalpbotEvent event,
                            @NotNull Set<Class<?>> reflections) {
        super(context);
        this.contextState = contextState;
        this.event = event;
        this.halpbotCore = halpbotCore;
        this.reflections = reflections;
    }

    @SafeVarargs
    public static MethodContext of(@NotNull Type type,
                                   @NotNull Class<? extends Annotation>... annotationTypes) {
        return new MethodContext(
            "", new ContextState(type, new Annotation[0], new ArrayList<>(Arrays.asList(annotationTypes))),
            null, null, Collections.emptySet());
    }

    @SafeVarargs
    public static MethodContext of(@NotNull String context, @NotNull Type type,
                                   @NotNull Class<? extends Annotation>... annotationTypes) {
        return new MethodContext(
            context, new ContextState(type, new Annotation[0], new ArrayList<>(Arrays.asList(annotationTypes))),
            null, null, Collections.emptySet());
    }

    public static MethodContext of(@NotNull String context, @NotNull ParsingToken parsingToken) {
        return new MethodContext(
            context, new ContextState(parsingToken.getType(), parsingToken.getAnnotations(), parsingToken.getAnnotationTypes()),
            null, null, Collections.emptySet());
    }

    public static MethodContext of(@NotNull ParsingToken parsingToken) {
        return of("", parsingToken);
    }

    public static MethodContext of(@NotNull String content, @NotNull HalpbotCore halpbotCore,
                                   @NotNull HalpbotEvent event, @NotNull Set<Class<?>> reflections) {
        return new MethodContext(content, ContextState.EMPTY, halpbotCore, event, reflections);
    }

    public static MethodContext of(@NotNull String content) {
        return new MethodContext(content, ContextState.EMPTY, null, null, Collections.emptySet());
    }

    public static MethodContext of(@NotNull String content, SimpleCommand simpleCommand) {
        return new MethodContext(content, ContextState.EMPTY, null, null, simpleCommand.getReflections());
    }

    public void update(@NotNull ParsingToken parsingToken) {
        this.contextState = new ContextState(parsingToken.getType(), parsingToken.getAnnotations(), parsingToken.getAnnotationTypes());
    }

    public @Nullable HalpbotEvent getEvent() {
        return this.event;
    }

    public @NotNull Set<Class<?>> getReflections() {
        return this.reflections;
    }

    public @NotNull ContextState getContextState() {
        return this.contextState;
    }

    public @NotNull HalpbotCore getHalpbotCore() {
        return this.halpbotCore;
    }

    public void setContextState(@NotNull ContextState contextState) {
        this.contextState = contextState;
    }

    /**
     * Retrieves the first {@link Annotation} of the specified {@link Class type}. If the annotation isn't present an
     * empty {@link Exceptional} is returned.
     *
     * @param type
     *      The {@link Class type} of the annotation
     * @param <T>
     *      The type of the annoation
     *
     * @return An {@link Exceptional} containing the annotation, or {@link Exceptional#empty()} if not present
     */
    @SuppressWarnings("unchecked")
    public <T extends Annotation> Exceptional<T> getAnnotation(Class<T> type) {
        return Exceptional.of(
            Arrays.stream(this.getContextState().getAnnotations())
                .filter(a -> type.isAssignableFrom(a.annotationType()))
                .findFirst()
                .map(a -> (T) a));
    }
}
