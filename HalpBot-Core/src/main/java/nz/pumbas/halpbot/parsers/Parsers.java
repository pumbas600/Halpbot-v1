package nz.pumbas.halpbot.parsers;

import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.PrivateChannel;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.utils.MiscUtil;

import nz.pumbas.halpbot.commands.annotations.Id;
import nz.pumbas.halpbot.commands.annotations.Source;
import nz.pumbas.halpbot.commands.commandadapters.AbstractCommandAdapter;
import nz.pumbas.halpbot.commands.exceptions.TokenCommandException;
import nz.pumbas.halpbot.commands.tokens.TokenCommand;
import nz.pumbas.halpbot.commands.tokens.TokenManager;
import nz.pumbas.halpbot.commands.tokens.context.InvocationContext;
import nz.pumbas.halpbot.commands.annotations.Implicit;
import nz.pumbas.halpbot.objects.Exceptional;
import nz.pumbas.halpbot.utilities.HalpbotUtils;
import nz.pumbas.halpbot.utilities.Reflect;
import nz.pumbas.halpbot.utilities.enums.Priority;
import nz.pumbas.halpbot.commands.annotations.Children;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

import nz.pumbas.halpbot.commands.annotations.Remaining;
import nz.pumbas.halpbot.commands.annotations.Unmodifiable;

@SuppressWarnings({"rawtypes", "unchecked", "ClassWithTooManyFields"})
public final class Parsers
{

    /**
     * A static method that can be called so that the parsers in this file are loaded.
     * Note: This is already automatically called by {@link ParserHandlerImpl}.
     */
    public static void load() {}

    private Parsers() {}

    //region Parsers

    //region Simple Parsers

    public static final TypeParser<Byte> BYTE_PARSER = TypeParser.builder(Byte.class)
        .convert(ctx ->
            ctx.getNext(Reflect.getSyntax(Byte.class))
                .map(Byte::parseByte))
        .register();

    public static final TypeParser<Short> SHORT_PARSER = TypeParser.builder(Short.class)
        .convert(ctx ->
            ctx.getNext(Reflect.getSyntax(Short.class))
                .map(Short::parseShort))
        .register();

    public static final TypeParser<Integer> INTEGER_PARSER = TypeParser.builder(Integer.class)
        .convert(ctx ->
            ctx.getNext(Reflect.getSyntax(Integer.class))
                .map(Integer::parseInt))
        .register();

    public static final TypeParser<Long> LONG_PARSER = TypeParser.builder(Long.class)
        .convert(ctx ->
            ctx.getNext(Reflect.getSyntax(Long.class))
                .map(number -> {
                    if (number.startsWith("-"))
                        return Long.parseLong(number);
                    else return Long.parseUnsignedLong(number);
                }))
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
        .convert(InvocationContext::getNextSafe)
        .register();

