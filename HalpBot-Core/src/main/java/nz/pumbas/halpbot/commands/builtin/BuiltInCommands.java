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

package nz.pumbas.halpbot.commands.builtin;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;

import org.dockbox.hartshorn.core.annotations.stereotype.Service;
import org.dockbox.hartshorn.core.domain.Exceptional;
import org.jetbrains.annotations.Nullable;

import java.awt.Color;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import javax.inject.Inject;

import nz.pumbas.halpbot.commands.CommandAdapter;
import nz.pumbas.halpbot.converters.annotations.parameter.Source;
import nz.pumbas.halpbot.commands.actioninvokable.context.command.CommandContext;
import nz.pumbas.halpbot.commands.annotations.Command;
import nz.pumbas.halpbot.converters.annotations.parameter.Unrequired;
import nz.pumbas.halpbot.permissions.HalpbotPermissions;
import nz.pumbas.halpbot.permissions.Permissions;
import nz.pumbas.halpbot.permissions.PermissionService;
import nz.pumbas.halpbot.sql.SQLManager;
import nz.pumbas.halpbot.utilities.HalpbotUtils;

@Service
public class BuiltInCommands
{
    @Inject private PermissionService permissionService;
    @Inject private HelpService helpService;

    @Command(alias = { "help", "halp" }, description = "Displays the help information for the specified command")
    public Object halp(@Source Guild guild, CommandAdapter commandAdapter, @Unrequired("") String commandAlias) {
        if (commandAlias.isEmpty()) {
            return this.helpService.build(commandAdapter);
        }

        String prefix = commandAdapter.prefix(guild.getIdLong());
        String alias = commandAlias.toLowerCase(Locale.ROOT);
        if (alias.startsWith(prefix))
            alias = alias.substring(prefix.length()).stripLeading();

        Exceptional<CommandContext> commandContext = commandAdapter.commandContextSafely(alias);
        if (commandContext.absent())
            return "That doesn't seem to be a registered command :sob:";

        return this.helpService.build(commandAdapter, commandContext.get());
    }

    @Permissions(permissions = HalpbotPermissions.BOT_OWNER)
    @Command(description = "Shuts the bot down. Any existing RestActions will be completed first.")
    public void shutdown(JDA jda) {
        jda.shutdown();
    }

    @Permissions(permissions = HalpbotPermissions.BOT_OWNER)
    @Command(description = "Shuts the bot down immediately")
    public void forceShutdown(JDA jda) {
        jda.shutdownNow();
    }
    
    @Command(description = "Retrieves the current status of the bot")
    public String status(JDA jda) {
        return String.format("The current status of the bot is: **%s**",
            HalpbotUtils.capitalise(jda.getStatus().toString()));
    }

//    @Command(description = "Retrieves the permissions that the specified user has, or the author if no user is specified")
//    public MessageEmbed permissions(@Source Guild guild, @Source Member author, @Unrequired @Nullable Member member) {
//        if (null == member) member = author;
//        User user = member.getUser();
//
//        EmbedBuilder embedBuilder = new EmbedBuilder();
//        embedBuilder.setTitle("Permissions");
//        embedBuilder.setColor(Color.ORANGE);
//        embedBuilder.setFooter(user.getName(), user.getAvatarUrl());
//
//        StringBuilder builder = new StringBuilder();
//        Set<String> permissions = this.permissionService.permissions(guild.getIdLong(), member);
//        if (permissions.isEmpty())
//            builder.append("No permissions");
//        else {
//            for (String permission : permissions) {
//                builder.append(permission).append('\n');
//            }
//        }
//
//        embedBuilder.setDescription(builder.toString());
//        return embedBuilder.build();
//    }

//    //TODO: Rework
//    @Command(description = "Returns all the permissions in the database")
//    public MessageEmbed allPermissions() {
//        EmbedBuilder embedBuilder = new EmbedBuilder();
//        embedBuilder.setTitle("Permissions");
//        embedBuilder.setColor(Color.ORANGE);
//
//        StringBuilder builder = new StringBuilder();
//        List<String> permissions = this.permissionManager.permissions();
//        if (permissions.isEmpty())
//            builder.append("No permissions");
//        else {
//            for (String permission : permissions) {
//                builder.append(permission).append('\n');
//            }
//        }
//
//        embedBuilder.setDescription(builder.toString());
//        return embedBuilder.build();
//    }

//    @Permission(HalpbotPermissions.GIVE_PERMISSIONS)
//    @Command(description = "Gives the user the specified permission")
//    public String givePermission(@Source User author, User user, String permission) {
//        permission = permission.toLowerCase(Locale.ROOT);
//
//        if (this.permissionManager.hasPermission(user, permission)) {
//            return "That user already has that permission!";
//        }
//        if (!this.permissionManager.isPermission(permission)) {
//            return "The permission '" + permission + "' doesn't exist";
//        }
//        if (!this.permissionManager.hasPermission(author, permission)) {
//            return "You must have the permission '" + permission + "' to give it to others";
//        }
//        this.permissionManager.givePermission(user, permission);
//        return String.format("Successfully gave the user the permission '%s'", permission);
//    }

    //TODO: Make it so that it automatically throws an error when a field is null
    @Permissions(Permission.MANAGE_PERMISSIONS)
    @Command(description = "Binds a permission to a role")
    public String bind(@Source @Nullable Guild guild, String permission, @Nullable Role newRole) {
        if (guild == null)
            return "This cannot be used in a private message";
        Exceptional<Role> oldRole = this.permissionService.guildRole(guild, permission);
        if (newRole == null)
            return "The role specified doesn't exist";

        if (oldRole.present() && oldRole.get().getIdLong() == newRole.getIdLong())
            return "The permission `%s` is already bound to `%s`".formatted(permission, newRole.getName());

        String result = oldRole.absent()
                ? "Binding the permission `%s` to `%s`".formatted(permission, newRole.getName())
                : "Updating the binding of the permission `%s` from `%s` to `%s`"
                        .formatted(permission, oldRole.get().getName(), newRole.getName());

        this.permissionService.updateOrSave(guild.getIdLong(),permission, newRole.getIdLong());
        return result;
    }

    @Permissions(Permission.MANAGE_PERMISSIONS)
    @Command(description = "Returns the role bindings for the permissions in the specified guild")
    public Object guildPermissions(@Source @Nullable Guild guild) {
        if (guild == null)
            return "This cannot be used in a private message";

        Map<String, Long> bindings = this.permissionService.permissionBindings(guild);
        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setTitle("%s's Permission Bindings".formatted(guild.getName()))
                .setColor(Color.ORANGE);

        for (String permission : bindings.keySet().stream().sorted().collect(Collectors.toList())) {
            Long roleId = bindings.get(permission);
            String role = "Unbound";
            if (roleId != null) {
                Role guildRole = guild.getRoleById(roleId);
                if (guildRole != null)
                    role = guildRole.getName();
            }
            embedBuilder.appendDescription("`%s` - `%s`\n".formatted(permission, role));
        }
        return embedBuilder.build();
    }

    @Deprecated(forRemoval = true)
    @Permissions(permissions = HalpbotPermissions.BOT_OWNER)
    @Command(description = "Forces all the SQLDrivers to invoke their reload listeners, refreshing any cached database information")
    public String reloadDatabase() {
        HalpbotUtils.context().get(SQLManager.class)
            .reloadAllDrivers();

        return "Reloaded database drivers";
    }
}
