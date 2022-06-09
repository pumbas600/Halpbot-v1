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

package net.pumbas.halpbot.commands;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.components.Button;
import net.dv8tion.jda.api.requests.restaction.MessageAction;
import net.pumbas.halpbot.actions.cooldowns.Cooldown;
import net.pumbas.halpbot.actions.cooldowns.CooldownType;
import net.pumbas.halpbot.buttons.ButtonAdapter;
import net.pumbas.halpbot.buttons.ButtonHandler;
import net.pumbas.halpbot.commands.annotations.Command;
import net.pumbas.halpbot.converters.annotations.parameter.Implicit;
import net.pumbas.halpbot.converters.annotations.parameter.Remaining;
import net.pumbas.halpbot.converters.annotations.parameter.Source;
import net.pumbas.halpbot.converters.annotations.parameter.Unrequired;
import net.pumbas.halpbot.customparameters.Shape;
import net.pumbas.halpbot.decorators.log.Log;
import net.pumbas.halpbot.permissions.PermissionService;
import net.pumbas.halpbot.triggers.Trigger;
import net.pumbas.halpbot.utilities.Duration;
import net.pumbas.halpbot.utilities.Require;

import org.checkerframework.checker.units.qual.Time;
import org.dockbox.hartshorn.application.context.ApplicationContext;
import org.dockbox.hartshorn.component.Service;
import org.jetbrains.annotations.Nullable;

import jakarta.inject.Inject;

@Log
@Service
public class HalpbotCommands {

    @Inject
    private PermissionService permissionService;
    @Inject
    private ApplicationContext applicationContext;

    @Command(alias = "source")
    public String source() {
        return "You can see the source code for me here: https://github.com/pumbas600/HalpBot :kissing_heart:";
    }

    @Command(alias = "invite", description = "Retrieves the invite for this discord bot")
    public String invite() {
        return "https://canary.discord.com/api/oauth2/authorize?client_id=819840092327772170&permissions=2147544128&scope=bot%20applications.commands";
    }

    @Command(alias = "suggestion")
    public String suggestion() {
        return "You can note issues and suggestions for me here: https://github.com/pumbas600/HalpBot/issues";
    }

    @Trigger(value = {"halpbot", "show", "easter egg"}, require = Require.ALL, display = @Duration(20))
    public String easterEgg() {
        return "Here's the easter egg! :sparkles:";
    }

    @Command
    public String author(final User user) {
        return user.toString();
    }

    @Command
    public String hasPermission(@Nullable @Source final Guild guild,
                                @Source final Member author,
                                final String permission,
                                @Unrequired @Nullable Member member)
    {
        if (guild == null)
            return "This cannot be done in a private message";
        if (member == null)
            member = author;
        return this.permissionService.hasPermission(guild, member, permission)
            ? "You have the permission!"
            : "You don't have the permission :(";
    }

    @Time
    @Command(description = "Tests dynamic buttons")
    public MessageAction dynamicDemo(final MessageReceivedEvent event, final ButtonAdapter buttonAdapter, @Remaining final String suffix) {
        return event.getChannel()
            .sendMessage("This is a dynamic suffix adding button demo")
            .setActionRow(buttonAdapter.register(
                Button.primary("halpbot:button:suffix", "Add suffix"), suffix));
    }

    @Time
    @ButtonHandler(id = "halpbot:button:suffix", display = @Duration(10))
    public String suffix(@Source final User user, final String suffix) {
        return user.getName() + suffix;
    }

    @Cooldown(duration = @Duration(90), type = CooldownType.MEMBER)
    @Command(description = "Tests the cooldown decorators")
    public String cooldownMember() {
        return "This command is logged when its invoked and has a 90 second cooldown!";
    }

    @Cooldown(duration = @Duration(90), type = CooldownType.USER)
    @Command(description = "Tests the cooldown decorators")
    public String cooldownUser() {
        return "This command is logged when its invoked and has a 90 second cooldown!";
    }

    @Cooldown(duration = @Duration(90), type = CooldownType.GUILD)
    @Command(description = "Tests the cooldown decorators")
    public String cooldownGuild() {
        return "This command is logged when its invoked and has a 90 second cooldown!";
    }

    @Command(description = "Tests the @Log decorator")
    public String log() {
        return "This command is logged when it is invoked";
    }

    @Time
    @Command(description = "Tests the @Time decorator")
    public String time(final int limit) {
        double sum = 0;
        // Some expensive action:
        for (int i = 0; i < limit; i++) {
            sum += Math.sqrt(i);
        }

        return "Action complete!";
    }

    @Command(alias = "centroid", description = "Finds the centroid defined by the specified shapes")
    public String centroid(@Implicit final Shape[] shapes)
    {
        double sumAx = 0;
        double sumAy = 0;
        double totalA = 0;

        for (final Shape shape : shapes) {
            sumAx += shape.getArea() * shape.getxPos();
            sumAy += shape.getArea() * shape.getyPos();
            totalA += shape.getArea();
        }

        return String.format("x: %.2f, y: %.2f", sumAx / totalA, sumAy / totalA);
    }
}
