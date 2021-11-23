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
import net.dv8tion.jda.api.interactions.Interaction;
import net.dv8tion.jda.api.interactions.commands.OptionType;

import org.dockbox.hartshorn.core.HartshornUtils;
import org.dockbox.hartshorn.core.annotations.service.Service;
import org.dockbox.hartshorn.core.context.ApplicationContext;
import org.dockbox.hartshorn.core.context.element.TypeContext;
import org.dockbox.hartshorn.core.domain.Exceptional;

import nz.pumbas.halpbot.adapters.AbstractHalpbotAdapter;
import nz.pumbas.halpbot.adapters.HalpbotAdapter;
import nz.pumbas.halpbot.adapters.HalpbotCore;
import nz.pumbas.halpbot.converters.annotations.Ignore;
import nz.pumbas.halpbot.converters.annotations.parameter.Source;
import nz.pumbas.halpbot.commands.commandadapters.AbstractCommandAdapter;
import nz.pumbas.halpbot.commands.exceptions.CommandException;
import nz.pumbas.halpbot.commands.commandmethods.SimpleCommand;
import nz.pumbas.halpbot.commands.CommandManager;
import nz.pumbas.halpbot.commands.context.InvocationContext;
import nz.pumbas.halpbot.converters.annotations.parameter.Implicit;
import nz.pumbas.halpbot.commands.persistant.PersistantUserData;
import nz.pumbas.halpbot.converters.annotations.NonCommandParameters;
import nz.pumbas.halpbot.converters.types.ArrayTypeContext;
import nz.pumbas.halpbot.utilities.Reflect;
import nz.pumbas.halpbot.utilities.enums.Priority;
import nz.pumbas.halpbot.converters.annotations.parameter.Children;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import nz.pumbas.halpbot.converters.annotations.parameter.Remaining;
import nz.pumbas.halpbot.converters.annotations.parameter.Unmodifiable;

@Service
@NonCommandParameters(
        types = {
                PersistantUserData.class, GenericEvent.class, Interaction.class, HalpbotCore.class,
                AbstractHalpbotAdapter.class, JDA.class, ApplicationContext.class
        },
        annotations = Source.class
)
@SuppressWarnings({"rawtypes", "unchecked", "ClassWithTooManyFields"})
public final class DefaultConverters
{

    private DefaultConverters() {}

    //region Converters

    //region Simple Converters

    public static final TypeConverter<Byte> BYTE_CONVERTER = TypeConverter.builder(Byte.class)
            .convert(invocationContext ->
                    invocationContext.next(Reflect.getSyntax(Byte.class))
                            .map(Byte::parseByte))
            .optionType(OptionType.INTEGER)
            .build();

    public static final TypeConverter<Short> SHORT_CONVERTER = TypeConverter.builder(Short.class)
            .convert(invocationContext ->
                    invocationContext.next(Reflect.getSyntax(Short.class))
                            .map(Short::parseShort))
            .optionType(OptionType.INTEGER)
            .build();

    public static final TypeConverter<Integer> INTEGER_CONVERTER = TypeConverter.builder(Integer.class)
            .convert(invocationContext ->
                    invocationContext.next(Reflect.getSyntax(Integer.class))
                            .map(Integer::parseInt))
            .optionType(OptionType.INTEGER)
            .build();

    public static final TypeConverter<Long> LONG_CONVERTER = TypeConverter.builder(Long.class)
            .convert(invocationContext ->
                    invocationContext.next(Reflect.getSyntax(Long.class))
                            .map(number -> {
                                if (number.startsWith("-"))
                                    return Long.parseLong(number);
                                else return Long.parseUnsignedLong(number);
                            }))
            .optionType(OptionType.INTEGER)
            .build();

    public static final TypeConverter<Float> FLOAT_CONVERTER = TypeConverter.builder(Float.class)
            .convert(invocationContext ->
                    invocationContext.next(Reflect.getSyntax(Float.class))
                            .map(Float::parseFloat))
            .build();

