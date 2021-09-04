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

package nz.pumbas.halpbot;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;

import java.awt.Color;
import java.util.Locale;
import java.util.Optional;

import nz.pumbas.halpbot.commands.commandmethods.CommandMethod;
import nz.pumbas.halpbot.commands.commandadapters.AbstractCommandAdapter;
import nz.pumbas.halpbot.commands.annotations.Command;
import nz.pumbas.halpbot.commands.annotations.Unrequired;
import nz.pumbas.halpbot.utilities.HalpbotUtils;

public class BuiltInCommands
{
    @Command(alias = "Halp", description = "Displays the help information for the specified command")
    public Object halp(AbstractCommandAdapter commandAdapter, @Unrequired("") String commandAlias) {
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

        return AbstractCommandAdapter.buildHelpMessage(alias, commandMethod.get(), "Here's the overview");
    }

    @Command(alias = "shutdown", description = "Shuts the bot down. Any existing RestActions will be completed first.",
             restrictedTo = 260930648330469387L)
    public void shutdown(JDA jda) {
        jda.shutdown();
    }

    @Command(alias = "forceShutdown", description = "Shuts the bot down immediately",
             restrictedTo = 260930648330469387L)
    public void forceShutdown(JDA jda) {
        jda.shutdownNow();
    }
    
    @Command(alias = "status", description = "Retrieves the current status of the bot")
    public String status(JDA jda) {
        return String.format("The current status of the bot is: **%s**",
            HalpbotUtils.capitalise(jda.getStatus().toString()));
    }

}
