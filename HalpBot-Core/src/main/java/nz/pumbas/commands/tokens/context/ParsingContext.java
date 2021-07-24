package nz.pumbas.commands.tokens.context;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import nz.pumbas.commands.commandadapters.AbstractCommandAdapter;
import nz.pumbas.commands.exceptions.IllegalFormatException;
import nz.pumbas.commands.tokens.TokenCommand;
import nz.pumbas.commands.tokens.tokentypes.ParsingToken;
import nz.pumbas.utilities.Reflect;

public class ParsingContext extends InvocationContext
{
    private final AbstractCommandAdapter commandAdapter;
    private final MessageReceivedEvent event;
    private final Set<Class<?>> reflections;
    private Class<?> clazz;
    private Type type;
    private List<Class<? extends Annotation>> annotationTypes;

    protected ParsingContext(@NotNull String context, @Nullable Type type,
                             @Nullable AbstractCommandAdapter commandAdapter,
                             @Nullable MessageReceivedEvent event,
                             @NotNull List<Class<? extends Annotation>> annotationTypes,
                             @NotNull Set<Class<?>> reflections)
    {
        super(context);
        this.clazz = Reflect.wrapPrimative(Reflect.asClass(type));
        this.type = type;
        this.commandAdapter = commandAdapter;
        this.event = event;
        this.annotationTypes = annotationTypes;
        this.reflections = reflections;
    }

    @SafeVarargs
    public static ParsingContext of(@NotNull Type type,
                                    @NotNull Class<? extends Annotation>... annotationTypes) {
        return new ParsingContext(
            "", type, null, null, new ArrayList<>(Arrays.asList(annotationTypes)), Collections.emptySet());
    }

    @SafeVarargs
    public static ParsingContext of(@NotNull String context, @NotNull Type type,
                                    @NotNull Class<? extends Annotation>... annotationTypes) {
        return new ParsingContext(
            context, type, null, null, new ArrayList<>(Arrays.asList(annotationTypes)), Collections.emptySet());
    }

    public static ParsingContext of(@NotNull String context, @NotNull ParsingToken parsingToken) {
        return new ParsingContext(
            context, parsingToken.type(), null, null, parsingToken.annotationTypes(), Collections.emptySet());
    }

    public static ParsingContext of(@NotNull ParsingToken parsingToken) {
        return of("", parsingToken);
    }

    public static ParsingContext of(@NotNull String content, @NotNull AbstractCommandAdapter commandAdapter,
                                    @NotNull MessageReceivedEvent event, @NotNull Set<Class<?>> reflections) {
        return new ParsingContext(content, null, commandAdapter, event, Collections.emptyList(), reflections);
    }

    public static ParsingContext of(@NotNull String content) {
        return new ParsingContext(content, null, null, null, Collections.emptyList(), Collections.emptySet());
    }

    public static ParsingContext of(@NotNull String content, TokenCommand tokenCommand) {
        return new ParsingContext(content, null, null, null, Collections.emptyList(), tokenCommand.getReflections());
    }

    public void update(@NotNull ParsingToken parsingToken) {
        this.type = parsingToken.type();
        this.clazz = Reflect.wrapPrimative(Reflect.asClass(this.type));
        this.annotationTypes = parsingToken.annotationTypes();
    }

    public Class<?> clazz() {
        return this.clazz;
    }

    public Type type() {
        return this.type;
    }

    public AbstractCommandAdapter commandAdapter()
    {
        return this.commandAdapter;
    }

    public MessageReceivedEvent event()
    {
        return this.event;
    }

    public List<Class<? extends Annotation>> annotationTypes()
    {
        return this.annotationTypes;
    }

    public Set<Class<?>> reflections()
    {
        return this.reflections;
    }
}
