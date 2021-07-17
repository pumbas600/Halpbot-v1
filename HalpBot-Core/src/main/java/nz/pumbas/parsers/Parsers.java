package nz.pumbas.parsers;

import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.PrivateChannel;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.GenericEvent;

import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

import nz.pumbas.commands.annotations.Source;
import nz.pumbas.commands.annotations.Unmodifiable;
import nz.pumbas.commands.commandadapters.AbstractCommandAdapter;
import nz.pumbas.commands.tokens.context.ParsingContext;
import nz.pumbas.commands.validation.Implicit;
import nz.pumbas.objects.Tuple;
import nz.pumbas.utilities.Exceptional;
import nz.pumbas.utilities.Reflect;
import nz.pumbas.utilities.enums.Priority;

@SuppressWarnings({"rawtypes", "unchecked", "ClassWithTooManyFields"})
public final class Parsers
{

    private Parsers() {}

    private static final Map<Class<?>, List<Tuple<Class<?>, TypeParser<?>>>> TypeParsers = new HashMap<>();
    private static final List<Tuple<Predicate<Class<?>>, TypeParser<?>>> FallbackTypeParsers = new ArrayList<>();

    public static <T> TypeParser<T> from(@NotNull ParsingContext ctx) {
        return (TypeParser<T>) from(ctx.clazz(), ctx);
    }

    public static <T> TypeParser<T> from(@NotNull Class<T> type,
                                         @NotNull ParsingContext ctx) {
        if (TypeParsers.containsKey(type)) {
            return (TypeParser<T>) retrieveParserByAnnotation(TypeParsers.get(type), ctx);
        }
        return (TypeParser<T>) TypeParsers.keySet()
            .stream()
            .filter(c -> c.isAssignableFrom(type))
            .findFirst()
            .map(c -> (Object) retrieveParserByAnnotation(TypeParsers.get(c), ctx))
            .orElseGet(() -> FallbackTypeParsers.stream()
                .filter(t -> t.getKey().test(type))
                .findFirst()
                .map(Tuple::getValue)
                .orElseThrow());

    }

    private static TypeParser<?> retrieveParserByAnnotation(@NotNull List<Tuple<Class<?>, TypeParser<?>>> parsers,
                                                            @NotNull ParsingContext ctx) {
        assert !parsers.isEmpty();

        for (Tuple<Class<?>, TypeParser<?>> parser : parsers) {
            if (ctx.annotationTypes().contains(parser.getKey())) {
                ctx.annotationTypes().remove(parser.getKey());
                return parser.getValue();
            }
        }
        //otherwise return the last parser
        return parsers.get(parsers.size() - 1).getValue();
    }

    public static void registerParser(@NotNull Class<?> type,
                                      @NotNull Class<?> annotationType,
                                      @NotNull TypeParser<?> typeParser) {
        if (!TypeParsers.containsKey(type))
            TypeParsers.put(type, new ArrayList<>());

        List<Tuple<Class<?>, TypeParser<?>>> parsers = TypeParsers.get(type);
        for (int i = 0; i < parsers.size(); i++) {
            if (0 < parsers.get(i).getValue().getPriority().compareTo(typeParser.getPriority())) {
                parsers.add(i, Tuple.of(annotationType, typeParser));
                return;
            }
        }
        parsers.add(Tuple.of(annotationType, typeParser));
    }

    public static void registerParser(@NotNull Predicate<Class<?>> filter,
                                      @NotNull TypeParser<?> typeParser) {

        FallbackTypeParsers.add(Tuple.of(filter, typeParser));
    }

    //region Parsers

    //region Simple Parsers

    public static final TypeParser<Integer> INTEGER_PARSER = TypeParser.builder(Integer.class)
        .convert(ctx ->
            ctx.getNext(Reflect.getSyntax(Integer.class))
                .map(Integer::parseInt))
        .register();

    public static final TypeParser<Float> FLOAT_PARSER = TypeParser.builder(Float.class)
        .convert(ctx ->
            ctx.getNext(Reflect.getSyntax(Float.class))
                .map(Float::parseFloat))
        .register();

    public static final TypeParser<Double> DOUBLE_PARSER = TypeParser.builder(Double.class)
        .convert(ctx ->
            ctx.getNext(Reflect.getSyntax(Double.class))
                .map(Double::parseDouble))
        .register();

    public static final TypeParser<Character> CHARACTER_PARSER = TypeParser.builder(Character.class)
        .convert(ctx ->
            ctx.getNext(Reflect.getSyntax(Character.class))
                .map(in -> in.charAt(0)))
        .register();

    public static final TypeParser<String> STRING_PARSER = TypeParser.builder(String.class)
        .convert(ctx -> Exceptional.of(ctx.getNext()))
        .register();

    public static final TypeParser<Boolean> BOOLEAN_PARSER = TypeParser.builder(Boolean.class)
        .convert(ctx ->
            ctx.getNext(Reflect.getSyntax(Boolean.class))
                .map(in -> {
                    String lowered = in.toLowerCase(Locale.ROOT);
                    return "true".equals(lowered) || "yes".equals(lowered) || "t".equals(lowered) || "y".equals(lowered);
            }))
        .register();

