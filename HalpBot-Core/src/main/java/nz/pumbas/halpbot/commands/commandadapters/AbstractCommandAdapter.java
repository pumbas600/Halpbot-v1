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
import net.dv8tion.jda.api.entities.ISnowflake;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.ShutdownEvent;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

import org.dockbox.hartshorn.core.annotations.service.Service;
import org.dockbox.hartshorn.core.context.ApplicationContext;
import org.dockbox.hartshorn.core.domain.Exceptional;
import org.jetbrains.annotations.NotNull;

import java.awt.Color;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import javax.inject.Inject;

import nz.pumbas.halpbot.adapters.HalpbotAdapter;
import nz.pumbas.halpbot.commands.CommandManager;
import nz.pumbas.halpbot.commands.annotations.SlashCommand;
import nz.pumbas.halpbot.events.HalpbotEvent;
import nz.pumbas.halpbot.events.InteractionEvent;
import nz.pumbas.halpbot.events.MessageEvent;
import nz.pumbas.halpbot.commands.exceptions.IllegalPersistantDataConstructor;
import nz.pumbas.halpbot.commands.persistant.AbstractPersistantUserData;
import nz.pumbas.halpbot.commands.persistant.PersistantData;
import nz.pumbas.halpbot.commands.persistant.PersistantUserData;
import nz.pumbas.halpbot.commands.annotations.Description;
import nz.pumbas.halpbot.commands.commandmethods.CommandContext;
import nz.pumbas.halpbot.permissions.HalpbotPermissions;
import nz.pumbas.halpbot.permissions.PermissionManager;
import nz.pumbas.halpbot.utilities.ErrorManager;
import nz.pumbas.halpbot.commands.OnReady;
import nz.pumbas.halpbot.commands.OnShutdown;
import nz.pumbas.halpbot.commands.BuiltInCommands;
import nz.pumbas.halpbot.commands.annotations.Command;
import nz.pumbas.halpbot.commands.exceptions.IllegalCommandException;
import nz.pumbas.halpbot.commands.exceptions.OutputException;
import nz.pumbas.halpbot.commands.tokens.ParsingToken;
import nz.pumbas.halpbot.commands.tokens.PlaceholderToken;
import nz.pumbas.halpbot.commands.tokens.Token;
import nz.pumbas.halpbot.utilities.HalpbotUtils;
import nz.pumbas.halpbot.utilities.Reflect;

@Service
public abstract class AbstractCommandAdapter extends HalpbotAdapter
{
    @Inject
    private ApplicationContext applicationContext;

    protected PermissionManager permissionManager = HalpbotUtils.context().get(PermissionManager.class);

    protected final Map<String, CommandContext> registeredCommands = new HashMap<>();
    protected final Map<String, CommandContext> registeredSlashCommands = new HashMap<>();
    protected final Map<Long, Map<Class<? extends PersistantUserData>, PersistantUserData>> persistantUserData =
        new ConcurrentHashMap<>();

    protected final String commandPrefix;

    protected AbstractCommandAdapter(String commandPrefix) {
        this.commandPrefix = commandPrefix;
        this.registerCommands(new BuiltInCommands());
    }

    /**
     * Registers the {@link CommandContext command methods} contained within the specified {@link Object instances}.
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

    @Override
    public void accept(JDA jda) {
        this.registerSlashCommands(jda);
    }

    @Override
    public void registerObjects(Object... objects) {
        this.registerCommands(objects);
    }

    /**
     * Registers all the slash commands. Note: According to JDA documentation, it can take up to an hour for the
     * slash command to show up in the client.
     *
     * @param jda
     *     The {@link JDA}
     */
    private void registerSlashCommands(JDA jda) {
        for (CommandContext commandMethod : this.registeredSlashCommands.values()) {
            SlashCommand slashCommand = commandMethod.executable().annotation(SlashCommand.class).get();
            if (slashCommand.remove()) {
                this.deleteSlashCommandById(jda, this.formatSlashCommandIdentifiers(commandMethod.aliases()));
            }
            else if (slashCommand.register()) {
                jda.upsertCommand(this.generateSlashCommandData(commandMethod))
                    .queue();
                HalpbotUtils.logger().info("Registered the slash command: " + commandMethod.aliases());
            }
        }
    }

    private void deleteSlashCommandById(JDA jda, String name) {
        jda.retrieveCommands().queue(commands ->
            commands.stream()
                .filter(command -> command.getName().equals(name))
                .findFirst()
                .map(ISnowflake::getId)
                .ifPresent(id ->
                    jda.deleteCommandById(id).queue(success ->
                        HalpbotUtils.logger().info("Successfully deleted slash command: " + name))
                )
        );
    }

