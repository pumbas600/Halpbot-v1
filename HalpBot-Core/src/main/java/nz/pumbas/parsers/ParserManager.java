package nz.pumbas.parsers;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import nz.pumbas.commands.tokens.context.ParsingContext;
import nz.pumbas.objects.Tuple;
import nz.pumbas.utilities.Reflect;

public final class ParserManager
{
    private ParserManager() {}

    private static final Map<Class<?>, List<ParserContext>> TypeParsers = new HashMap<>();
    private static final List<Tuple<Predicate<Class<?>>, ParserContext>> FallbackTypeParsers = new ArrayList<>();

    @SuppressWarnings("unchecked")
    public static <T> Parser<T> from(@NotNull ParsingContext ctx) {
        return (Parser<T>) from(ctx.clazz(), ctx);
    }

    @SuppressWarnings("unchecked")
    public static <T> Parser<T> from(@NotNull Class<T> type, @NotNull ParsingContext ctx) {
        if (TypeParsers.containsKey(type)) {
            return (Parser<T>) retrieveParserByAnnotation(TypeParsers.get(type), ctx);
        }
        return (Parser<T>) TypeParsers.keySet()
            .stream()
            .filter(c -> c.isAssignableFrom(type))
            .findFirst()
            .map(c -> (Object) retrieveParserByAnnotation(TypeParsers.get(c), ctx))
            .orElseGet(() -> FallbackTypeParsers.stream()
                .filter(t -> filterFallbackParsers(t, type, ctx))
                .findFirst()
                .map(t -> t.getValue().parser())
                .orElse(Reflect.cast(Parsers.OBJECT_PARSER)));
    }

    private static Parser<?> retrieveParserByAnnotation(@NotNull List<ParserContext> parsers,
                                                        @NotNull ParsingContext ctx) {
        if (parsers.isEmpty())
            return Parsers.OBJECT_PARSER;

        for (ParserContext parser : parsers) {
            if (ctx.annotationTypes().contains(parser.annotationType())) {
                ctx.annotationTypes().remove(parser.annotationType());
                return parser.parser();
            }
        }
        // Otherwise return the last parser
        return parsers.get(parsers.size() - 1).parser();
    }

    private static boolean filterFallbackParsers(@NotNull Tuple<Predicate<Class<?>>, ParserContext> tuple,
                                                 @NotNull Class<?> type,
                                                 @NotNull ParsingContext ctx) {
        if (!tuple.getKey().test(type)) return false;

        Class<?> annotationType = tuple.getValue().annotationType();
        if (annotationType.isAssignableFrom(Void.class))
            return true;
        else if (ctx.annotationTypes().contains(annotationType)) {
            ctx.annotationTypes().remove(annotationType);
            return true;
        }
        else if (tuple.getValue().includeClassAnnotations()) {
            return false; // TODO: Class annotations
        }
        return false;
    }

    public static void registerParser(@NotNull Class<?> type,
                                      @NotNull Class<?> annotationType,
                                      @NotNull Parser<?> typeParser) {
        if (!TypeParsers.containsKey(type))
            TypeParsers.put(type, new ArrayList<>());

        List<ParserContext> parsers = TypeParsers.get(type);
        for (int i = 0; i < parsers.size(); i++) {
            if (0 < parsers.get(i).parser().priority().compareTo(typeParser.priority())) {
                parsers.add(i, new ParserContext(annotationType, typeParser, false));
                return;
            }
        }
        parsers.add(new ParserContext(annotationType, typeParser, false));
    }

    public static void registerParser(@NotNull Predicate<Class<?>> filter,
                                      @NotNull Class<?> annotationType,
                                      @NotNull Parser<?> typeParser) {

        Tuple<Predicate<Class<?>>, ParserContext> parser =
            Tuple.of(filter, new ParserContext(annotationType, typeParser, false));

        for (int i = 0; i < FallbackTypeParsers.size(); i++) {
            if (0 < FallbackTypeParsers.get(i).getValue().parser().priority().compareTo(typeParser.priority())) {
                FallbackTypeParsers.add(i, parser);
                return;
            }
        }
        FallbackTypeParsers.add(parser);
    }
}
