package nz.pumbas.commands.tokens.context;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Set;

import nz.pumbas.commands.commandadapters.AbstractCommandAdapter;
import nz.pumbas.commands.tokens.TokenCommand;
import nz.pumbas.commands.tokens.tokentypes.ParsingToken;
import nz.pumbas.utilities.Exceptional;

public class MethodContext extends InvocationContext
{
    private ContextState contextState;
    private final AbstractCommandAdapter commandAdapter;
    private final MessageReceivedEvent event;
    private final Set<Class<?>> reflections;

    protected MethodContext(@NotNull String context,
                            @NotNull ContextState contextState,
                            @Nullable AbstractCommandAdapter commandAdapter,
                            @Nullable MessageReceivedEvent event,
                            @NotNull Set<Class<?>> reflections)
    {
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
            context, new ContextState(parsingToken.type(), parsingToken.annotations(), parsingToken.annotationTypes()),
            null, null, Collections.emptySet());
    }

    public static MethodContext of(@NotNull ParsingToken parsingToken) {
        return of("", parsingToken);
    }

    public static MethodContext of(@NotNull String content, @NotNull AbstractCommandAdapter commandAdapter,
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
        this.contextState = new ContextState(parsingToken.type(), parsingToken.annotations(), parsingToken.annotationTypes());
    }

    public AbstractCommandAdapter commandAdapter()
    {
        return this.commandAdapter;
    }

    public MessageReceivedEvent event()
    {
        return this.event;
    }

    public Set<Class<?>> reflections()
    {
        return this.reflections;
    }

    public ContextState contextState() {
        return this.contextState;
    }

    public void contextState(ContextState contextState) {
        this.contextState = contextState;
    }

    @SuppressWarnings("unchecked")
    public <T extends Annotation> Exceptional<T> annotation(Class<T> type) {
        return Exceptional.of(
            Arrays.stream(this.contextState().annotations())
                .filter(a -> a.annotationType().isAssignableFrom(type))
                .findFirst()
                .map(a -> (T)a));
    }
}
