package nz.pumbas.commands.commandadapters;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.Color;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import nz.pumbas.commands.CommandManager;
import nz.pumbas.commands.CommandMethod;
import nz.pumbas.commands.ErrorManager;
import nz.pumbas.commands.annotations.Command;
import nz.pumbas.commands.annotations.CommandGroup;
import nz.pumbas.commands.exceptions.IllegalCommandException;
import nz.pumbas.commands.exceptions.OutputException;
import nz.pumbas.utilities.Reflect;

public abstract class AbstractCommandAdapter extends ListenerAdapter
{
    /**
     * A map of the registered {@link String command aliases} and their respective {@link CommandMethod}.
     */
    protected final Map<String, CommandMethod> registeredCommands = new HashMap<>();

    protected AbstractCommandAdapter(@Nullable JDABuilder builder)
    {
        builder.addEventListeners(this);
    }

    /**
     * Registers the {@link CommandMethod command methods} contained within the specified {@link Object instances}.
     *
     * @param instances
     *      The {@link Object objects} to check for {@link Command commands}
     *
     * @return {@link AbstractCommandAdapter Itself}
     */
    public AbstractCommandAdapter registerCommands(@NotNull Object... instances)
    {
        for (Object instance : instances) {
            this.registerCommandMethods(instance, Reflect.getAnnotatedMethods(
                instance.getClass(), Command.class, false));
        }
        return this;
    }

    /**
     * Retrieves the {@link CommandMethod} that matches the specified command alias.
     *
     * @param commandAlias
     *      The {@link String} command alias of the {@link CommandMethod}
     *
     * @return An {@link Optional} containing the {@link CommandMethod} if present
     */
    public Optional<CommandMethod> getCommandMethod(@NotNull String commandAlias)
    {
        if (!this.registeredCommands.containsKey(commandAlias))
            return Optional.empty();
        return Optional.of(this.registeredCommands.get(commandAlias));
    }

