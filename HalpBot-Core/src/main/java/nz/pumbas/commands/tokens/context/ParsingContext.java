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
import java.util.stream.Collectors;

import nz.pumbas.commands.commandadapters.AbstractCommandAdapter;
import nz.pumbas.commands.tokens.TokenCommand;
import nz.pumbas.commands.tokens.tokentypes.ParsingToken;
import nz.pumbas.utilities.Exceptional;
import nz.pumbas.utilities.Reflect;

public class ParsingContext extends InvocationContext
{
    private final AbstractCommandAdapter commandAdapter;
    private final MessageReceivedEvent event;
    private final Set<Class<?>> reflections;
    private Type type;
    private List<Class<? extends Annotation>> annotationTypes;
    private Annotation[] annotations;

    protected ParsingContext(@NotNull String context, @Nullable Type type,
                             @Nullable AbstractCommandAdapter commandAdapter,
                             @Nullable MessageReceivedEvent event,
                             @NotNull List<Class<? extends Annotation>> annotationTypes,
                             @NotNull Set<Class<?>> reflections,
                             @NotNull Annotation[] annotations)
    {
        super(context);
        this.type = type;
        this.commandAdapter = commandAdapter;
        this.event = event;
        this.annotationTypes = annotationTypes;
        this.reflections = reflections;
        this.annotations = annotations;
    }

    @SafeVarargs
    public static ParsingContext of(@NotNull Type type,
                                    @NotNull Class<? extends Annotation>... annotationTypes) {
        return new ParsingContext(
            "", type, null, null, new ArrayList<>(Arrays.asList(annotationTypes)), Collections.emptySet(), new Annotation[0]);
    }

    @SafeVarargs
    public static ParsingContext of(@NotNull String context, @NotNull Type type,
                                    @NotNull Class<? extends Annotation>... annotationTypes) {
        return new ParsingContext(
            context, type, null, null, new ArrayList<>(Arrays.asList(annotationTypes)), Collections.emptySet(), new Annotation[0]);
    }

    public static ParsingContext of(@NotNull String context, @NotNull ParsingToken parsingToken) {
        return new ParsingContext(
            context, parsingToken.type(), null, null, parsingToken.annotationTypes(), Collections.emptySet(), parsingToken.annotations());
    }

    public static ParsingContext of(@NotNull ParsingToken parsingToken) {
        return of("", parsingToken);
    }

    public static ParsingContext of(@NotNull String content, @NotNull AbstractCommandAdapter commandAdapter,
                                    @NotNull MessageReceivedEvent event, @NotNull Set<Class<?>> reflections) {
        return new ParsingContext(content, null, commandAdapter, event, Collections.emptyList(), reflections, new Annotation[0]);
    }

    public static ParsingContext of(@NotNull String content) {
        return new ParsingContext(content, null, null, null, Collections.emptyList(), Collections.emptySet(), new Annotation[0]);
    }

    public static ParsingContext of(@NotNull String content, TokenCommand tokenCommand) {
        return new ParsingContext(content, null, null, null, Collections.emptyList(), tokenCommand.getReflections(), new Annotation[0]);
    }

    public void update(@NotNull ParsingToken parsingToken) {
        this.type = parsingToken.type();
        this.annotationTypes = parsingToken.annotationTypes();
        this.annotations = parsingToken.annotations();
    }

    public Class<?> clazz() {
        return Reflect.wrapPrimative(Reflect.asClass(this.type));
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

    public void annotationTypes(List<Class<? extends Annotation>> annotationTypes) {
        this.annotationTypes = annotationTypes;
    }

    public Annotation[] annotations() {
        return this.annotations;
    }

    public void annotations(Annotation[] annotations) {
        this.annotations = annotations;
    }

    public Set<Class<?>> reflections()
    {
        return this.reflections;
    }

    public ContextState contextState() {
        return new ContextState(this.currentIndex, this.annotations, this.annotationTypes);
    }

    public void contextState(ContextState contextState) {
        this.currentIndex = contextState.currentIndex();
        this.annotations = contextState.annotations();
        this.annotationTypes = contextState.annotationTypes();

    }

    @SuppressWarnings("unchecked")
    public <T extends Annotation> Exceptional<T> annotation(Class<T> type) {
        return Exceptional.of(
            Arrays.stream(this.annotations)
                .filter(a -> a.annotationType().isAssignableFrom(type))
                .findFirst()
                .map(a -> (T)a));
    }
}
