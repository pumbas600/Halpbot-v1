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

package net.pumbas.halpbot.configurations;

import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.requests.restaction.MessageAction;
import net.pumbas.halpbot.events.HalpbotEvent;
import net.pumbas.halpbot.objects.DiscordObject;
import net.pumbas.halpbot.utilities.HalpbotUtils;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

public interface DisplayConfiguration {

    default void display(HalpbotEvent event, Object object, Duration duration) {
        if (duration.isNegative())
            this.display(event, object);
        else
            this.displayTemporary(event, object, duration.getSeconds());
    }

    default void display(HalpbotEvent event, Object object) {
        if (object instanceof RestAction<?> restAction)
            restAction.queue();
        else if (object instanceof MessageEmbed messageEmbed)
            this.display(event, messageEmbed);
        else if (object instanceof DiscordObject discordObject)
            this.display(event, HalpbotUtils.limitMessageLength(discordObject.toDiscordString()));
        else
            this.display(event, HalpbotUtils.limitMessageLength(object.toString()));
    }

    default void displayTemporary(HalpbotEvent event, Object object, long seconds) {
        if (object instanceof MessageAction action)
            action.queue((m) -> m.delete().queueAfter(seconds, TimeUnit.SECONDS));
        else if (object instanceof RestAction<?> action)
            action.queue();
        else if (object instanceof MessageEmbed messageEmbed)
            this.displayTemporary(event, messageEmbed, seconds);
        else if (object instanceof DiscordObject discordObject)
            this.displayTemporary(event, HalpbotUtils.limitMessageLength(discordObject.toDiscordString()), seconds);
        else
            this.displayTemporary(event, HalpbotUtils.limitMessageLength(object.toString()), seconds);
    }

    void display(HalpbotEvent event, MessageEmbed embed);

    void display(HalpbotEvent event, String message);

    void displayTemporary(HalpbotEvent event, MessageEmbed embed, long seconds);

    void displayTemporary(HalpbotEvent event, String message, long seconds);
}
