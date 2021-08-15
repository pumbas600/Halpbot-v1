package nz.pumbas.halpbot;

import net.dv8tion.jda.api.EmbedBuilder;

import java.awt.Color;
import java.util.Locale;
import java.util.Optional;

import nz.pumbas.halpbot.commands.CommandMethod;
import nz.pumbas.halpbot.commands.commandadapters.CommandAdapter;
import nz.pumbas.halpbot.commands.annotations.Command;
import nz.pumbas.halpbot.commands.annotations.Unrequired;

public class BuiltInCommands
{
    @Command(alias = "Halp", description = "Displays the help information for the specified command")
    public Object halp(CommandAdapter commandAdapter, @Unrequired("") String commandAlias) {
        if (commandAlias.isEmpty()) {
            EmbedBuilder embedBuilder = new EmbedBuilder()
                .setColor(Color.ORANGE)
                .setTitle("HALP - Commands");

            var registeredCommands = commandAdapter.getRegisteredCommands();
            for (var command : registeredCommands.entrySet()) {
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("\n**Usage**\n")
                    .append(command.getKey()).append(" ").append(command.getValue().getUsage())
                    .append("\n**Description**\n")
                    .append(command.getValue().getDescription());

                embedBuilder.addField(command.getKey(), stringBuilder.toString(), true);
            }

            return embedBuilder.build();
        }

        String alias = commandAlias.toLowerCase(Locale.ROOT);
        if (!alias.startsWith(commandAdapter.getCommandPrefix()))
            alias = commandAdapter.getCommandPrefix() + alias;

        Optional<CommandMethod> commandMethod = commandAdapter.getCommandMethod(alias);
        if (commandMethod.isEmpty())
            return "That doesn't seem to be a registered command :sob:";

        return CommandAdapter.buildHelpMessage(alias, commandMethod.get(), "Here's the overview");
    }

}
