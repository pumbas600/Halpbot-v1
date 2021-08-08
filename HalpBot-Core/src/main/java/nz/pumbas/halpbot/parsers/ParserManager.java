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

public final class ParserManager
{
    static {
        TypeParsers = new HashMap<>();
        FallbackTypeParsers = new ArrayList<>();

        // Causes the static parsers to be loaded.
        Parsers.load();
    }

    private ParserManager() {}

    private static final Map<Class<?>, List<ParserContext>> TypeParsers;
    private static final List<Tuple<Predicate<Class<?>>, ParserContext>> FallbackTypeParsers;

    @SuppressWarnings("unchecked")
    public static <T> Parser<T> from(@NotNull MethodContext ctx) {
        return (Parser<T>) from(ctx.getContextState().getClazz(), ctx);
    }

    @SuppressWarnings("unchecked")
    public static <T> Parser<T> from(@NotNull Class<T> type, @NotNull MethodContext ctx) {
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
                                                        @NotNull MethodContext ctx) {
        if (parsers.isEmpty())
            return Parsers.OBJECT_PARSER;

        for (ParserContext parser : parsers) {
            if (ctx.getContextState().getAnnotationTypes().contains(parser.annotationType())) {
                ctx.getContextState().getAnnotationTypes().remove(parser.annotationType());
                return parser.parser();
            }
        }
        // Otherwise return the last parser
        return parsers.get(parsers.size() - 1).parser();
    }

    private static boolean filterFallbackParsers(@NotNull Tuple<Predicate<Class<?>>, ParserContext> tuple,
                                                 @NotNull Class<?> type,
                                                 @NotNull MethodContext ctx) {
        if (!tuple.getKey().test(type)) return false;

        Class<?> annotationType = tuple.getValue().annotationType();
        if (annotationType.isAssignableFrom(Void.class))
            return true;
        else if (ctx.getContextState().getAnnotationTypes().contains(annotationType)) {
            ctx.getContextState().getAnnotationTypes().remove(annotationType);
            return true;
        } else if (tuple.getValue().includeClassAnnotations()) {
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
            if (0 < parsers.get(i).parser().getPriority().compareTo(typeParser.getPriority())) {
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
            if (0 < FallbackTypeParsers.get(i).getValue().parser().getPriority().compareTo(typeParser.getPriority())) {
                FallbackTypeParsers.add(i, parser);
                return;
            }
        }
        FallbackTypeParsers.add(parser);
    }
}