    public static final TypeConverter<Double> DOUBLE_CONVERTER = TypeConverter.builder(Double.class)
            .convert(invocationContext ->
                    invocationContext.next(Reflect.getSyntax(Double.class))
                            .map(Double::parseDouble))
            .build();

    public static final TypeConverter<Character> CHARACTER_CONVERTER = TypeConverter.builder(Character.class)
            .convert(invocationContext ->
                    invocationContext.next(Reflect.getSyntax(Character.class))
                            .map(in -> in.charAt(0)))
            .optionType(OptionType.STRING)
            .build();

    public static final TypeConverter<String> STRING_CONVERTER = TypeConverter.builder(String.class)
            .convert(InvocationContext::nextSafe)
            .optionType(OptionType.STRING)
            .build();

    public static final TypeConverter<String> REMAINING_STRINGS_CONVERTER = TypeConverter.builder(String.class)
            .annotation(Remaining.class)
            .priority(Priority.EARLY)
            .convert(invocationContext -> Exceptional.of(invocationContext::remaining))
            .build();

    @SuppressWarnings("OverlyComplexBooleanExpression")
    public static final TypeConverter<Boolean> BOOLEAN_CONVERTER = TypeConverter.builder(Boolean.class)
            .convert(invocationContext ->
                    invocationContext.next(Reflect.getSyntax(Boolean.class))
                            .map(in -> {
                                String lowered = in.toLowerCase(Locale.ROOT);
                                return "true".equals(lowered) || "yes".equals(lowered) || "t".equals(lowered)
                                        || "y".equals(lowered) | "1".equals(lowered);
                            }))
            .optionType(OptionType.BOOLEAN)
            .build();

    public static final TypeConverter<Enum> ENUM_CONVERTER = TypeConverter.builder(Enum.class)
            .convert(invocationContext ->
                    Reflect.parseEnumValue(
                                    (TypeContext<Enum<?>>) invocationContext.currentType(),
                                    invocationContext.next())
                            .map(Enum.class::cast))
            .optionType(OptionType.STRING)
            .build();

    @Ignore
    public static final TypeConverter<Object> OBJECT_CONVERTER = TypeConverter.builder(Object.class)
            .priority(Priority.DEFAULT)
            .convert(invocationContext -> {
                TypeContext<?> typeContext = invocationContext.currentType();
                String expectedTypeAlias = CommandManager.getTypeAlias(typeContext.type());
                Exceptional<String> typeAlias = invocationContext.next("[", false);

                if (typeAlias.caught())
                    return Exceptional.of(typeAlias.error());
                if (typeAlias.absent())
                    return Exceptional.of(new CommandException("Missing opening '[' when creating the object " + expectedTypeAlias));

                if (!typeAlias.get().equalsIgnoreCase(expectedTypeAlias))
                    return Exceptional.of(
                            new CommandException("Expected the alias " + expectedTypeAlias + " but got " + typeAlias.get()));

                invocationContext.assertNext('[');
                int currentIndex = invocationContext.currentIndex();
                Exceptional<Object> result = Exceptional.empty();
                //TODO: Refactor to utilise Invokable and HalpbotCommandContext
                for (SimpleCommand simpleCommand : CommandManager.getParsedConstructors(typeContext.type())) {
                    invocationContext.currentIndex(currentIndex);
                    result = simpleCommand.parse(invocationContext, true);
                    if (result.present() && invocationContext.isNext(']', true)) break;

                    else if (!result.caught()) result = Exceptional.of(
                            new CommandException("There seems to have been an error when constructing the object " + expectedTypeAlias));

                }
                return result;
            }).build();

