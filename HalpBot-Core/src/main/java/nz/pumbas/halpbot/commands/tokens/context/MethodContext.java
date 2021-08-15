package nz.pumbas.halpbot.commands.tokens.context;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Set;

import nz.pumbas.halpbot.commands.commandadapters.CommandAdapter;
import nz.pumbas.halpbot.commands.tokens.TokenCommand;
import nz.pumbas.halpbot.commands.tokens.tokentypes.ParsingToken;
import nz.pumbas.halpbot.objects.Exceptional;

public class MethodContext extends InvocationContext
{
    private @NotNull ContextState contextState;
    private @Nullable final CommandAdapter commandAdapter;
    private @Nullable final MessageReceivedEvent event;
    private @NotNull final Set<Class<?>> reflections;

    protected MethodContext(@NotNull String context,
                            @NotNull ContextState contextState,
                            @Nullable CommandAdapter commandAdapter,
                            @Nullable MessageReceivedEvent event,
                            @NotNull Set<Class<?>> reflections) {
        super(context);
        this.contextState = contextState;
        this.commandAdapter = commandAdapter;
        this.event = event;
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

    public static MethodContext of(@NotNull String content, @NotNull CommandAdapter commandAdapter,
                                   @NotNull MessageReceivedEvent event, @NotNull Set<Class<?>> reflections) {
        return new MethodContext(content, ContextState.EMPTY, commandAdapter, event, reflections);
    }

    public static MethodContext of(@NotNull String content) {
        return new MethodContext(content, ContextState.EMPTY, null, null, Collections.emptySet());
    }

    public static MethodContext of(@NotNull String content, TokenCommand tokenCommand) {
        return new MethodContext(content, ContextState.EMPTY, null, null, tokenCommand.getReflections());
    }

    public void update(@NotNull ParsingToken parsingToken) {
        this.contextState = new ContextState(parsingToken.getType(), parsingToken.getAnnotations(), parsingToken.getAnnotationTypes());
    }

    public @Nullable CommandAdapter getCommandAdapter() {
        return this.commandAdapter;
    }

    public @Nullable MessageReceivedEvent getEvent() {
        return this.event;
    }

    public @NotNull Set<Class<?>> getReflections() {
        return this.reflections;
    }

    public @NotNull ContextState getContextState() {
        return this.contextState;
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