    public static final TypeParser<String> REMAINING_STRINGS_PARSER = TypeParser.builder(String.class)
        .annotation(Remaining.class)
        .priority(Priority.EARLY)
        .convert(ctx -> Exceptional.of(ctx::getRemaining))
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
                Reflect.parseEnumValue(ctx.getContextState().getClazz(), ctx.getNext())))
        .register();

    public static final TypeParser<Object> OBJECT_PARSER = TypeParser.builder(c -> true)
        .convert(ctx -> {
            Class<?> clazz = ctx.getContextState().getClazz();
            String expectedTypeAlias = TokenManager.getTypeAlias(clazz);
            Exceptional<String> typeAlias = ctx.getNext("[", false);

            if (typeAlias.caught())
                return Exceptional.of(typeAlias.error());
            if (typeAlias.isEmpty())
                return Exceptional.of(new TokenCommandException("Missing '[' when creating object " + expectedTypeAlias));

            if (!typeAlias.get().equalsIgnoreCase(expectedTypeAlias))
                return Exceptional.of(
                    new TokenCommandException("Expected the alias " + expectedTypeAlias + " but got " + typeAlias.get()));

            ctx.assertNext('[');
            int currentIndex = ctx.getCurrentIndex();
            Exceptional<Object> result = Exceptional.empty();
            for (TokenCommand tokenCommand : TokenManager.getParsedConstructors(clazz)) {
                ctx.setCurrentIndex(currentIndex);
                result = tokenCommand.parse(ctx, true);
                if (result.present() && ctx.isNext(']', true)) break;

                else if (!result.caught()) result = Exceptional.of(
                    new TokenCommandException("There seems to have been an error when constructing the " + expectedTypeAlias));

            }
            return result;
        }).build();

    public static final TypeParser<Object> CHILDREN_TYPE_PARSER = TypeParser.builder(Objects::nonNull)
        .priority(Priority.LAST)
        .annotation(Children.class)
        .convert(ctx -> {
            final Exceptional<Object> result = OBJECT_PARSER.getMapper().apply(ctx);
            if (result.isErrorAbsent())
                return result;
            return ctx.getAnnotation(Children.class).flatMap(children -> {
                // If there are no children then it will return the error in 'result'
                Exceptional<Object> parsed = result;

                for (Class<?> child : children.value()) {
                    ctx.getContextState().setType(child);
                    parsed = OBJECT_PARSER.getMapper().apply(ctx);
                    if (parsed.isErrorAbsent()) break;
                }
                return parsed;
            });
        }).register();

    //endregion

    //region Collection Parsers

    public static final TypeParser<List> LIST_PARSER = TypeParser.builder(List.class)
        .convert(ctx -> {
            Type subType = Reflect.getGenericType(ctx.getContextState().getType());
            Parser<?> elementParser = HalpbotUtils.context().get(ParserHandler.class)
                .from(Reflect.wrapPrimative(Reflect.asClass(subType)), ctx);

            return Exceptional.of(() -> {
                List<Object> list = new ArrayList<>();
                ctx.assertNext('[');
                ctx.getContextState().setType(subType);

                while (!ctx.isNext(']', true)) {
                    elementParser.getMapper()
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
            Exceptional<List> listExceptional = LIST_PARSER.getMapper().apply(ctx);
            if (listExceptional.isErrorAbsent())
                return listExceptional;

            Type subType = Reflect.getGenericType(ctx.getContextState().getType());
            Parser<?> elementParser = HalpbotUtils.context().get(ParserHandler.class)
                .from(Reflect.wrapPrimative(Reflect.asClass(subType)), ctx);
            ctx.getContextState().setType(subType);

            List<Object> list = new ArrayList<>();

            Exceptional<?> element = elementParser.getMapper()
                .apply(ctx)
                .present(list::add);
            if (element.absent()) return Exceptional.of(element.error());

            while (ctx.hasNext()) {
                element = elementParser.getMapper()
                    .apply(ctx);
                if (element.present()) list.add(element.get());
                else break;
            }

            return Exceptional.of(list);

        }).register();

    public static final TypeParser<List> UNMODIFIABLE_LIST_PARSER = TypeParser.builder(List.class)
        .annotation(Unmodifiable.class)
        .priority(Priority.EARLY)
        .convert(ctx ->
            HalpbotUtils.context().get(ParserHandler.class)
                .from(List.class, ctx)
                .getMapper()
                .apply(ctx)
                .map(Collections::unmodifiableList))
        .register();

    public static final TypeParser<Set> SET_PARSER = TypeParser.builder(Set.class)
        .convert(ctx ->
            HalpbotUtils.context().get(ParserHandler.class)
                .from(List.class, ctx)
                .getMapper()
                .apply(ctx)
                .map(HashSet::new))
        .register();


    public static final TypeParser<Set> UNMODIFIABLE_SET_PARSER = TypeParser.builder(Set.class)
        .annotation(Unmodifiable.class)
        .priority(Priority.EARLY)
        .convert(ctx ->
            HalpbotUtils.context().get(ParserHandler.class)
                .from(Set.class, ctx)
                .getMapper()
                .apply(ctx)
                .map(Collections::unmodifiableSet))
        .register();

    public static final TypeParser<Object> ARRAY_PARSER = TypeParser.builder(Class::isArray)
        .convert(ctx ->
            HalpbotUtils.context().get(ParserHandler.class)
                .from(List.class, ctx)
                .getMapper()
                .apply(ctx)
                .map(list ->
                    Reflect.toArray(Reflect.getArrayType(ctx.getContextState().getClazz()), list)))
        .register();

    //endregion

    //region JDA Parsers

    public static final TypeParser<TextChannel> TEXT_CHANNEL_PARSER = TypeParser.builder(TextChannel.class)
        .convert(ctx ->
            ctx.getNextSurrounded("<#", ">")
                .map(ctx.getEvent().getGuild()::getTextChannelById))
        .register();

    public static final TypeParser<Member> MEMBER_PARSER = TypeParser.builder(Member.class)
        .convert(ctx ->
            ctx.getNextSurrounded("<@!", ">")
                .map(id -> ctx.getEvent().getGuild().retrieveMemberById(id).complete()))
        .register();

    public static final TypeParser<User> USER_PARSER = TypeParser.builder(User.class)
        .convert(ctx ->
            ctx.getNextSurrounded("<@!", ">")
                .map(id -> ctx.getEvent().getJDA().retrieveUserById(id).complete()))
        .register();

    public static final TypeParser<Long> ID_LONG_PARSER = TypeParser.builder(Long.class)
        .annotation(Id.class)
        .priority(Priority.EARLY)
        .convert(
            ctx -> {
                Class<?> idType = ctx.getAnnotation(Id.class).get().value();
                Exceptional<Long> parsedId = Exceptional.empty();

                if (TextChannel.class.isAssignableFrom(idType))
                    parsedId = ctx.getNextSurrounded("<#", ">").map(MiscUtil::parseSnowflake);
                else if (Member.class.isAssignableFrom(idType) || User.class.isAssignableFrom(idType))
                    parsedId = ctx.getNextSurrounded("<@!", ">").map(MiscUtil::parseSnowflake);
                return parsedId.absent() ? LONG_PARSER.getMapper().apply(ctx) : parsedId;
            })
        .register();

    //endregion

    //region Source Parsers

    public static final TypeParser<GenericEvent> EVENT_PARSER = TypeParser.builder(GenericEvent.class)
        .convert(ctx -> Exceptional.of(ctx.getEvent()))
        .register();

    public static final TypeParser<AbstractCommandAdapter> COMMAND_ADAPTER_PARSER =
        TypeParser.builder(AbstractCommandAdapter.class)
            .convert(ctx -> Exceptional.of(ctx.getCommandAdapter()))
            .register();

    public static final TypeParser<MessageChannel> SOURCE_MESSAGE_CHANNEL_PARSER =
        TypeParser.builder(MessageChannel.class)
            .annotation(Source.class)
            .priority(Priority.FIRST)
            .convert(ctx -> Exceptional.of(ctx.getEvent().getChannel()))
            .register();

    public static final TypeParser<TextChannel> SOURCE_TEXT_CHANNEL_PARSER =
        TypeParser.builder(TextChannel.class)
            .annotation(Source.class)
            .priority(Priority.FIRST)
            .convert(ctx -> Exceptional.of(() -> ctx.getEvent().getTextChannel()))
            .register();

    public static final TypeParser<PrivateChannel> SOURCE_PRIVATE_CHANNEL_PARSER =
        TypeParser.builder(PrivateChannel.class)
            .annotation(Source.class)
            .priority(Priority.FIRST)
            .convert(ctx -> Exceptional.of(() -> ctx.getEvent().getPrivateChannel()))
            .register();

    public static final TypeParser<User> SOURCE_USER_PARSER = TypeParser.builder(User.class)
        .annotation(Source.class)
        .priority(Priority.FIRST)
        .convert(ctx -> Exceptional.of(ctx.getEvent().getAuthor()))
        .register();

    public static final TypeParser<Guild> SOURCE_GUILD_PARSER = TypeParser.builder(Guild.class)
        .annotation(Source.class)
        .priority(Priority.FIRST)
        .convert(ctx -> Exceptional.of(ctx.getEvent().getGuild()))
        .register();

    public static final TypeParser<ChannelType> SOURCE_CHANNEL_TYPE_PARSER = TypeParser.builder(ChannelType.class)
        .annotation(Source.class)
        .priority(Priority.FIRST)
        .convert(ctx -> Exceptional.of(ctx.getEvent().getChannelType()))
        .register();

    //endregion

    //endregion
}