    public static final TypeConverter<Object> CHILDREN_TYPE_CONVERTER = TypeConverter.builder(Object.class)
            .priority(Priority.LAST)
            .annotation(Children.class)
            .convert(invocationContext -> {
                final Exceptional<Object> result = OBJECT_CONVERTER.apply(invocationContext);
                if (!result.caught())
                    return result;
                return invocationContext.annotation(Children.class).flatMap(children -> {
                    // If there are no children then it will return the error in 'result'
                    Exceptional<Object> parsed = result;

                    for (Class<?> child : children.value()) {
                        invocationContext.currentType(TypeContext.of(child));
                        parsed = OBJECT_CONVERTER.apply(invocationContext);
                        if (!parsed.caught()) break;
                    }
                    return parsed;
                });
            }).build();

    //endregion

    //region Collection Converters

    public static final TypeConverter<List> LIST_CONVERTER = TypeConverter.builder(List.class)
            .convert(invocationContext -> {
                //TODO: Test for arrays
                TypeContext<?> genericType = invocationContext.currentType().typeParameters(List.class).get(0);
                Converter<?> elementConverter = invocationContext.applicationContext()
                        .get(ConverterHandler.class)
                        .from(genericType, invocationContext);

                return Exceptional.of(() -> {
                    List<Object> list = new ArrayList<>();
                    invocationContext.assertNext('[');
                    invocationContext.currentType(genericType);

                    while (!invocationContext.isNext(']', true)) {
                        elementConverter.apply(invocationContext)
                                .present(list::add)
                                .rethrow();
                    }
                    return list;
                });
            }).build();

    public static final TypeConverter<List> IMPLICIT_LIST_CONVERTER = TypeConverter.builder(List.class)
            .annotation(Implicit.class)
            .priority(Priority.LAST)
            .convert(invocationContext -> {
                Exceptional<List> listExceptional = LIST_CONVERTER.apply(invocationContext);
                if (listExceptional.errorAbsent() && listExceptional.present())
                    return listExceptional;

                //TODO: Test for arrays
                TypeContext<?> genericType = invocationContext.currentType().typeParameters(List.class).get(0);
                Converter<?> elementConverter = invocationContext.applicationContext()
                        .get(ConverterHandler.class)
                        .from(genericType, invocationContext);

                invocationContext.currentType(genericType);

                List<Object> list = new ArrayList<>();

                Exceptional<?> element = elementConverter.apply(invocationContext)
                        .present(list::add);
                // Expects there to be atleast one element if the arrays being passed implicitly
                if (element.absent()) return Exceptional.of(element.error()); //TODO: More useful error here

                while (invocationContext.hasNext()) {
                    element = elementConverter.apply(invocationContext);
                    if (element.present()) list.add(element.get());
                    else break;
                }

                return Exceptional.of(list);
            }).build();

    public static final TypeConverter<List> UNMODIFIABLE_LIST_CONVERTER = TypeConverter.builder(List.class)
            .annotation(Unmodifiable.class)
            .priority(Priority.EARLY)
            .convert(invocationContext ->
                    invocationContext.applicationContext().get(ConverterHandler.class)
                            .from(List.class, invocationContext)
                            .apply(invocationContext)
                            .map(Collections::unmodifiableList))
            .build();

    public static final TypeConverter<Set> SET_CONVERTER = TypeConverter.builder(Set.class)
            .convert(invocationContext ->
                    invocationContext.applicationContext().get(ConverterHandler.class)
                            .from(List.class, invocationContext)
                            .apply(invocationContext)
                            .map(HashSet::new))
            .build();


    public static final TypeConverter<Set> UNMODIFIABLE_SET_CONVERTER = TypeConverter.builder(Set.class)
            .annotation(Unmodifiable.class)
            .priority(Priority.EARLY)
            .convert(invocationContext ->
                    invocationContext.applicationContext().get(ConverterHandler.class)
                            .from(Set.class, invocationContext)
                            .apply(invocationContext)
                            .map(Collections::unmodifiableSet))
            .build();

