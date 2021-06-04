package nz.pumbas.commands.commandadapters;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.Color;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import nz.pumbas.commands.CommandMethod;
import nz.pumbas.commands.ErrorManager;
import nz.pumbas.commands.annotations.Command;
import nz.pumbas.commands.exceptions.OutputException;
import nz.pumbas.utilities.Reflect;

public abstract class AbstractCommandAdapter extends ListenerAdapter
{
    protected final Map<String, CommandMethod> registeredCommands = new HashMap<>();

    protected AbstractCommandAdapter(@Nullable JDABuilder builder)
    {
        builder.addEventListeners(this);
    }

    public AbstractCommandAdapter registerCommands(Object... instances)
    {
        for (Object instance : instances) {
            this.registerCommandMethods(instance, Reflect.getAnnotatedMethods(
                instance.getClass(), Command.class, false));
        }
        return this;
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event)
    {
        if (event.getAuthor().isBot()) return;

        String[] splitText = event.getMessage().getContentRaw().split(" ", 2);
        String commandAlias = splitText[0];
        String content = 2 == splitText.length ? splitText[1] : null;

        if (this.registeredCommands.containsKey(commandAlias)) {
            CommandMethod commandMethod = this.registeredCommands.get(commandAlias);

            try {
                if (!this.handleCommandMethodCall(event, commandMethod, content)) {
                    //Content didn't match the required format of the command
                    event.getChannel().sendMessage(
                        new EmbedBuilder()
                            .setColor(Color.cyan)
                            .setTitle("HALP")
                            .addField(commandAlias, "There seemed to be an error in the formatting of " +
                                "your command usage", false)
                            .build()
                    ).queue();
                }
            } catch (OutputException e) {
                ErrorManager.handle(event, e);
            }
        }
    }

    protected abstract void registerCommandMethods(Object instance, List<Method> annotatedMethods);

    protected abstract boolean handleCommandMethodCall(@NotNull MessageReceivedEvent event,
                                                       @NotNull CommandMethod commandMethod,
                                                       @Nullable String content) throws OutputException;
}
