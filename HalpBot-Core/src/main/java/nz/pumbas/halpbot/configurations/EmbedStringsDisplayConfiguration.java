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

package nz.pumbas.halpbot.configurations;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

import java.awt.Color;

import nz.pumbas.halpbot.events.HalpbotEvent;
import nz.pumbas.halpbot.utilities.HalpbotUtils;

public class EmbedStringsDisplayConfiguration implements DisplayConfiguration
{
    @Override
    public void display(HalpbotEvent event, String message) {
        this.display(event, this.createEmbed(message));
    }

    @Override
    public void display(HalpbotEvent event, MessageEmbed embed) {
        event.reply(embed);
    }

    @Override
    public void displayTemporary(HalpbotEvent event, String message, long seconds) {
        this.displayTemporary(event, this.createEmbed(message), seconds);
    }

    @Override
    public void displayTemporary(HalpbotEvent event, MessageEmbed embed, long seconds) {
        event.replyTemporarily(embed, seconds);
    }

    private MessageEmbed createEmbed(String message) {
        EmbedBuilder builder = new EmbedBuilder();
        builder.setDescription(HalpbotUtils.limitEmbedDescriptionLength(message));
        builder.setColor(Color.ORANGE);
        return builder.build();
    }
}
