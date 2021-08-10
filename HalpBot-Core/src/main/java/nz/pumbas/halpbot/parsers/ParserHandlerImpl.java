package nz.pumbas.halpbot.parsers;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import nz.pumbas.halpbot.objects.Tuple;
import nz.pumbas.halpbot.commands.tokens.context.MethodContext;
import nz.pumbas.halpbot.utilities.Reflect;
import nz.pumbas.halpbot.utilities.context.LateInit;

public class ParserHandlerImpl implements ParserHandler, LateInit
{

    private final Map<Class<?>, List<ParserContext>> typeParsers = new HashMap<>();
    private final List<Tuple<Predicate<Class<?>>, ParserContext>> fallbackTypeParsers = new ArrayList<>();

    /**
     * A late initialisation function that is called after the object has been first constructed.
     */
    @Override
    public void lateInitialisation() {
        // Load the parsers
        Parsers.load();
    }

    /**
     * Retrieves the {@link Parser} for the specified {@link Class type} and {@link MethodContext}.
     *
     * @param type
     *      The {@link Class type} of the {@link TypeParser}
     * @param ctx
     *      The {@link MethodContext}
     * @param <T>
     *      The type of the {@link TypeParser}
     *
     * @return The retrieved {@link Parser}
     */
    @Override
    @SuppressWarnings("unchecked")
    public <T> Parser<T> from(@NotNull Class<T> type, @NotNull MethodContext ctx) {
        if (this.typeParsers.containsKey(type)) {
            return (Parser<T>) this.retrieveParserByAnnotation(this.typeParsers.get(type), ctx);
        }
        return (Parser<T>) this.typeParsers.keySet()
            .stream()
            .filter(c -> c.isAssignableFrom(type))
            .findFirst()
            .map(c -> (Object) this.retrieveParserByAnnotation(this.typeParsers.get(c), ctx))
            .orElseGet(() -> this.fallbackTypeParsers.stream()
                .filter(t -> this.filterFallbackParsers(t, type, ctx))
                .findFirst()
                .map(t -> t.getValue().getParser())
                .orElse(Reflect.cast(Parsers.OBJECT_PARSER)));
    }

    /**
     * Retrieves the {@link Parser} with the corresponding annotation from the list of {@link ParserContext}.
     *
     * @param parsers
     *      The list of {@link ParserContext}
     * @param ctx
     *      The {@link MethodContext}
     *
     * @return The {@link Parser} with the corresponding annotation
     */
    private Parser<?> retrieveParserByAnnotation(@NotNull List<ParserContext> parsers, @NotNull MethodContext ctx) {
        if (parsers.isEmpty())
            return Parsers.OBJECT_PARSER;

        for (ParserContext parser : parsers) {
            if (ctx.getContextState().getAnnotationTypes().contains(parser.getAnnotationType())) {
                ctx.getContextState().getAnnotationTypes().remove(parser.getAnnotationType());
                return parser.getParser();
            }
        }
        // Otherwise return the last parser
        return parsers.get(parsers.size() - 1).getParser();
    }

    private boolean filterFallbackParsers(@NotNull Tuple<Predicate<Class<?>>, ParserContext> tuple,
                                          @NotNull Class<?> type, @NotNull MethodContext ctx) {
        if (!tuple.getKey().test(type)) return false;

        Class<?> annotationType = tuple.getValue().getAnnotationType();
        if (annotationType.isAssignableFrom(Void.class))
            return true;
        else if (ctx.getContextState().getAnnotationTypes().contains(annotationType)) {
            ctx.getContextState().getAnnotationTypes().remove(annotationType);
            return true;
        }
        return false;
    }

    /**
     * Registers a {@link Parser} against the {@link Class type} with the specified {@link Class annotation type}.
     *
     * @param type
     *      The type of the {@link TypeParser}
     * @param annotationType
     *      The {@link Class type} of the annotation
     * @param typeParser
     *      The {@link Parser} to register
     */
    @Override
    public void registerParser(@NotNull Class<?> type, @NotNull Class<?> annotationType, @NotNull Parser<?> typeParser) {
        if (!this.typeParsers.containsKey(type))
            this.typeParsers.put(type, new ArrayList<>());

        List<ParserContext> parsers = this.typeParsers.get(type);
        for (int i = 0; i < parsers.size(); i++) {
            if (0 < parsers.get(i).getParser().getPriority().compareTo(typeParser.getPriority())) {
                parsers.add(i, new ParserContext(annotationType, typeParser));
                return;
            }
        }
        parsers.add(new ParserContext(annotationType, typeParser));
    }

    /**
     * Registers a {@link Parser} against the {@link Predicate filter} with the specified {@link Class annotation type}.
     *
     * @param filter
     *      The {@link Predicate filter} for this {@link Parser}
     * @param annotationType
     *      The {@link Class type} of the annotation
     * @param typeParser
     *      The {@link Parser} to register
     */
    @Override
    public void registerParser(@NotNull Predicate<Class<?>> filter, @NotNull Class<?> annotationType,
                               @NotNull Parser<?> typeParser) {

        Tuple<Predicate<Class<?>>, ParserContext> parser =
            Tuple.of(filter, new ParserContext(annotationType, typeParser));

        for (int i = 0; i < this.fallbackTypeParsers.size(); i++) {
            if (0 < this.fallbackTypeParsers.get(i).getValue().getParser().getPriority().compareTo(typeParser.getPriority())) {
                this.fallbackTypeParsers.add(i, parser);
                return;
            }
        }
        this.fallbackTypeParsers.add(parser);
    }
}
