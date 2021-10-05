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

package nz.pumbas.halpbot.commands;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;

import java.awt.Color;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import nz.pumbas.halpbot.commands.annotations.Source;
import nz.pumbas.halpbot.commands.commandmethods.CommandMethod;
import nz.pumbas.halpbot.commands.commandadapters.AbstractCommandAdapter;
import nz.pumbas.halpbot.commands.annotations.Command;
import nz.pumbas.halpbot.commands.annotations.Unrequired;
import nz.pumbas.halpbot.permissions.HalpbotPermissions;
import nz.pumbas.halpbot.permissions.PermissionManager;
import nz.pumbas.halpbot.sql.SQLManager;
import nz.pumbas.halpbot.utilities.HalpbotUtils;

public class BuiltInCommands
{
    private final PermissionManager permissionManager = HalpbotUtils.context().get(PermissionManager.class);

    @Command(description = "Displays the help information for the specified command")
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

    @Command(description = "Shuts the bot down. Any existing RestActions will be completed first.",
             permissions = HalpbotPermissions.BOT_OWNER)
    public void shutdown(JDA jda) {
        jda.shutdown();
    }

    @Command(description = "Shuts the bot down immediately",
             permissions = HalpbotPermissions.BOT_OWNER)
    public void forceShutdown(JDA jda) {
        jda.shutdownNow();
    }
    
    @Command(description = "Retrieves the current status of the bot")
    public String status(JDA jda) {
        return String.format("The current status of the bot is: **%s**",
            HalpbotUtils.capitalise(jda.getStatus().toString()));
    }

    @Command(description = "Retrieves the permissions that the specified user has, or the author if no user is specified")
    public MessageEmbed permissions(@Source User author, @Unrequired User user) {
        if (null == user) user = author;

        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setTitle("Permissions");
        embedBuilder.setColor(Color.ORANGE);
        embedBuilder.setFooter(user.getName(), user.getAvatarUrl());

        StringBuilder builder = new StringBuilder();
        List<String> permissions = this.permissionManager.getPermissions(user);
        if (permissions.isEmpty())
            builder.append("No permissions");
        else {
            for (String permission : permissions) {
                builder.append(permission).append('\n');
            }
        }

        embedBuilder.setDescription(builder.toString());
        return embedBuilder.build();
    }

    @Command(description = "Returns all the permissions in the database")
    public MessageEmbed allPermissions() {
        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setTitle("Permissions");
        embedBuilder.setColor(Color.ORANGE);

        StringBuilder builder = new StringBuilder();
        List<String> permissions = this.permissionManager.getAllPermissions();
        if (permissions.isEmpty())
            builder.append("No permissions");
        else {
            for (String permission : permissions) {
                builder.append(permission).append('\n');
            }
        }

        embedBuilder.setDescription(builder.toString());
        return embedBuilder.build();
    }

    @Command(description = "Gives the user the specified permission",
             permissions = HalpbotPermissions.GIVE_PERMISSIONS)
    public String givePermission(@Source User author, User user, String permission) {
        permission = permission.toLowerCase(Locale.ROOT);

        if (this.permissionManager.hasPermissions(user, permission)) {
            return "That user already has that permission!";
        }
        if (!this.permissionManager.isPermission(permission)) {
            return "The permission '" + permission + "' doesn't exist";
        }
        if (!this.permissionManager.hasPermissions(author, permission)) {
            return "You must have the permission '" + permission + "' to give it to others";
        }
        this.permissionManager.givePermission(user, permission);
        return String.format("Successfully gave the user the permission '%s'", permission);
    }

    @Command(description = "Forces all the SQLDrivers to invoke their reload listeners, refreshing any cached database information",
             permissions = HalpbotPermissions.BOT_OWNER)
    public String reloadDatabase() {
        HalpbotUtils.context().get(SQLManager.class)
            .reloadAllDrivers();

        return "Reloaded database drivers";
    }
}
