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

package nz.pumbas.halpbot.commands.commandadapters;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.ShutdownEvent;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.events.message.GenericMessageEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.Interaction;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.Color;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import nz.pumbas.halpbot.commands.annotations.Description;
import nz.pumbas.halpbot.commands.commandmethods.CommandMethod;
import nz.pumbas.halpbot.commands.CommandType;
import nz.pumbas.halpbot.commands.DiscordString;
import nz.pumbas.halpbot.utilities.ErrorManager;
import nz.pumbas.halpbot.commands.OnReady;
import nz.pumbas.halpbot.commands.OnShutdown;
import nz.pumbas.halpbot.BuiltInCommands;
import nz.pumbas.halpbot.commands.annotations.Command;
import nz.pumbas.halpbot.commands.exceptions.IllegalCommandException;
import nz.pumbas.halpbot.commands.exceptions.OutputException;
import nz.pumbas.halpbot.commands.tokens.ParsingToken;
import nz.pumbas.halpbot.commands.tokens.PlaceholderToken;
import nz.pumbas.halpbot.commands.tokens.Token;
import nz.pumbas.halpbot.objects.Exceptional;
import nz.pumbas.halpbot.utilities.Reflect;

public abstract class AbstractCommandAdapter extends ListenerAdapter
{
    protected final Map<String, CommandMethod> registeredCommands = new HashMap<>();
    protected final Map<String, CommandMethod> registeredSlashCommands = new HashMap<>();
    protected final Map<Long, Map<String, Consumer<MessageReactionAddEvent>>> reactionCallbacks = new HashMap<>();

    protected final String commandPrefix;

    protected AbstractCommandAdapter(@Nullable JDABuilder builder, String commandPrefix) {
        builder.addEventListeners(this);
        this.commandPrefix = commandPrefix;
        this.registerCommands(new BuiltInCommands());
    }

    /**
     * Registers the {@link CommandMethod command methods} contained within the specified {@link Object instances}.
     *
     * @param instances
     *     The {@link Object objects} to check for {@link Command commands}
     *
     * @return {@link AbstractCommandAdapter Itself}
     */
    public AbstractCommandAdapter registerCommands(@NotNull Object... instances) {
        for (Object instance : instances) {
            this.registerCommandMethods(instance, Reflect.getAnnotatedMethods(
                instance.getClass(), Command.class, false));
        }
        return this;
    }

    /**
     * Registers all the slash commands. Note: According to JDA documentation, it can take up to an hour for the
     * slash command to show up in the client.
     *
     * @param jda
     *     The {@link JDA}
     */
    public void registerSlashCommands(JDA jda) {
        for (CommandMethod commandMethod : this.registeredSlashCommands.values()) {
            jda.upsertCommand(
                this.generateSlashCommandData(commandMethod))
                .queue();
        }
    }

    public CommandData generateSlashCommandData(CommandMethod commandMethod) {
        CommandData data = new CommandData(commandMethod.getAlias(), commandMethod.getDescription());
        for (Token token : commandMethod.getTokens()) {
            if (token instanceof ParsingToken) {
                ParsingToken parsingToken = (ParsingToken) token;
                if (!parsingToken.isCommandParameter()) continue;

                Description description = parsingToken.getAnnotation(Description.class);
                String optionDescription = null == description ? "N/A" : description.value();

                data.addOption(parsingToken.getConverter().getOptionType(), parsingToken.getParameterName(),
                    optionDescription, !token.isOptional());
            }
            else if (token instanceof PlaceholderToken){
                PlaceholderToken placeholderToken = (PlaceholderToken) token;
                data.addOption(OptionType.STRING, placeholderToken.getPlaceHolder(),
                    placeholderToken.getPlaceHolder(), !token.isOptional());
            }
        }
        return data;
    }

    /**
     * Retrieves the {@link CommandMethod} that matches the specified command alias.
     *
     * @param commandAlias
     *     The {@link String} command alias of the {@link CommandMethod}
     *
     * @return An {@link Optional} containing the {@link CommandMethod} if present
     */
    public Optional<CommandMethod> getCommandMethod(@NotNull String commandAlias) {
        if (!this.registeredCommands.containsKey(commandAlias))
            return Optional.empty();
        return Optional.of(this.registeredCommands.get(commandAlias));
    }

    /**
     * Listens to {@link MessageReceivedEvent} and calls
     * {@link AbstractCommandAdapter#handleCommandMethodCall(MessageReceivedEvent, CommandMethod, String)}
     * if the message is the invocation of a registered {@link CommandMethod}.
     *
     * @param event
     *     The {@link MessageReceivedEvent} that's been received
     */
    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        if (event.getAuthor().isBot()) return;

