/*
 * MIT License
 *
 * Copyright (c) 2021 pumbas600
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package nz.pumbas.halpbot.converters;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.PrivateChannel;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.utils.MiscUtil;

import nz.pumbas.halpbot.commands.annotations.Explicit;
import nz.pumbas.halpbot.commands.annotations.Id;
import nz.pumbas.halpbot.commands.annotations.Source;
import nz.pumbas.halpbot.commands.commandadapters.AbstractCommandAdapter;
import nz.pumbas.halpbot.commands.exceptions.TokenCommandException;
import nz.pumbas.halpbot.commands.commandmethods.SimpleCommand;
import nz.pumbas.halpbot.commands.CommandManager;
import nz.pumbas.halpbot.commands.context.InvocationContext;
import nz.pumbas.halpbot.commands.annotations.Implicit;
import nz.pumbas.halpbot.commands.persistant.PersistantUserData;
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
public final class Converters
{

    /**
     * A static method that can be called so that the converters in this file are loaded.
     * Note: This is already automatically called by {@link ConverterHandlerImpl}.
     */
    public static void load() {}

    private Converters() {}

    //region Converters

    //region Simple Converters

    public static final TypeConverter<Byte> BYTE_CONVERTER = TypeConverter.builder(Byte.class)
        .convert(ctx ->
            ctx.getNext(Reflect.getSyntax(Byte.class))
                .map(Byte::parseByte))
        .optionType(OptionType.INTEGER)
        .register();

    public static final TypeConverter<Short> SHORT_CONVERTER = TypeConverter.builder(Short.class)
        .convert(ctx ->
            ctx.getNext(Reflect.getSyntax(Short.class))
                .map(Short::parseShort))
        .optionType(OptionType.INTEGER)
        .register();

    public static final TypeConverter<Integer> INTEGER_CONVERTER = TypeConverter.builder(Integer.class)
        .convert(ctx ->
            ctx.getNext(Reflect.getSyntax(Integer.class))
                .map(Integer::parseInt))
        .optionType(OptionType.INTEGER)
        .register();

    public static final TypeConverter<Long> LONG_CONVERTER = TypeConverter.builder(Long.class)
        .convert(ctx ->
            ctx.getNext(Reflect.getSyntax(Long.class))
                .map(number -> {
                    if (number.startsWith("-"))
                        return Long.parseLong(number);
                    else return Long.parseUnsignedLong(number);
                }))
        .optionType(OptionType.INTEGER)
        .register();

    public static final TypeConverter<Float> FLOAT_CONVERTER = TypeConverter.builder(Float.class)
        .convert(ctx ->
            ctx.getNext(Reflect.getSyntax(Float.class))
                .map(Float::parseFloat))
        .register();

    public static final TypeConverter<Double> DOUBLE_CONVERTER = TypeConverter.builder(Double.class)
        .convert(ctx ->
            ctx.getNext(Reflect.getSyntax(Double.class))
                .map(Double::parseDouble))
        .register();

    public static final TypeConverter<Character> CHARACTER_CONVERTER = TypeConverter.builder(Character.class)
        .convert(ctx ->
            ctx.getNext(Reflect.getSyntax(Character.class))
                .map(in -> in.charAt(0)))
        .optionType(OptionType.STRING)
        .register();

    public static final TypeConverter<String> STRING_CONVERTER = TypeConverter.builder(String.class)
        .convert(InvocationContext::getNextSafe)
        .optionType(OptionType.STRING)
        .register();

    public static final TypeConverter<String> REMAINING_STRINGS_CONVERTER = TypeConverter.builder(String.class)
        .annotation(Remaining.class)
        .priority(Priority.EARLY)
        .convert(ctx -> Exceptional.of(ctx::getRemaining))
        .register();

    public static final TypeConverter<String> EXPLICIT_STRING_CONVERTER = TypeConverter.builder(String.class)
        .annotation(Explicit.class)
        .priority(Priority.EARLY)
        .convert(ctx -> ctx.getNextSurrounded("\"", "\"")
            .orExceptional(() -> STRING_CONVERTER.getMapper().apply(ctx)))
        .register();

    public static final TypeConverter<Boolean> BOOLEAN_CONVERTER = TypeConverter.builder(Boolean.class)
        .convert(ctx ->
            ctx.getNext(Reflect.getSyntax(Boolean.class))
                .map(in -> {
                    String lowered = in.toLowerCase(Locale.ROOT);
                    return "true".equals(lowered) || "yes".equals(lowered) || "t".equals(lowered) || "y".equals(lowered);
                }))
        .optionType(OptionType.BOOLEAN)
        .register();

    public static final TypeConverter<Enum> ENUM_CONVERTER = TypeConverter.builder(Enum.class)
        .convert(ctx ->
            Exceptional.of(
                Reflect.parseEnumValue(ctx.getContextState().getClazz(), ctx.getNext()))
                .orElse(() -> {
                    throw new IllegalArgumentException(
                        "That didn't seem to be a valid value for " + ctx.getContextState().getClazz().getSimpleName());
                }))
        .optionType(OptionType.STRING)
        .register();

    public static final TypeConverter<Object> OBJECT_CONVERTER = TypeConverter.builder(c -> true)
        .convert(ctx -> {
            Class<?> clazz = ctx.getContextState().getClazz();
            String expectedTypeAlias = CommandManager.getTypeAlias(clazz);
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
            for (SimpleCommand simpleCommand : CommandManager.getParsedConstructors(clazz)) {
                ctx.setCurrentIndex(currentIndex);
                result = simpleCommand.parse(ctx, true);
                if (result.present() && ctx.isNext(']', true)) break;

                else if (!result.caught()) result = Exceptional.of(
                    new TokenCommandException("There seems to have been an error when constructing the " + expectedTypeAlias));

            }
            return result;
        }).build();

    public static final TypeConverter<Object> CHILDREN_TYPE_CONVERTER = TypeConverter.builder(Objects::nonNull)
        .priority(Priority.LAST)
        .annotation(Children.class)
        .convert(ctx -> {
            final Exceptional<Object> result = OBJECT_CONVERTER.getMapper().apply(ctx);
            if (result.isErrorAbsent())
                return result;
            return ctx.getAnnotation(Children.class).flatMap(children -> {
                // If there are no children then it will return the error in 'result'
                Exceptional<Object> parsed = result;

                for (Class<?> child : children.value()) {
                    ctx.getContextState().setType(child);
                    parsed = OBJECT_CONVERTER.getMapper().apply(ctx);
                    if (parsed.isErrorAbsent()) break;
                }
                return parsed;
            });
        }).register();

    //endregion

    //region Collection Converters

    public static final TypeConverter<List> LIST_CONVERTER = TypeConverter.builder(List.class)
        .convert(ctx -> {
            Type subType = Reflect.getGenericType(ctx.getContextState().getType());
            Converter<?> elementConverter = HalpbotUtils.context().get(ConverterHandler.class)
                .from(Reflect.wrapPrimative(Reflect.asClass(subType)), ctx);

            return Exceptional.of(() -> {
                List<Object> list = new ArrayList<>();
                ctx.assertNext('[');
                ctx.getContextState().setType(subType);

                while (!ctx.isNext(']', true)) {
                    elementConverter.getMapper()
                        .apply(ctx)
                        .present(list::add)
                        .rethrow();
                }
                return list;
            });
        }).register();

    public static final TypeConverter<List> IMPLICIT_LIST_CONVERTER = TypeConverter.builder(List.class)
        .annotation(Implicit.class)
        .priority(Priority.LAST)
        .convert(ctx -> {
            Exceptional<List> listExceptional = LIST_CONVERTER.getMapper().apply(ctx);
            if (listExceptional.isErrorAbsent())
                return listExceptional;

            Type subType = Reflect.getGenericType(ctx.getContextState().getType());
            Converter<?> elementConverter = HalpbotUtils.context().get(ConverterHandler.class)
                .from(Reflect.wrapPrimative(Reflect.asClass(subType)), ctx);
            ctx.getContextState().setType(subType);

            List<Object> list = new ArrayList<>();

            Exceptional<?> element = elementConverter.getMapper()
                .apply(ctx)
                .present(list::add);
            if (element.absent()) return Exceptional.of(element.error());

            while (ctx.hasNext()) {
                element = elementConverter.getMapper()
                    .apply(ctx);
                if (element.present()) list.add(element.get());
                else break;
            }

            return Exceptional.of(list);

        }).register();

    public static final TypeConverter<List> UNMODIFIABLE_LIST_CONVERTER = TypeConverter.builder(List.class)
        .annotation(Unmodifiable.class)
        .priority(Priority.EARLY)
        .convert(ctx ->
            HalpbotUtils.context().get(ConverterHandler.class)
                .from(List.class, ctx)
                .getMapper()
                .apply(ctx)
                .map(Collections::unmodifiableList))
        .register();

    public static final TypeConverter<Set> SET_CONVERTER = TypeConverter.builder(Set.class)
        .convert(ctx ->
            HalpbotUtils.context().get(ConverterHandler.class)
                .from(List.class, ctx)
                .getMapper()
                .apply(ctx)
                .map(HashSet::new))
        .register();


    public static final TypeConverter<Set> UNMODIFIABLE_SET_CONVERTER = TypeConverter.builder(Set.class)
        .annotation(Unmodifiable.class)
        .priority(Priority.EARLY)
        .convert(ctx ->
            HalpbotUtils.context().get(ConverterHandler.class)
                .from(Set.class, ctx)
                .getMapper()
                .apply(ctx)
                .map(Collections::unmodifiableSet))
        .register();

    public static final TypeConverter<Object> ARRAY_CONVERTER = TypeConverter.builder(Class::isArray)
        .convert(ctx ->
            HalpbotUtils.context().get(ConverterHandler.class)
                .from(List.class, ctx)
                .getMapper()
                .apply(ctx)
                .map(list ->
                    Reflect.toArray(Reflect.getArrayType(ctx.getContextState().getClazz()), list)))
        .register();

    //endregion

    //region Misc

    public static final TypeConverter<PersistantUserData> PERSISTANT_USER_DATA_CONVERTER =
        TypeConverter.builder(PersistantUserData.class)
            .notCommandParameter()
            .convert(ctx -> Exceptional.of(
                ctx.getCommandAdapter().getPersistantUserData(
                    (Class<? extends PersistantUserData>) ctx.getContextState().getClazz(),
                    ctx.getEvent().getAuthor().getIdLong())
            ))
            .register();

    //endregion

    //region JDA Converters

    public static final TypeConverter<TextChannel> TEXT_CHANNEL_CONVERTER = TypeConverter.builder(TextChannel.class)
        .convert(ctx ->
            ctx.getNextSurrounded("<#", ">")
                .map(ctx.getEvent().getGuild()::getTextChannelById)
                .orExceptional(
                    () -> LONG_CONVERTER.getMapper()
                        .apply(ctx)
                        .map(id -> ctx.getEvent().getGuild().getTextChannelById(id))
                ))
        .optionType(OptionType.CHANNEL)
        .register();

    public static final TypeConverter<Member> MEMBER_CONVERTER = TypeConverter.builder(Member.class)
        .convert(ctx ->
            ctx.getNextSurrounded("<@!", ">")
                .map(id -> ctx.getEvent().getGuild().retrieveMemberById(id).complete())
                .orExceptional(
                    () -> LONG_CONVERTER.getMapper()
                        .apply(ctx)
                        .map(id -> ctx.getEvent().getGuild().retrieveMemberById(id).complete())
                ))
        .optionType(OptionType.USER)
        .register();

    public static final TypeConverter<User> USER_CONVERTER = TypeConverter.builder(User.class)
        .convert(ctx ->
            ctx.getNextSurrounded("<@!", ">")
                .map(id -> ctx.getEvent().getJDA().retrieveUserById(id).complete())
                .orExceptional(
                    () -> LONG_CONVERTER.getMapper()
                        .apply(ctx)
                        .map(id -> ctx.getEvent().getJDA().retrieveUserById(id).complete())
                ))
        .optionType(OptionType.USER)
        .register();

    public static final TypeConverter<Long> ID_LONG_CONVERTER = TypeConverter.builder(Long.class)
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
                return parsedId.orExceptional(() -> LONG_CONVERTER.getMapper().apply(ctx));
            })
        .optionType(OptionType.MENTIONABLE)
        .register();

    //endregion

    //region Source Converters

    public static final TypeConverter<GenericEvent> EVENT_CONVERTER = TypeConverter.builder(GenericEvent.class)
        .notCommandParameter()
        .convert(ctx -> Exceptional.of(ctx.getEvent()))
        .register();

    public static final TypeConverter<AbstractCommandAdapter> COMMAND_ADAPTER_CONVERTER =
        TypeConverter.builder(AbstractCommandAdapter.class)
            .notCommandParameter()
            .convert(ctx -> Exceptional.of(ctx.getCommandAdapter()))
            .register();

    public static final TypeConverter<JDA> JDA_CONVERTER = TypeConverter.builder(JDA.class)
        .notCommandParameter()
        .convert(ctx -> Exceptional.of(ctx.getEvent().getJDA()))
        .register();

    public static final TypeConverter<MessageChannel> SOURCE_MESSAGE_CHANNEL_CONVERTER =
        TypeConverter.builder(MessageChannel.class)
            .annotation(Source.class)
            .priority(Priority.FIRST)
            .convert(ctx -> Exceptional.of(ctx.getEvent().getChannel()))
            .register();

    public static final TypeConverter<TextChannel> SOURCE_TEXT_CHANNEL_CONVERTER =
        TypeConverter.builder(TextChannel.class)
            .annotation(Source.class)
            .priority(Priority.FIRST)
            .convert(ctx -> Exceptional.of(() -> ctx.getEvent().getTextChannel()))
            .register();

    public static final TypeConverter<PrivateChannel> SOURCE_PRIVATE_CHANNEL_CONVERTER =
        TypeConverter.builder(PrivateChannel.class)
            .annotation(Source.class)
            .priority(Priority.FIRST)
            .convert(ctx -> Exceptional.of(() -> ctx.getEvent().getPrivateChannel()))
            .register();

    public static final TypeConverter<User> SOURCE_USER_CONVERTER = TypeConverter.builder(User.class)
        .annotation(Source.class)
        .priority(Priority.FIRST)
        .convert(ctx -> Exceptional.of(ctx.getEvent().getAuthor()))
        .register();

    public static final TypeConverter<Guild> SOURCE_GUILD_CONVERTER = TypeConverter.builder(Guild.class)
        .annotation(Source.class)
        .priority(Priority.FIRST)
        .convert(ctx -> Exceptional.of(ctx.getEvent().getGuild()))
        .register();

    public static final TypeConverter<ChannelType> SOURCE_CHANNEL_TYPE_CONVERTER = TypeConverter.builder(ChannelType.class)
        .annotation(Source.class)
        .priority(Priority.FIRST)
        .convert(ctx -> Exceptional.of(ctx.getEvent().getChannelType()))
        .register();

    //endregion

    //endregion
}
