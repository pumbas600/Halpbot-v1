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
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.awt.Color;

import nz.pumbas.halpbot.commands.exceptions.ErrorMessageException;
import nz.pumbas.halpbot.commands.exceptions.UnimplementedFeatureException;

public final class ErrorManager
{
    private static long loggerUserId = -1;

    public static void setLoggerUserId(long userId) {
        loggerUserId = userId;
    }

    private ErrorManager() {}

    public static void handle(Throwable e) {
        handle(e, "Caught the error: ");
    }

    public static void handle(Throwable e, String message) {
        HalpbotUtils.logger().error(message, e);
        sendToLoggerUser(null, e, message);
    }

    public static void handle(MessageReceivedEvent event, Throwable e) {
        handle(event, e, "Caught the error: ");
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
        else {
            sendToLoggerUser(event, e, message);
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

    private static void sendToLoggerUser(MessageReceivedEvent event, Throwable e, String message) {
        if (-1 != loggerUserId) {
            EmbedBuilder error = new EmbedBuilder();
            error.setTitle(message);
            error.setColor(Color.red);
            error.setDescription(e.getMessage());
            if (null != event) {
                error.setFooter(event.getGuild().getName(), event.getGuild().getBannerUrl());
            }

            HalpbotUtils.getJDA().getUserById(loggerUserId)
                .openPrivateChannel()
                .flatMap(channel -> channel.sendMessageEmbeds(error.build()))
                .queue();
        }
    }
}