        // Remove all the additional whitespace
        String message = event.getMessage().getContentRaw().replaceAll("\\s+", " ");

        String[] splitText = message.split(" ", 2);
        String commandAlias = splitText[0].toLowerCase();
        String content = 2 == splitText.length ? splitText[1] : "";

        if (this.registeredCommands.containsKey(commandAlias)) {
            CommandMethod commandMethod = this.registeredCommands.get(commandAlias);

            try {
                this.handleCommandMethodCall(event, commandMethod, content)
                    .present(value -> this.displayCommandMethodResult(event, value))
                    .caught(exception -> event.getChannel()
                        .sendMessageEmbeds(
                            buildHelpMessage(commandAlias, commandMethod, exception.getMessage()))
                        .queue());

            } catch (OutputException e) {
                ErrorManager.handle(event, e);
            }
        }
    }

    /**
     * Listens to {@link SlashCommandEvent} and calls
     * {@link AbstractCommandAdapter#handleCommandMethodCall(MessageReceivedEvent, CommandMethod, String)}
     * if the message is the invocation of a registered {@link CommandMethod}.
     *
     * @param event
     *     The {@link SlashCommandEvent} that's been received
     */
    @Override
    public void onSlashCommand(@NotNull SlashCommandEvent event) {
        if(!this.registeredSlashCommands.containsKey(event.getName())) {
            this.displayCommandMethodResult(event,
                "There doesn't appear to be a command method backing this slash command");
            return;
        }

        CommandMethod commandMethod = this.registeredSlashCommands.get(event.getName());
        StringBuilder builder = new StringBuilder();
        for (OptionMapping option : event.getOptions()) {
            builder.append(option.getAsString()).append(' ');
        }

        //TODO: THIS
//        this.handleCommandMethodCall(event, commandMethod, builder.toString())
//            .present(value -> this.displayCommandMethodResult(event, value))
//            .caught(exception -> event.getChannel()
//                .sendMessageEmbeds(
//                    buildHelpMessage(event.getName(), commandMethod, exception.getMessage()))
//                .queue());

        this.displayCommandMethodResult(event, event.getOptions());
    }

    @Override
    public void onMessageReactionAdd(@NotNull MessageReactionAddEvent event) {
        if (event.getUser().isBot() || !this.reactionCallbacks.containsKey(event.getMessageIdLong()))
            return;

        Map<String, Consumer<MessageReactionAddEvent>> callbacks = this.reactionCallbacks.get(event.getMessageIdLong());
        if (!callbacks.containsKey(event.getReactionEmote().getAsReactionCode()))
            return;

        callbacks.get(event.getReactionEmote().getAsReactionCode())
            .accept(event);
    }

    public void addReactionCallback(Message message, String emoji, Consumer<MessageReactionAddEvent> consumer) {
        if (!this.reactionCallbacks.containsKey(message.getIdLong())) {
            Map<String, Consumer<MessageReactionAddEvent>> hashmap = new HashMap<>();
            hashmap.put(emoji, consumer);
            this.reactionCallbacks.put(message.getIdLong(), hashmap);
        }
        else {
            this.reactionCallbacks.get(message.getIdLong()).put(emoji, consumer);
        }
        message.addReaction(emoji).queue();
    }

    /**
     * Builds the help {@link MessageEmbed} describing the {@link CommandMethod}.
     *
     * @param commandAlias
     *     The {@link String} alias for this command
     * @param commandMethod
     *     The {@link CommandMethod} which correlates to this command
     * @param message
     *     Any message that you want to be included in the help embed
     *
     * @return The {@link MessageEmbed} for the help message
     */
    public static MessageEmbed buildHelpMessage(@NotNull String commandAlias, @NotNull CommandMethod commandMethod,
                                                @NotNull String message) {
        return new EmbedBuilder()
            .setColor(Color.cyan)
            .setTitle("HALP")
            .addField(commandAlias, message, false)
            .addField("Usage", commandAlias + " " + commandMethod.getUsage(), true)
            .addField("Description", commandMethod.getDescription(), true)
            .build();
    }

    /**
     * Registers the {@link Method methods} and generates {@link CommandMethod command methods} for each one.
     *
     * @param instance
     *     The {@link Object instance} that the {@link Method methods} belong to
     * @param annotatedMethods
     *     The {@link Object instance's} {@link Method methods} which are annotated with {@link Command}
     */
    protected void registerCommandMethods(@NotNull Object instance, @NotNull List<Method> annotatedMethods) {
        for (Method method : annotatedMethods) {
            method.setAccessible(true);
            Command command = method.getAnnotation(Command.class);

            String commandAlias = this.getCommandPrefix() + command.alias().toLowerCase();
            if (this.registeredCommands.containsKey(commandAlias))
                throw new IllegalCommandException(
                    String.format("The alias %s has already been defined and so it can't be used by the method %s",
                        commandAlias, method.getName()));

            CommandMethod commandMethod = this.createCommandMethod(instance, method, command);
            if (CommandType.MESSAGE == command.commandType())
                this.registeredCommands.put(commandAlias, commandMethod);
            else
                this.registeredSlashCommands.put(commandAlias, commandMethod);
        }
    }

    /**
     * Displays the result of a {@link CommandMethod}. If this is a {@link MessageEmbed}, it will be automatically
     * cast and sent as such.
     *
     * @param event
     *     The {@link GenericEvent} event that was sent
     * @param result
     *     The {@link Object} that was returned by the {@link CommandMethod}
     */
    protected void displayCommandMethodResult(@NotNull GenericEvent event, @NotNull Object result) {
        if (result instanceof MessageEmbed) {
            if (event instanceof GenericMessageEvent)
                ((GenericMessageEvent) event).getChannel().sendMessageEmbeds((MessageEmbed) result).queue();
            else if (event instanceof Interaction)
                ((Interaction) event).replyEmbeds((MessageEmbed) result).queue();
        }
        else {
            String message = result instanceof DiscordString
                ? ((DiscordString) result).toDiscordString() : result.toString();
            if (event instanceof GenericMessageEvent)
                ((GenericMessageEvent) event).getChannel().sendMessage(message).queue();
            else if (event instanceof Interaction)
                ((Interaction) event).reply(message).queue();
        }
    }

    /**
     * Passes the {@link ReadyEvent} to all the registered instances of {@link OnReady}.
     *
     * @param event
     *     The JDA {@link ReadyEvent}
     */
    @Override
    public void onReady(@NotNull ReadyEvent event) {
        this.getUniqueCommandMethodInstances()
            .stream()
            .filter(o -> o instanceof OnReady)
            .forEach(o -> ((OnReady) o).onReady(event));
    }

    /**
     * Passes the {@link ShutdownEvent} to all the registered instances of {@link OnShutdown}.
     *
     * @param event
     *     The JDA {@link ShutdownEvent}
     */
    @Override
    public void onShutdown(@NotNull ShutdownEvent event) {
        this.getUniqueCommandMethodInstances()
            .stream()
            .filter(o -> o instanceof OnShutdown)
            .forEach(o -> ((OnShutdown) o).onShutDown(event));
    }

    /**
     * @return A {@link Set} containing the unique {@link Object instances} for the registered {@link CommandMethod
     *     command methods}
     */
    private Set<Object> getUniqueCommandMethodInstances() {
        return this.registeredCommands.values()
            .stream()
            .map(CommandMethod::getInstance)
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());
    }

    /**
     * @return The {@link String command prefix} for this command adapter
     */
    public String getCommandPrefix() {
        return this.commandPrefix;
    }

    /**
     * @return The registered {@link CommandMethod command methods} in an unmodifiable {@link Map}
     */
    public Map<String, CommandMethod> getRegisteredCommands() {
        return Collections.unmodifiableMap(this.registeredCommands);
    }

    /**
     * Creates a {@link CommandMethod} from the specified information.
     *
     * @param instance
     *     The {@link Object} that the {@link Method} belongs to
     * @param method
     *     The {@link Method} for this {@link CommandMethod}
     * @param command
     *     The {@link Command} which annotates the {@link Method}
     *
     * @return The created {@link CommandMethod}
     */
    protected abstract CommandMethod createCommandMethod(@NotNull Object instance,
                                                         @NotNull Method method,
                                                         @NotNull Command command);

    /**
     * Handles the invocation, matching and parsing of a {@link CommandMethod} with the given {@link String content}.
     *
     * @param event
     *     The {@link MessageReceivedEvent} which invoked the {@link CommandMethod}
     * @param commandMethod
     *     The {@link CommandMethod} which matches to the command alias that was invoked
     * @param content
     *     The rest of the {@link String} after the {@link String command alias} or null if there was nothing else
     *
     * @return The parsed method call as an {@link Exceptional}
     * @throws OutputException
     *     Any {@link OutputException} thrown by the {@link CommandMethod} when it was invoked
     */
    protected abstract Exceptional<Object> handleCommandMethodCall(@NotNull MessageReceivedEvent event,
                                                                   @NotNull CommandMethod commandMethod,
                                                                   @NotNull String content) throws OutputException;
}