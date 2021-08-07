package nz.pumbas;

import net.dv8tion.jda.api.EmbedBuilder;

import java.awt.Color;
import java.util.Locale;
import java.util.Optional;

import nz.pumbas.commands.CommandMethod;
import nz.pumbas.commands.annotations.Command;
import nz.pumbas.commands.annotations.Unrequired;
import nz.pumbas.commands.commandadapters.AbstractCommandAdapter;

public class BuiltInCommands
{
    @Command(alias = "Halp", description = "Displays the help information for the specified command")
    public Object halp(AbstractCommandAdapter commandAdapter, @Unrequired String commandAlias) {
        if (commandAlias.isEmpty()) {
            EmbedBuilder embedBuilder = new EmbedBuilder()
                .setColor(Color.ORANGE)
                .setTitle("HALP - Commands");

            var registeredCommands = commandAdapter.getRegisteredCommands();
            for (var command : registeredCommands.entrySet()) {
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("\n**Usage**\n")
                    .append(command.getValue().getDisplayCommand().isEmpty() ? "N/A" : command.getValue().getDisplayCommand())
                        .append("\n**Description**\n")
                    .append(command.getValue().getDescription().isEmpty() ? "N/A" : command.getValue().getDescription());

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

        return AbstractCommandAdapter.buildHelpMessage(alias, commandMethod.get(), "Here's the overview");
    }

}
