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

package net.pumbas.halpbot.commands.builtin;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDAInfo;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.pumbas.halpbot.commands.CommandAdapter;
import net.pumbas.halpbot.commands.actioninvokable.context.command.CommandContext;
import net.pumbas.halpbot.commands.annotations.Command;
import net.pumbas.halpbot.converters.annotations.parameter.Source;
import net.pumbas.halpbot.converters.annotations.parameter.Unrequired;
import net.pumbas.halpbot.permissions.HalpbotPermissions;
import net.pumbas.halpbot.permissions.PermissionService;
import net.pumbas.halpbot.permissions.Permissions;
import net.pumbas.halpbot.permissions.repositories.GuildPermission;
import net.pumbas.halpbot.permissions.repositories.GuildPermissionId;
import net.pumbas.halpbot.utilities.HalpbotUtils;

import org.dockbox.hartshorn.component.Service;
import org.dockbox.hartshorn.util.Result;
import org.jetbrains.annotations.Nullable;

import java.awt.Color;
import java.lang.management.ManagementFactory;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import jakarta.inject.Inject;

@Service
public class BuiltInCommands {

    @Inject
    private PermissionService permissionService;
    @Inject
    private HelpService helpService;

    @Command(description = "Displays the current information of this bot")
    public void info(final MessageReceivedEvent event, final JDA jda) {
        final Runtime runtime = Runtime.getRuntime();
        final User selfUser = jda.getSelfUser();

        jda.getRestPing().queue(ping ->
            event.getChannel().sendMessageEmbeds(
                    new EmbedBuilder()
                        .setTitle("%s's Information".formatted(selfUser.getName()))
                        .setColor(Color.ORANGE)
                        .addField("JVM Version:", System.getProperty("java.version"), true)
                        .addField("JDA Version:", JDAInfo.VERSION, true)
                        .addBlankField(true)
                        .addField("Gateway Ping:", jda.getGatewayPing() + "ms", true)
                        .addField("Rest Ping:", ping + "ms", true)
                        .addBlankField(true)
                        .addField("Memory Usage:", ((runtime.totalMemory() - runtime.freeMemory()) >> 20) + "MB / " + (runtime.maxMemory() >> 20) + "MB", true)
                        .addField("Thread Count:", String.valueOf(ManagementFactory.getThreadMXBean().getThreadCount()), true)
                        .addBlankField(true)
                        .build())
                .queue());
    }

    @Command(alias = {"help", "halp"}, description = "Displays the help information for the specified command")
    public Object halp(@Source final Guild guild, final CommandAdapter commandAdapter, @Unrequired("") final String commandAlias) {
        if (commandAlias.isEmpty()) {
            return this.helpService.build(commandAdapter);
        }

        final String prefix = commandAdapter.prefix(guild.getIdLong());
        String alias = commandAlias.toLowerCase(Locale.ROOT);
        if (alias.startsWith(prefix))
            alias = alias.substring(prefix.length()).stripLeading();

        final Result<CommandContext> commandContext = commandAdapter.commandContextSafely(alias);
        if (commandContext.absent())
            return "That doesn't seem to be a registered command :sob:";

        return this.helpService.build(commandAdapter, commandContext.get());
    }

    @Permissions(permissions = HalpbotPermissions.BOT_OWNER)
    @Command(description = "Shuts the bot down. Any existing RestActions will be completed first.")
    public void shutdown(final JDA jda) {
        jda.shutdown();
    }

    @Permissions(permissions = HalpbotPermissions.BOT_OWNER)
    @Command(description = "Shuts the bot down immediately")
    public void forceShutdown(final JDA jda) {
        jda.shutdownNow();
    }

    @Command(description = "Retrieves the current status of the bot")
    public String status(final JDA jda) {
        return String.format("The current status of the bot is: **%s**",
            HalpbotUtils.capitalise(jda.getStatus().toString()));
    }

    //TODO: Make it so that it automatically throws an error when a field is null
    @Permissions(user = Permission.MANAGE_PERMISSIONS)
    @Command(description = "Binds a permission to a role")
    public String bind(@Source @Nullable final Guild guild, final String permission, @Nullable final Role newRole) {
        if (!this.permissionService.useRoleBinding())
            return "Role binding has been disabled for this bot";
        if (guild == null)
            return "This cannot be used in a private message";
        if (newRole == null)
            return "The role specified doesn't exist";
        if (!this.permissionService.isRegistered(permission))
            return "%s is not a bindable permission".formatted(permission);

        final Result<GuildPermission> oldGp =
            this.permissionService.findById(new GuildPermissionId(guild.getIdLong(), permission));
        String result = "Binding the permission `%s` to `%s`".formatted(permission, newRole.getName());

        if (oldGp.present()) {
            final Role oldRole = guild.getRoleById(oldGp.get().roleId());
            if (oldRole != null) {
                if (oldRole.getIdLong() == newRole.getIdLong())
                    return "The permission `%s` is already bound to `%s`".formatted(permission, newRole.getName());
                result = "Updating the binding of the permission `%s` from `%s` to `%s`"
                    .formatted(permission, oldRole.getName(), newRole.getName());
            }
            //this.permissionService.close();
        }
        this.permissionService.updateOrSave(new GuildPermission(guild.getIdLong(), permission, newRole.getIdLong()));
        //this.permissionService.close();
        return result;
    }

    @Permissions(user = Permission.MANAGE_PERMISSIONS)
    @Command(description = "Returns the role bindings for the permissions in the specified guild")
    public Object guildPermissions(@Source @Nullable final Guild guild) {
        if (!this.permissionService.useRoleBinding())
            return "Role binding has been disabled for this bot";
        if (guild == null)
            return "This cannot be used in a private message";

        final Map<String, Long> bindings = this.permissionService.roleBindings(guild);
        final EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setTitle("%s's Permission Bindings".formatted(guild.getName()))
            .setColor(Color.ORANGE);

        for (final String permission : bindings.keySet().stream().sorted().collect(Collectors.toList())) {
            final Long roleId = bindings.get(permission);
            String role = "Unbound";
            if (roleId != null) {
                final Role guildRole = guild.getRoleById(roleId);
                if (guildRole != null)
                    role = guildRole.getName();
            }
            embedBuilder.appendDescription("`%s` - `%s`\n".formatted(permission, role));
        }
        return embedBuilder.build();
    }
}
