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
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.interactions.Interaction;
import net.dv8tion.jda.api.interactions.commands.OptionType;

import org.dockbox.hartshorn.core.annotations.stereotype.Service;
import org.dockbox.hartshorn.core.context.ApplicationContext;
import org.dockbox.hartshorn.core.context.element.TypeContext;
import org.dockbox.hartshorn.core.domain.Exceptional;

import nz.pumbas.halpbot.adapters.HalpbotAdapter;
import nz.pumbas.halpbot.HalpbotCore;
import nz.pumbas.halpbot.commands.CommandAdapter;
import nz.pumbas.halpbot.commands.actioninvokable.context.CommandInvocationContext;
import nz.pumbas.halpbot.commands.actioninvokable.context.constructor.CustomConstructorContext;
import nz.pumbas.halpbot.converters.annotations.Ignore;
import nz.pumbas.halpbot.converters.annotations.parameter.Source;
import nz.pumbas.halpbot.commands.exceptions.CommandException;
import nz.pumbas.halpbot.converters.annotations.parameter.Implicit;
import nz.pumbas.halpbot.converters.types.ArrayTypeContext;
import nz.pumbas.halpbot.utilities.Reflect;
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
@SuppressWarnings({"rawtypes", "unchecked", "ClassWithTooManyFields", "NonFinalUtilityClass"})
public class DefaultConverters
{
    //region Converters

    //region Simple Converters

    public static final TypeConverter<Byte> BYTE_CONVERTER = TypeConverter.builder(Byte.class)
            .convert(invocationContext -> invocationContext.next(Reflect.getSyntax(Byte.class))
                    .map(Byte::parseByte))
            .optionType(OptionType.INTEGER)
            .build();

    public static final TypeConverter<Short> SHORT_CONVERTER = TypeConverter.builder(Short.class)
            .convert(invocationContext -> invocationContext.next(Reflect.getSyntax(Short.class))
                    .map(Short::parseShort))
            .optionType(OptionType.INTEGER)
            .build();

    public static final TypeConverter<Integer> INTEGER_CONVERTER = TypeConverter.builder(Integer.class)
            .convert(invocationContext -> invocationContext.next(Reflect.getSyntax(Integer.class))
                    .map(Integer::parseInt))
            .optionType(OptionType.INTEGER)
            .build();

    public static final TypeConverter<Long> LONG_CONVERTER = TypeConverter.builder(Long.class)
            .convert(invocationContext -> invocationContext.next(Reflect.getSyntax(Long.class))
                    .map(number -> {
                        if (number.startsWith("-"))
                            return Long.parseLong(number);
                        else return Long.parseUnsignedLong(number);
                    }))
            .optionType(OptionType.INTEGER)
            .build();

    public static final TypeConverter<Float> FLOAT_CONVERTER = TypeConverter.builder(Float.class)
            .convert(invocationContext -> invocationContext.next(Reflect.getSyntax(Float.class))
                    .map(Float::parseFloat))
            .build();

    public static final TypeConverter<Double> DOUBLE_CONVERTER = TypeConverter.builder(Double.class)
            .convert(invocationContext -> invocationContext.next(Reflect.getSyntax(Double.class))
                    .map(Double::parseDouble))
            .build();

    public static final TypeConverter<Character> CHARACTER_CONVERTER = TypeConverter.builder(Character.class)
            .convert(invocationContext ->
                    invocationContext.next(Reflect.getSyntax(Character.class))
                            .map(in -> in.charAt(0)))
            .optionType(OptionType.STRING)
            .build();

    public static final TypeConverter<String> STRING_CONVERTER = TypeConverter.builder(String.class)
            .convert(invocationContext -> invocationContext.next(Reflect.getSyntax(String.class)))
            .optionType(OptionType.STRING)
            .build();

    public static final TypeConverter<String> REMAINING_STRINGS_CONVERTER = TypeConverter.builder(String.class)
            .annotation(Remaining.class)
            .convert(invocationContext -> Exceptional.of(invocationContext::remaining))
            .build();

