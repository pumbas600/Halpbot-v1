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

package net.pumbas.halpbot.commands.examples;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.pumbas.halpbot.converters.annotations.parameter.Source;
import net.pumbas.halpbot.triggers.Trigger;
import net.pumbas.halpbot.triggers.TriggerStrategy;
import net.pumbas.halpbot.utilities.Duration;
import net.pumbas.halpbot.utilities.Require;

import org.dockbox.hartshorn.component.Service;

import java.awt.Color;

@Service
public class ExampleTriggers {

    // Respond to any messages that contain 'my id' or 'discord id' anywhere with their id for 30 seconds
    @Trigger(value = {"my id", "discord id"}, strategy = TriggerStrategy.ANYWHERE, display = @Duration(30))
    public MessageEmbed usersId(@Source final User user) {
        return new EmbedBuilder()
            .setAuthor(user.getAsTag(), null, user.getAvatarUrl())
            .setColor(Color.CYAN)
            .setDescription("Your id is: " + user.getId())
            .build();
    }

    // Respond to any messages that contains all the triggers. Require.ALL automatically sets the strategy to ANYWHERE
    @Trigger(value = {"found", "bug"}, require = Require.ALL)
    public String foundBug() {
        return this.howToReportIssue();
    }

    @Trigger(value = {"how", "report", "issue"}, require = Require.ALL)
    public String howToReportIssue() {
        return "You can report any issues you find here: https://github.com/pumbas600/Halpbot/issues/new/choose";
    }
}