    public static final TypeConverter<Object> ARRAY_CONVERTER = TypeConverter.builder(ArrayTypeContext.TYPE)
            .convert(invocationContext ->
                    invocationContext.applicationContext().get(ConverterHandler.class)
                            .from(List.class, invocationContext)
                            .apply(invocationContext)
                            .map(list -> HartshornUtils.toArray(
                                    invocationContext.currentType().elementType().get().type(), list)))
            .build();

    //endregion

    //region Misc

    @SuppressWarnings("ConstantConditions")
    public static final TypeConverter<PersistantUserData> PERSISTANT_USER_DATA_CONVERTER =
            TypeConverter.builder(PersistantUserData.class)
                    .requiresHalpbotEvent(true)
                    .convert(invocationContext -> Exceptional.of(
                            invocationContext.applicationContext().get(HalpbotCore.class).get(AbstractCommandAdapter.class)
                                    .getPersistantUserData(
                                            (TypeContext<? extends PersistantUserData>) invocationContext.currentType(),
                                            invocationContext.halpbotEvent().getUser().getIdLong())
                    ))
                    .build();

    //endregion

    //region JDA Converters

    @SuppressWarnings("ConstantConditions")
    public static final TypeConverter<TextChannel> TEXT_CHANNEL_CONVERTER = TypeConverter.builder(TextChannel.class)
            .requiresHalpbotEvent(true)
            .convert(invocationContext -> invocationContext.nextSurrounded("<#", ">")
                    .map(invocationContext.halpbotEvent().getGuild()::getTextChannelById)
                    .orElse(
                            () -> LONG_CONVERTER.apply(invocationContext)
                                    .map(id -> invocationContext.halpbotEvent().getGuild().getTextChannelById(id))
                                    .orNull()
                    ))
            .optionType(OptionType.CHANNEL)
            .build();

    @SuppressWarnings("ConstantConditions")
    public static final TypeConverter<Member> MEMBER_CONVERTER = TypeConverter.builder(Member.class)
            .requiresHalpbotEvent(true)
            .convert(invocationContext ->
                    invocationContext.nextSurrounded("<@!", ">")
                            .map(id -> invocationContext.halpbotEvent().getGuild().retrieveMemberById(id).complete())
                            .orElse(
                                    () -> LONG_CONVERTER.apply(invocationContext)
                                            .map(id -> invocationContext.halpbotEvent().getGuild().retrieveMemberById(id).complete())
                                            .orNull()
                            ))
            .optionType(OptionType.USER)
            .build();

    @SuppressWarnings("ConstantConditions")
    public static final TypeConverter<User> USER_CONVERTER = TypeConverter.builder(User.class)
            .requiresHalpbotEvent(true)
            .convert(invocationContext ->
                    invocationContext.nextSurrounded("<@!", ">")
                            .map(id -> invocationContext.halpbotEvent().getJDA().retrieveUserById(id).complete())
                            .orElse(
                                    () -> LONG_CONVERTER.apply(invocationContext)
                                            .map(id -> invocationContext.halpbotEvent().getJDA().retrieveUserById(id).complete())
                                            .orNull()
                            ))
            .optionType(OptionType.USER)
            .build();

    //endregion

    //region Source Converters

    @SuppressWarnings("ConstantConditions")
    public static final TypeConverter<GenericEvent> EVENT_CONVERTER = TypeConverter.builder(GenericEvent.class)
            .requiresHalpbotEvent(true)
            .convert(invocationContext -> Exceptional.of(() -> invocationContext.halpbotEvent().getEvent(GenericEvent.class)))
            .build();

    @SuppressWarnings("ConstantConditions")
    public static final TypeConverter<Interaction> INTERACTION_CONVERTER = TypeConverter.builder(Interaction.class)
            .requiresHalpbotEvent(true)
            .convert(invocationContext -> Exceptional.of(() -> invocationContext.halpbotEvent().getEvent(Interaction.class)))
            .build();