    public static final TypeConverter<Boolean> BOOLEAN_CONVERTER = TypeConverter.builder(Boolean.class)
            .convert(invocationContext -> invocationContext.next(Reflect.getSyntax(Boolean.class))
                    .map(in -> {
                        String lowered = in.toLowerCase(Locale.ROOT);
                        return "true".equals(lowered) || "yes".equals(lowered) || "t".equals(lowered)
                                || "y".equals(lowered) || "1".equals(lowered);
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
            .convert(invocationContext -> {
                CommandAdapter commandAdapter = invocationContext.applicationContext().get(CommandAdapter.class);
                TypeContext<?> typeContext = invocationContext.currentType();
                String expectedTypeAlias = commandAdapter.typeAlias(typeContext);
                Exceptional<String> typeAlias = invocationContext.next("(", true);

                if (typeAlias.caught())
                    return Exceptional.of(typeAlias.error());
                if (typeAlias.absent())
                    return Exceptional.of(new CommandException("Missing opening '(' when creating the object " + expectedTypeAlias));

                if (!typeAlias.get().equalsIgnoreCase(expectedTypeAlias))
                    return Exceptional.of(
                            new CommandException("Expected the alias '%s' but got '%s'"
                                    .formatted(expectedTypeAlias, typeAlias.get())));

                int currentIndex = invocationContext.currentIndex();
                Exceptional<Object> result = Exceptional.empty();

                invocationContext.canHaveContextLeft(true);
                for (CustomConstructorContext constructorContext : commandAdapter.customConstructors(typeContext)) {
                    invocationContext.currentIndex(currentIndex);
                    result = constructorContext.invoke(invocationContext);
                    if (result.present() && invocationContext.isNext(')', true)) break;

                    else if (!result.caught()) result = Exceptional.of(
                            new CommandException("There seems to have been an error when constructing the object " + expectedTypeAlias));
                }
                return result;
            }).build();

    public static final TypeConverter<Object> CHILDREN_TYPE_CONVERTER = TypeConverter.builder(Object.class)
            .annotation(Children.class)
            .convert(invocationContext -> {
                final Exceptional<Object> result = OBJECT_CONVERTER.apply(invocationContext);
                if (!result.caught())
                    return result;
                return invocationContext.annotation(Children.class).flatMap(children -> {
                    // If there are no children then it will return the error in 'result'
                    Exceptional<Object> parsed = result;

                    invocationContext.canHaveContextLeft(true);
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
                TypeContext<?> genericType = invocationContext.currentType().isArray()
                        ? invocationContext.currentType().elementType().get()
                        : invocationContext.currentType().typeParameters().get(0);
                Converter<CommandInvocationContext, ?> elementConverter = invocationContext.applicationContext()
                        .get(ConverterHandler.class)
                        .from(genericType, invocationContext);

                return Exceptional.of(() -> {
                    List<Object> list = new ArrayList<>();
                    invocationContext.assertNext('[');
                    invocationContext.currentType(genericType);

                    while (!invocationContext.isNext(']', true)) {
                        elementConverter.apply(invocationContext)
                                .present(list::add)
                                .rethrowUnchecked();
                    }
                    return list;
                });
            }).build();

    public static final TypeConverter<List> IMPLICIT_LIST_CONVERTER = TypeConverter.builder(List.class)
            .annotation(Implicit.class)
            .convert(invocationContext -> {
                Exceptional<List> listExceptional = LIST_CONVERTER.apply(invocationContext);
                if (listExceptional.errorAbsent() && listExceptional.present())
                    return listExceptional;

                TypeContext<?> genericType = invocationContext.currentType().isArray()
                        ? invocationContext.currentType().elementType().get()
                        : invocationContext.currentType().typeParameters().get(0);
                Converter<CommandInvocationContext, ?> elementConverter = invocationContext.applicationContext()
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
                            .map(list -> Reflect.toArray(
                                    invocationContext.currentType().elementType().get().type(), list)))
            .build();

    //endregion

    //region Misc

//    @SuppressWarnings("ConstantConditions")
//    public static final SourceConverter<PersistantUserData> PERSISTANT_USER_DATA_CONVERTER =
//            SourceConverter.builder(PersistantUserData.class)
//                    .requiresHalpbotEvent(true)
//                    .convert(invocationContext -> Exceptional.of(
//                            invocationContext.applicationContext().get(HalpbotCore.class).get(AbstractCommandAdapter.class)
//                                    .getPersistantUserData(
//                                            (TypeContext<? extends PersistantUserData>) invocationContext.currentType(),
//                                            invocationContext.halpbotEvent().getUser().getIdLong())
//                    ))
//                    .build();

    //endregion

    //region JDA Converters

    public static final TypeConverter<TextChannel> TEXT_CHANNEL_CONVERTER = TypeConverter.builder(TextChannel.class)
            .convert(invocationContext -> {
                final Guild guild = invocationContext.halpbotEvent().guild();
                if (guild == null)
                    return Exceptional.of(
                            new UnsupportedOperationException("You can't specify a text channel in a private message"));
                Exceptional<TextChannel> textChannel = invocationContext.nextSurrounded("<#", ">")
                        .map(guild::getTextChannelById);
                if (textChannel.caught())
                    textChannel = LONG_CONVERTER.apply(invocationContext)
                                .map(guild::getTextChannelById);
                return textChannel;
            })
            .optionType(OptionType.CHANNEL)
            .build();

    public static final TypeConverter<Role> ROLE_CONVERTER = TypeConverter.builder(Role.class)
            .convert(invocationContext -> {
                final Guild guild = invocationContext.halpbotEvent().guild();
                if (guild == null)
                    return Exceptional.of(
                            new UnsupportedOperationException("You can't specify a role in a private message"));
                Exceptional<Role> role = invocationContext.nextSurrounded("<@&", ">")
                        .map(guild::getRoleById);
                if (role.caught())
                    role = LONG_CONVERTER.apply(invocationContext)
                            .map(guild::getRoleById);
                return role;
            })
            .optionType(OptionType.CHANNEL)
            .build();

    public static final TypeConverter<Member> MEMBER_CONVERTER = TypeConverter.builder(Member.class)
            .convert(invocationContext -> {
                final Guild guild = invocationContext.halpbotEvent().guild();
                if (guild == null)
                    return Exceptional.of(
                            new UnsupportedOperationException("You can't specify a member in a private message"));
                Exceptional<Member> member = invocationContext.nextSurrounded("<@!", ">")
                        .map(id -> guild.retrieveMemberById(id).complete());
                if (member.caught())
                    member = LONG_CONVERTER.apply(invocationContext)
                            .map(id -> guild.retrieveMemberById(id).complete());
                return member;
            })
            .optionType(OptionType.USER)
            .build();

    public static final TypeConverter<User> USER_CONVERTER = TypeConverter.builder(User.class)
            .convert(invocationContext -> {
                Exceptional<User> user = invocationContext.nextSurrounded("<@!", ">")
                        .map(id -> invocationContext.halpbotEvent().jda().retrieveUserById(id).complete());
                if (user.caught())
                    user = invocationContext.nextSurrounded("<@", ">")
                            .map(id -> invocationContext.halpbotEvent().jda().retrieveUserById(id).complete());
                if (user.caught())
                    user = LONG_CONVERTER.apply(invocationContext)
                            .map(id -> invocationContext.halpbotEvent().jda().retrieveUserById(id).complete());
                return user;
            })
            .optionType(OptionType.USER)
            .build();

    //endregion

    //region Source Converters

    public static final SourceConverter<GenericEvent> EVENT_CONVERTER = SourceConverter.builder(GenericEvent.class)
            .convert(invocationContext -> Exceptional.of(() -> invocationContext.halpbotEvent().event(GenericEvent.class)))
            .build();

    public static final SourceConverter<Interaction> INTERACTION_CONVERTER = SourceConverter.builder(Interaction.class)
            .convert(invocationContext -> Exceptional.of(() -> invocationContext.halpbotEvent().event(Interaction.class)))
            .build();

    public static final SourceConverter<HalpbotCore> HALPBOT_CORE_CONVERTER = SourceConverter.builder(HalpbotCore.class)
            .convert(invocationContext -> Exceptional.of(invocationContext.applicationContext().get(HalpbotCore.class)))
            .build();

    public static final SourceConverter<HalpbotAdapter> HALPBOT_ADAPTER_CONVERTER =
            SourceConverter.builder(HalpbotAdapter.class)
                    .convert(invocationContext -> Exceptional.of(
                            (HalpbotAdapter) invocationContext.applicationContext()
                                    .get(invocationContext.currentType())))
                    .build();
    public static final SourceConverter<JDA> JDA_CONVERTER = SourceConverter.builder(JDA.class)
            .convert(invocationContext -> Exceptional.of(invocationContext.halpbotEvent().jda()))
            .build();

    public static final SourceConverter<ApplicationContext> APPLICATION_CONTEXT_CONVERTER =
            SourceConverter.builder(ApplicationContext.class)
                    .convert(invocationContext -> Exceptional.of(invocationContext.applicationContext()))
                    .build();

    public static final SourceConverter<MessageChannel> SOURCE_MESSAGE_CHANNEL_CONVERTER =
            SourceConverter.builder(MessageChannel.class)
                    .annotation(Source.class)
                    .convert(invocationContext -> Exceptional.of(invocationContext.halpbotEvent().messageChannel()))
                    .build();

    public static final SourceConverter<TextChannel> SOURCE_TEXT_CHANNEL_CONVERTER =
            SourceConverter.builder(TextChannel.class)
                    .annotation(Source.class)
                    .convert(invocationContext -> Exceptional.of(() -> invocationContext.halpbotEvent().textChannel()))
                    .build();

    public static final SourceConverter<PrivateChannel> SOURCE_PRIVATE_CHANNEL_CONVERTER =
            SourceConverter.builder(PrivateChannel.class)
                    .annotation(Source.class)
                    .convert(invocationContext -> Exceptional.of(() -> invocationContext.halpbotEvent().privateChannel()))
                    .build();

    public static final SourceConverter<User> SOURCE_USER_CONVERTER = SourceConverter.builder(User.class)
            .annotation(Source.class)
            .convert(invocationContext -> Exceptional.of(invocationContext.halpbotEvent().user()))
            .build();

    public static final SourceConverter<Member> SOURCE_MEMBER_CONVERTER = SourceConverter.builder(Member.class)
            .annotation(Source.class)
            .convert(invocationContext -> Exceptional.of(invocationContext.halpbotEvent().member()))
            .build();


    public static final SourceConverter<Guild> SOURCE_GUILD_CONVERTER = SourceConverter.builder(Guild.class)
            .annotation(Source.class)
            .convert(invocationContext -> Exceptional.of(invocationContext.halpbotEvent().guild()))
            .build();

    public static final SourceConverter<ChannelType> SOURCE_CHANNEL_TYPE_CONVERTER = SourceConverter.builder(ChannelType.class)
            .annotation(Source.class)
            .convert(invocationContext -> Exceptional.of(invocationContext.halpbotEvent().channelType()))
            .build();

    //endregion

    //endregion
}