    public static final TypeParser<Enum> ENUM_PARSER = TypeParser.builder(Enum.class)
        .convert(ctx ->
            Exceptional.of(
                Reflect.parseEnumValue(ctx.clazz(), ctx.getNext())))
        .register();

    //endregion

    //region Collection Parsers

    public static final TypeParser<List> LIST_PARSER = TypeParser.builder(List.class)
        .convert(ctx -> {
            Type type = Reflect.getGenericType(ctx.type());
            TypeParser<?> elementParser = from(
                Reflect.wrapPrimative(Reflect.asClass(type)),
                ctx);

            ctx.type(type);

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
        }).register();

    public static final TypeParser<List> IMPLICIT_LIST_PARSER = TypeParser.builder(List.class)
        .annotation(Implicit.class)
        .priority(Priority.LAST)
        .convert(ctx -> {
            Type type = Reflect.getGenericType(ctx.type());
            TypeParser<?> elementParser = from(
                Reflect.wrapPrimative(Reflect.asClass(type)),
                ctx);

            ctx.type(type);
            List<Object> list = new ArrayList<>();

            Exceptional<?> element = elementParser.getParser()
                .apply(ctx)
                .present(list::add);
            if (element.absent()) return Exceptional.of(element.error());

            while (ctx.hasNext()) {
                element = elementParser.getParser().apply(ctx);
                if (element.present()) list.add(element.get());
                else break;
            }

            return Exceptional.of(list);
        }).register();

    public static final TypeParser<List> UNMODIFIABLE_LIST_PARSER = TypeParser.builder(List.class)
        .annotation(Unmodifiable.class)
        .priority(Priority.EARLY)
        .convert(ctx ->
            from(List.class, ctx).getParser()
                .apply(ctx)
                .map(Collections::unmodifiableList))
        .register();

    public static final TypeParser<Set> SET_PARSER = TypeParser.builder(Set.class)
        .convert(ctx ->
            from(List.class, ctx).getParser()
                .apply(ctx)
                .map(HashSet::new))
        .register();


    public static final TypeParser<Set> UNMODIFIABLE_SET_PARSER = TypeParser.builder(Set.class)
        .annotation(Unmodifiable.class)
        .priority(Priority.EARLY)
        .convert(ctx ->
            from(Set.class, ctx).getParser()
                .apply(ctx)
                .map(Collections::unmodifiableSet))
        .register();

    public static final TypeParser<Object> ARRAY_PARSER = TypeParser.builder(Class::isArray)
        .convert(ctx ->
            from(List.class, ctx).getParser()
                .apply(ctx)
                .map(list -> Reflect.toArray(Reflect.getArrayType(ctx.clazz()), list)))
        .register();

    //endregion

    //region Source Parsers

    public static final TypeParser<GenericEvent> EVENT_PARSER = TypeParser.builder(GenericEvent.class)
        .convert(ctx -> Exceptional.of(ctx.event()))
        .register();

    public static final TypeParser<AbstractCommandAdapter> COMMAND_ADAPTER_PARSER =
        TypeParser.builder(AbstractCommandAdapter.class)
            .convert(ctx -> Exceptional.of(ctx.commandAdapter()))
            .register();

    public static final TypeParser<MessageChannel> SOURCE_MESSAGE_CHANNEL_PARSER =
        TypeParser.builder(MessageChannel.class)
            .annotation(Source.class)
            .priority(Priority.FIRST)
            .convert(ctx -> Exceptional.of(ctx.event().getChannel()))
            .register();

    public static final TypeParser<TextChannel> SOURCE_TEXT_CHANNEL_PARSER =
        TypeParser.builder(TextChannel.class)
            .annotation(Source.class)
            .priority(Priority.FIRST)
            .convert(ctx -> Exceptional.of(() -> ctx.event().getTextChannel()))
            .register();

    public static final TypeParser<PrivateChannel> SOURCE_PRIVATE_CHANNEL_PARSER =
        TypeParser.builder(PrivateChannel.class)
            .annotation(Source.class)
            .priority(Priority.FIRST)
            .convert(ctx -> Exceptional.of(() -> ctx.event().getPrivateChannel()))
            .register();

    public static final TypeParser<User> SOURCE_USER_PARSER = TypeParser.builder(User.class)
        .annotation(Source.class)
        .priority(Priority.FIRST)
        .convert(ctx -> Exceptional.of(ctx.event().getAuthor()))
        .register();

    public static final TypeParser<Guild> SOURCE_GUILD_PARSER = TypeParser.builder(Guild.class)
        .annotation(Source.class)
        .priority(Priority.FIRST)
        .convert(ctx -> Exceptional.of(ctx.event().getGuild()))
        .register();

    public static final TypeParser<ChannelType> SOURCE_CHANNEL_TYPE_PARSER = TypeParser.builder(ChannelType.class)
        .annotation(Source.class)
        .priority(Priority.FIRST)
        .convert(ctx -> Exceptional.of(ctx.event().getChannelType()))
        .register();

    //endregion

    //endregion
}