    /**
     * Listens to {@link MessageReceivedEvent} and calls {@link AbstractCommandAdapter#handleCommandMethodCall(MessageReceivedEvent, CommandMethod, String)}
     * if the message is the invocation of a registered {@link CommandMethod}.
     *
     * @param event
     *      The {@link MessageReceivedEvent} that's been received
     */
    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event)
    {
        if (event.getAuthor().isBot()) return;

        String[] splitText = event.getMessage().getContentRaw().split(" ", 2);
        String commandAlias = splitText[0].toLowerCase();
        String content = 2 == splitText.length ? splitText[1] : "";

//        if ("$halp".equals(commandAlias) && this.registeredCommands.containsKey(content.toLowerCase()))
//            this.handleHelpCommand(event,content.toLowerCase());

        if (this.registeredCommands.containsKey(commandAlias)) {
            CommandMethod commandMethod = this.registeredCommands.get(commandAlias);

            try {
                if (!this.handleCommandMethodCall(event, commandMethod, content)) {
                    //Content didn't match the required format of the command
                    event.getChannel()
                        .sendMessage(
                        this.buildHelpMessage(commandAlias, commandMethod,
                            "There seemed to be an error in the formatting of your command usage"))
                        .queue();
                }
            } catch (OutputException e) {
                ErrorManager.handle(event, e);
            }
        }
    }

    /**
     * Builds the help {@link MessageEmbed} describing the {@link CommandMethod}.
     *
     * @param commandAlias
     *      The {@link String} alias for this command
     * @param commandMethod
     *      The {@link CommandMethod} which correlates to this command
     * @param message
     *      Any message that you want to be included in the help embed
     *
     * @return The {@link MessageEmbed} for the help message
     */
    public static MessageEmbed buildHelpMessage(@NotNull String commandAlias, @NotNull CommandMethod commandMethod,
                                                @NotNull String message)
    {
        return new EmbedBuilder()
            .setColor(Color.cyan)
            .setTitle("HALP")
            .addField(commandAlias, message, false)
            .addField("Usage", commandMethod.getDisplayCommand(), true)
            .addField("Description", commandMethod.getDescription(), true)
            .build();
    }

    /**
     * Handles the calling and creation of the help command if invoked.
     *
     * @param event
     *      The {@link MessageReceivedEvent} that this was received from
     * @param commandAlias
     *      The {@link String} alias for the command that you want the help message displayed for
     */
    protected void handleHelpCommand(@NotNull MessageReceivedEvent event, @NotNull String commandAlias)
    {
        if (commandAlias.isEmpty()) {
            event.getChannel().sendMessage("I will try my very best!").queue();
            return;
        }

        CommandMethod commandMethod = this.registeredCommands.get(commandAlias);
        event.getChannel()
            .sendMessage(buildHelpMessage(commandAlias, commandMethod, "Here's the overview"))
            .queue();
    }

    /**
     * Registers the {@link Method methods} and generates {@link CommandMethod command methods} for each one.
     *
     * @param instance
     *      The {@link Object instance} that the {@link Method methods} belong to
     * @param annotatedMethods
     *      The {@link Object instance's} {@link Method methods} which are annotated with {@link Command}
     */
    protected void registerCommandMethods(@NotNull Object instance, @NotNull List<Method> annotatedMethods)
    {
        String defaultPrefix = Reflect.getAnnotationFieldElse(
            instance.getClass(),
            CommandGroup.class,
            CommandGroup::defaultPrefix, "");
        boolean hasDefaultPrefix = !defaultPrefix.isEmpty();

        for (Method method : annotatedMethods) {
            method.setAccessible(true);
            Command command = method.getAnnotation(Command.class);
            boolean hasPrefix = !command.prefix().isEmpty();

            if (!hasDefaultPrefix && !hasPrefix)
                throw new IllegalCommandException(
                    String.format("There's no default prefix for the method %s, so the command must define one.", method.getName()));

            String commandAlias = (hasPrefix ? command.prefix() : defaultPrefix) + command.alias();
            if (this.registeredCommands.containsKey(commandAlias))
                throw new IllegalCommandException(
                    String.format("The alias %s has already been defined and so it can't be used by the method %s",
                        commandAlias, method.getName()));
            else {
                this.registeredCommands.put(commandAlias.toLowerCase(),
                    this.createCommandMethod(instance, method, command));
            }
        }
    }

    /**
     * Displays the result of a {@link CommandMethod}. If this is a {@link MessageEmbed}, it will be automatically
     * cast and sent as such.
     *
     * @param event
     *      The {@link MessageReceivedEvent} event that was sent
     * @param oResult
     *      The {@link Optional<Object>} that was returned by the {@link CommandMethod}
     */
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    protected void displayCommandMethodResult(@NotNull MessageReceivedEvent event, @NotNull Optional<Object> oResult)
    {
        if (oResult.isEmpty())
            return;

        Object result = oResult.get();
        if (result instanceof MessageEmbed)
            event.getChannel().sendMessage((MessageEmbed) result).queue();
        else
            event.getChannel().sendMessage(result.toString()).queue();
    }

    /**
     * Creates a {@link CommandMethod} from the specified information.
     *
     * @param instance
     *      The {@link Object} that the {@link Method} belongs to
     * @param method
     *      The {@link Method} for this {@link CommandMethod}
     * @param command
     *      The {@link Command} which annotates the {@link Method}
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
     *      The {@link MessageReceivedEvent} which invoked the {@link CommandMethod}
     * @param commandMethod
     *      The {@link CommandMethod} which matches to the command alias that was invoked
     * @param content
     *      The rest of the {@link String} after the {@link String command alias} or null if there was nothing else
     *
     * @return If the {@link String content} matched this {@link CommandMethod} and it was invoked
     * @throws OutputException Any {@link OutputException} thrown by the {@link CommandMethod} when it was invoked
     */
    protected abstract boolean handleCommandMethodCall(@NotNull MessageReceivedEvent event,
                                                       @NotNull CommandMethod commandMethod,
                                                       @NotNull String content) throws OutputException;
}