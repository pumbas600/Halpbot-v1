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

package nz.pumbas.halpbot.commands.examples;

import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.components.Button;

import org.dockbox.hartshorn.core.annotations.stereotype.Service;

import java.time.temporal.ChronoUnit;

import javax.inject.Inject;

import nz.pumbas.halpbot.buttons.ButtonAction;
import nz.pumbas.halpbot.buttons.ButtonAdapter;
import nz.pumbas.halpbot.commands.annotations.Command;
import nz.pumbas.halpbot.converters.annotations.parameter.Source;
import nz.pumbas.halpbot.converters.annotations.parameter.Unrequired;
import nz.pumbas.halpbot.utilities.Duration;
import nz.pumbas.halpbot.utilities.Int;

@Service
public class ExampleButtons
{
    @Inject
    private ButtonAdapter buttonAdapter;

    @Command(description = "Displays two test buttons")
    public void buttons(MessageReceivedEvent event) {
        event.getChannel().sendMessage("Click on one of these buttons!")
                .setActionRow(
                        // When the button is clicked, the @ButtonAction with the matching id is invoked
                        Button.primary("halpbot:example:primary", "Primary button!"),
                        Button.secondary("halpbot:example:secondary", "Secondary button!")
                ).queue();
    }

    @ButtonAction(id = "halpbot:example:primary")
    public String primary(ButtonClickEvent event) { // You can directly pass the event
        return "%s clicked the primary button!".formatted(event.getUser().getName());
    }

    // The display field specifies that the result should only be displayed for 20 seconds before being deleted
    @ButtonAction(id = "halpbot:example:secondary", display = @Duration(20))
    public String secondary(@Source User user) { // Alternatively, you can retrieve fields from the event using @Source
        return "%s clicked the secondary button!".formatted(user.getName());
    }

    @Command(description = "Creates an example dynamic button which allows you to increment a number by one")
    public void count(MessageReceivedEvent event, @Unrequired("1") int startingNumber) {
        event.getChannel().sendMessage("Number: " + startingNumber)
                .setActionRow(
                        this.buttonAdapter.register(
                                Button.success("halpbot:plusone", "+1"),
                                new Int(startingNumber)))
                .queue();
    }

    @ButtonAction(id = "halpbot:plusone", removeAfter = @Duration(20))
    public void dynamicButton(ButtonClickEvent event, Int num) {
        num.incrementBefore();
        event.editMessage("Number: " + num).queue();
    }
}