    public static final TypeConverter<HalpbotCore> HALPBOT_CORE_CONVERTER = TypeConverter.builder(HalpbotCore.class)
            .convert(invocationContext -> Exceptional.of(invocationContext.applicationContext().get(HalpbotCore.class)))
            .build();

    public static final TypeConverter<HalpbotAdapter> HALPBOT_ADAPTER_CONVERTER =
            TypeConverter.builder(HalpbotAdapter.class)
                    .convert(invocationContext -> invocationContext.applicationContext().get(HalpbotCore.class)
                            .getSafely((TypeContext<HalpbotAdapter>) invocationContext.currentType()))
                    .build();

    @SuppressWarnings("ConstantConditions")
    public static final TypeConverter<JDA> JDA_CONVERTER = TypeConverter.builder(JDA.class)
            .requiresHalpbotEvent(true)
            .convert(invocationContext -> Exceptional.of(invocationContext.halpbotEvent().getJDA()))
            .build();

    public static final TypeConverter<ApplicationContext> APPLICATION_CONTEXT_CONVERTER =
            TypeConverter.builder(ApplicationContext.class)
                    .convert(invocationContext -> Exceptional.of(invocationContext.applicationContext()))
                    .build();

    @SuppressWarnings("ConstantConditions")
    public static final TypeConverter<MessageChannel> SOURCE_MESSAGE_CHANNEL_CONVERTER =
            TypeConverter.builder(MessageChannel.class)
                    .requiresHalpbotEvent(true)
                    .annotation(Source.class)
                    .priority(Priority.FIRST)
                    .convert(invocationContext -> Exceptional.of(invocationContext.halpbotEvent().getMessageChannel()))
                    .build();

    @SuppressWarnings("ConstantConditions")
    public static final TypeConverter<TextChannel> SOURCE_TEXT_CHANNEL_CONVERTER =
            TypeConverter.builder(TextChannel.class)
                    .requiresHalpbotEvent(true)
                    .annotation(Source.class)
                    .priority(Priority.FIRST)
                    .convert(invocationContext -> Exceptional.of(() -> invocationContext.halpbotEvent().getTextChannel()))
                    .build();

    @SuppressWarnings("ConstantConditions")
    public static final TypeConverter<PrivateChannel> SOURCE_PRIVATE_CHANNEL_CONVERTER =
            TypeConverter.builder(PrivateChannel.class)
                    .requiresHalpbotEvent(true)
                    .annotation(Source.class)
                    .priority(Priority.FIRST)
                    .convert(invocationContext -> Exceptional.of(() -> invocationContext.halpbotEvent().getPrivateChannel()))
                    .build();

    @SuppressWarnings("ConstantConditions")
    public static final TypeConverter<User> SOURCE_USER_CONVERTER = TypeConverter.builder(User.class)
            .requiresHalpbotEvent(true)
            .annotation(Source.class)
            .priority(Priority.FIRST)
            .convert(invocationContext -> Exceptional.of(invocationContext.halpbotEvent().getUser()))
            .build();

    @SuppressWarnings("ConstantConditions")
    public static final TypeConverter<Guild> SOURCE_GUILD_CONVERTER = TypeConverter.builder(Guild.class)
            .requiresHalpbotEvent(true)
            .annotation(Source.class)
            .priority(Priority.FIRST)
            .convert(invocationContext -> Exceptional.of(invocationContext.halpbotEvent().getGuild()))
            .build();

    @SuppressWarnings("ConstantConditions")
    public static final TypeConverter<ChannelType> SOURCE_CHANNEL_TYPE_CONVERTER = TypeConverter.builder(ChannelType.class)
            .requiresHalpbotEvent(true)
            .annotation(Source.class)
            .priority(Priority.FIRST)
            .convert(invocationContext -> Exceptional.of(invocationContext.halpbotEvent().getChannelType()))
            .build();

    //endregion

    //endregion
}
