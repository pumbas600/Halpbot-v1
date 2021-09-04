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

package nz.pumbas.halpbot.utilities;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.awt.Color;

import nz.pumbas.halpbot.commands.exceptions.ErrorMessageException;
import nz.pumbas.halpbot.commands.exceptions.UnimplementedFeatureException;

public final class ErrorManager
{
    private ErrorManager() {}

    public static void handle(Throwable e) {
        HalpbotUtils.logger().error("Caught the error: ", e);
    }

    public static void handle(Throwable e, String message) {
        HalpbotUtils.logger().error(message, e);
    }

    public static void handle(MessageReceivedEvent event, Throwable e) {
        handle(event, e, "");
    }

    public static void handle(MessageReceivedEvent event, Throwable e, String message) {

        if (e instanceof UnimplementedFeatureException) {
            unimplementedFeatureEmbed(event, e.getMessage());
        }
        else if (e instanceof ErrorMessageException) {
            String warningMessage = ":warning: " + e.getMessage();
            HalpbotUtils.logger().warn(warningMessage);
            event.getChannel().sendMessage(warningMessage).queue();
        }
        else if (!message.isEmpty() && null != message)
            handle(e, message);
        else {
            handle(e);
        }
    }

    public static void unimplementedFeatureEmbed(MessageReceivedEvent event, String message) {
        event.getChannel().sendMessageEmbeds(
            new EmbedBuilder().setTitle(":confounded: Sorry...")
                .setColor(Color.red)
                .addField("This feature is not implemented yet", message, false)
                .build())
            .queue();
    }
}
