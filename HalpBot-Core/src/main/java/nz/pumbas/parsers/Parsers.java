package nz.pumbas.parsers;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import nz.pumbas.commands.annotations.Unmodifiable;
import nz.pumbas.commands.tokens.context.ParsingContext;
import nz.pumbas.commands.validation.Implicit;
import nz.pumbas.utilities.Exceptional;
import nz.pumbas.utilities.Reflect;

@SuppressWarnings({"rawtypes", "unchecked"})
public final class Parsers
{

    private Parsers() {}

//    public static final TypeParser<Enum> ENUM_PARSER = TypeParser.of(Enum.class, ctx -> {
//        String token = ctx.getNext();
//
//        return Result.of(Reflect.parseEnumValue(ctx.getType(), token),
//            Resource.get("halpbot.commands.match.enum", token, ctx.getType().getSimpleName()));
//    });

    public static final TypeParser<Integer> INTEGER_PARSER = TypeParser.of(Integer.class, ctx ->
        ctx.getNext(Reflect.getSyntax(Integer.class))
            .map(Integer::parseInt)
    );

    public static final TypeParser<Float> FLOAT_PARSER = TypeParser.of(Float.class, ctx ->
        ctx.getNext(Reflect.getSyntax(Float.class))
            .map(Float::parseFloat)
    );

    public static final TypeParser<Double> DOUBLE_PARSER = TypeParser.of(Double.class, ctx ->
        ctx.getNext(Reflect.getSyntax(Double.class))
            .map(Double::parseDouble)
    );

    public static final TypeParser<Character> CHARACTER_PARSER = TypeParser.of(Character.class, ctx ->
        ctx.getNext(Reflect.getSyntax(Character.class))
            .map(s -> s.charAt(0))
    );

    public static final TypeParser<String> STRING_PARSER = TypeParser.of(String.class, ctx ->
        Exceptional.of(ctx.getNext())
    );

    public static final TypeParser<Boolean> BOOLEAN_PARSER = TypeParser.of(Boolean.class, ctx ->
        ctx.getNext(Reflect.getSyntax(Boolean.class))
            .map(s -> {
                String lowered = s.toLowerCase(Locale.ROOT);
                return "true".equals(lowered) || "yes".equals(lowered) || "t".equals(lowered) || "y".equals(lowered);
            })
    );

    public static final TypeParser<List> LIST_PARSER = TypeParser.of(List.class, ctx -> {
        TypeParser<?> elementParser = retrieveParser(Reflect.getArrayType(ctx.getType()), ctx);
        return Exceptional.of(() -> {
            List<Object> list = new ArrayList<>();
            ctx.assertNext('[');

            while (!ctx.isNext(']', true)) {
                elementParser.getParser()
                    .apply(ctx)
                    .present(list::add)
                    .rethrow();
            }
            return list;
        });
    });

    public static final TypeParser<List> IMPLICIT_LIST_PARSER = TypeParser.of(List.class, Implicit.class, ctx -> {
        TypeParser<?> elementParser = retrieveParser(Reflect.getArrayType(ctx.getType()), ctx);
        List<Object> list = new ArrayList<>();

        Exceptional<?> element = elementParser.getParser().apply(ctx)
            .present(list::add);
        if (element.absent()) return Exceptional.of(element.error());

        while (ctx.hasNext()) {
            element = elementParser.getParser().apply(ctx);
            if (element.present()) list.add(element.get());
            else break;
        }

        return Exceptional.of(list);
    });

    public static final TypeParser<List> UNMODIFIABLE_LIST_PARSER = TypeParser.of(List.class, Unmodifiable.class, ctx ->
        retrieveParser(List.class, ctx).getParser()
            .apply(ctx)
            .map(Collections::unmodifiableList)
    );

    public static final TypeParser<Set> SET_PARSER = TypeParser.of(Set.class, ctx ->
        retrieveParser(List.class, ctx).getParser()
            .apply(ctx)
            .map(HashSet::new)
    );

    public static final TypeParser<Set> UNMODIFIABLE_SET_PARSER = TypeParser.of(Set.class, Unmodifiable.class, ctx ->
        retrieveParser(Set.class, ctx).getParser()
            .apply(ctx)
            .map(Collections::unmodifiableSet)
    );

    public static final TypeParser<Object> ARRAY_PARSER = TypeParser.of(Class::isArray, ctx ->
        retrieveParser(List.class, ctx).getParser()
            .apply(ctx)
            .map(list -> Reflect.toArray(Reflect.getArrayType(ctx.getType()), list))
    );

    public static <T> TypeParser<T> retrieveParser(Class<T> type, ParsingContext ctx) {
        //TODO: This
        return null;
    }
}