    /**
     * Given an {@link CommandContext}, it generates the slash command {@link CommandData} to be used to register it.
     *
     * @param commandMethod
     *      The {@link CommandContext} to generate the command data for
     *
     * @return The generated {@link CommandData}
     */
    public CommandData generateSlashCommandData(CommandContext commandMethod) {
        CommandData data = new CommandData(
            this.formatSlashCommandIdentifiers(commandMethod.aliases()),
            commandMethod.description());

        for (Token token : commandMethod.tokens()) {
            if (token instanceof ParsingToken) {
                ParsingToken parsingToken = (ParsingToken) token;
                if (!parsingToken.isCommandParameter()) continue;

                Description description = parsingToken.annotation(Description.class);
                String optionDescription = null == description ? "N/A" : description.value();
                String name = this.formatSlashCommandIdentifiers(parsingToken.getParameterName());

                data.addOption(parsingToken.converter().getOptionType(), name,
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

    private String formatSlashCommandIdentifiers(String name) {
        return name.replaceAll("([a-z])([A-Z])", "$1-$2")
            .toLowerCase(Locale.ROOT);
    }

    /**
     * Retrieves the {@link CommandContext} that matches the specified command alias.
     *
     * @param commandAlias
     *     The {@link String} command alias of the {@link CommandContext}
     *
     * @return An {@link Optional} containing the {@link CommandContext} if present
     */
    public Optional<CommandContext> getCommandMethod(@NotNull String commandAlias) {
        if (!this.registeredCommands.containsKey(commandAlias))
            return Optional.empty();
        return Optional.of(this.registeredCommands.get(commandAlias));
    }

    /**
     * Listens to {@link MessageReceivedEvent} and calls
     * {@link AbstractCommandAdapter#handleCommandMethodCall(HalpbotEvent, CommandContext, String)}
     * if the message is the invocation of a registered {@link CommandContext}.
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

        HalpbotEvent halpbotEvent = new MessageEvent(event);

        if (this.registeredCommands.containsKey(commandAlias)) {
            CommandContext commandMethod = this.registeredCommands.get(commandAlias);

            try {
                this.handleCommandMethodCall(halpbotEvent, commandMethod, content)
                    .present(value -> this.halpBotCore.getDisplayConfiguration().display(halpbotEvent, value))
                    .caught(exception -> event.getChannel()
                        .sendMessageEmbeds(
                            buildHelpMessage(commandAlias, commandMethod, exception.getMessage()))
                        .queue());

            } catch (OutputException e) {
                ErrorManager.handle(event, e);
            }
        }
        else if (commandAlias.startsWith(this.commandPrefix)) {
            this.halpBotCore.getDisplayConfiguration()
                .displayTemporary(
                    halpbotEvent,
                    "The command **" + commandAlias + "** doesn't seem to exist, you may want to check your spelling",
                    30);
        }
    }

    /**
     * Listens to {@link SlashCommandEvent} and calls
     * {@link AbstractCommandAdapter#handleCommandMethodCall(HalpbotEvent, CommandContext, String)}
     * if the message is the invocation of a registered {@link CommandContext}.
     *
     * @param event
     *     The {@link SlashCommandEvent} that's been received
     */
    @Override
    public void onSlashCommand(@NotNull SlashCommandEvent event) {
        HalpbotEvent halpbotEvent = new InteractionEvent(event);

        if(!this.registeredSlashCommands.containsKey(event.getName())) {
            this.halpBotCore.getDisplayConfiguration()
                .displayTemporary(
                    halpbotEvent,
                    "There doesn't appear to be a command method backing this slash command",
                    30);
            return;
        }

        CommandContext commandMethod = this.registeredSlashCommands.get(event.getName());
        StringBuilder builder = new StringBuilder();
        for (OptionMapping option : event.getOptions()) {
            builder.append(option.getAsString()).append(' ');
        }

        this.handleCommandMethodCall(new InteractionEvent(event), commandMethod, builder.toString())
            .present(value -> this.halpBotCore.getDisplayConfiguration().display(halpbotEvent, value))
            .caught(exception -> {
                ErrorManager.handle(exception);
                event.getChannel()
                    .sendMessageEmbeds(
                        buildHelpMessage(event.getName(), commandMethod, exception.getMessage()))
                    .queue();
            });
    }

    /**
     * Listens to the {@link GuildJoinEvent}. When the bot joins a server, it gives the server owner the
     * {@link HalpbotPermissions#GUILD_OWNER} permission.
     *
     * @param event
     *      The {@link GuildJoinEvent} that's been received
     */
    @Override
    public void onGuildJoin(@NotNull GuildJoinEvent event) {
        HalpbotUtils.logger().info("Joined the guild: " + event.getGuild().getName());
        long ownerId = event.getGuild().getOwnerIdLong();
        if (!this.permissionManager.hasPermissions(ownerId, HalpbotPermissions.GUILD_OWNER)) {
            this.permissionManager.givePermission(ownerId, HalpbotPermissions.GIVE_PERMISSIONS);
        }
    }

    /**
     * Removes the {@link PersistantUserData} from this command adapters.
     *
     * @param commandData
     *      The {@link PersistantUserData} to remove
     */
    public void removePersistantUserData(PersistantUserData commandData) {
        long userId  = commandData.getUserId();

        if (this.persistantUserData.containsKey(userId)) {
            this.persistantUserData.get(userId).remove(commandData.getClass());
            if (this.persistantUserData.get(userId).isEmpty()) {
                this.persistantUserData.remove(userId);
            }
        }
    }

    /**
     * Retrieves the {@link PersistantData} of the specified type for the user. If there is currently no registered
     * data for that user then a new {@link PersistantData} will be automatically created and returned. If the newly
     * created persistant data extends {@link AbstractPersistantUserData} then it will also automatically call
     * {@link AbstractPersistantUserData#setCommandAdapter(AbstractCommandAdapter)} to set itself as the registering
     * data holder.
     *
     * @param type
     *      The {@link Class} of the persistant data
     * @param userId
     *      The id of the user to retrieve the data for
     * @param <T>
     *      The type of the persistant data
     *
     * @return The persistant user data for the specified user
     * @throws IllegalPersistantDataConstructor
     *         If the constructor for the persistant user data type does not accept only a single {@link Long user id}.
     */
    @NotNull
    @SuppressWarnings("unchecked")
    public <T extends PersistantUserData> T getPersistantUserData(Class<T> type, long userId) {
        Map<Class<? extends PersistantUserData>, PersistantUserData> mappings;
        if (!this.persistantUserData.containsKey(userId)) {
            mappings = new ConcurrentHashMap<>();
            this.persistantUserData.put(userId, mappings);
        }
        else mappings = this.persistantUserData.get(userId);


        if (!mappings.containsKey(type)) {
            T userData = Reflect.createInstance(type, userId);
            if (null == userData)
                throw new IllegalPersistantDataConstructor(
                    "Persistant user data can only have a user id as a parameter for its constructor");

            if (userData instanceof AbstractPersistantUserData)
                ((AbstractPersistantUserData) userData).setCommandAdapter(this);

            mappings.put(type, userData);
            return userData;
        }
        T userData = (T) mappings.get(type);
        userData.setAlreadyExisted();
        return userData;
    }

    /**
     * Builds the help {@link MessageEmbed} describing the {@link CommandContext}.
     *
     * @param commandAlias
     *     The {@link String} alias for this command
     * @param commandMethod
     *     The {@link CommandContext} which correlates to this command
     * @param message
     *     Any message that you want to be included in the help embed
     *
     * @return The {@link MessageEmbed} for the help message
     */
    public static MessageEmbed buildHelpMessage(@NotNull String commandAlias, @NotNull CommandContext commandMethod,
                                                @NotNull String message) {
        return new EmbedBuilder()
            .setColor(Color.cyan)
            .setTitle("HALP")
            .addField(commandAlias, message, false)
            .addField("Usage", commandAlias + " " + commandMethod.usage(), true)
            .addField("Description", commandMethod.description(), true)
            .build();
    }

    /**
     * Registers the {@link Method methods} and generates {@link CommandContext command methods} for each one.
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

            String commandAlias = CommandManager.getCommandAlias(command, method);
            if (this.registeredCommands.containsKey(commandAlias))
                throw new IllegalCommandException(
                    String.format("The alias %s has already been defined and so it can't be used by the method %s",
                        commandAlias, method.getName()));

            CommandContext commandContext = this.createCommandMethod(instance, method, command);
            if (method.isAnnotationPresent(SlashCommand.class) ||
                instance.getClass().isAnnotationPresent(SlashCommand.class))
                    this.registeredSlashCommands.put(commandAlias, commandContext);
            else
                this.registeredCommands.put(this.getCommandPrefix() + commandAlias, commandContext);
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
        HalpbotUtils.setJDA(event.getJDA());

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
     * @return A {@link Set} containing the unique {@link Object instances} for the registered {@link CommandContext
     *     command methods}
     */
    private Set<Object> getUniqueCommandMethodInstances() {
        return this.registeredCommands.values()
            .stream()
            .map(CommandContext::instance)
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
     * @return The registered {@link CommandContext command methods} in an unmodifiable {@link Map}
     */
    public Map<String, CommandContext> getRegisteredCommands() {
        return Collections.unmodifiableMap(this.registeredCommands);
    }

    /**
     * Creates a {@link CommandContext} from the specified information.
     *
     * @param instance
     *     The {@link Object} that the {@link Method} belongs to
     * @param method
     *     The {@link Method} for this {@link CommandContext}
     * @param command
     *     The {@link Command} which annotates the {@link Method}
     *
     * @return The created {@link CommandContext}
     */
    protected abstract CommandContext createCommandMethod(@NotNull Object instance,
                                                          @NotNull Method method,
                                                          @NotNull Command command);

    /**
     * Handles the invocation, matching and parsing of a {@link CommandContext} with the given {@link String content}.
     *
     * @param event
     *     The {@link MessageReceivedEvent} which invoked the {@link CommandContext}
     * @param commandContext
     *     The {@link CommandContext} which matches to the command alias that was invoked
     * @param content
     *     The rest of the {@link String} after the {@link String command alias} or null if there was nothing else
     *
     * @return The parsed method call as an {@link Exceptional}
     * @throws OutputException
     *     Any {@link OutputException} thrown by the {@link CommandContext} when it was invoked
     */
    protected abstract Exceptional<Object> handleCommandMethodCall(@NotNull HalpbotEvent event,
                                                                   @NotNull CommandContext commandContext,
                                                                   @NotNull String content) throws OutputException;
}