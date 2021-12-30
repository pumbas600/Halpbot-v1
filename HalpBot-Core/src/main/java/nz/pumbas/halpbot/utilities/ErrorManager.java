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
import net.dv8tion.jda.api.events.message.GenericMessageEvent;

import org.dockbox.hartshorn.core.Enableable;
import org.dockbox.hartshorn.core.annotations.context.AutoCreating;
import org.dockbox.hartshorn.core.annotations.stereotype.Service;
import org.dockbox.hartshorn.core.context.ApplicationContext;
import org.jetbrains.annotations.Nullable;

import java.awt.Color;

import javax.inject.Inject;

import nz.pumbas.halpbot.HalpbotCore;
import nz.pumbas.halpbot.commands.exceptions.ErrorMessageException;
import nz.pumbas.halpbot.commands.exceptions.UnimplementedFeatureException;

@Service
@AutoCreating
public class ErrorManager implements Enableable
{
    @Nullable
    private static ErrorManager instance;

    @Inject private ApplicationContext applicationContext;
    @Inject private HalpbotCore halpbotCore;

    @Override
    public boolean canEnable() {
        return instance == null;
    }

    @Override
    public void enable() {
        instance = this;
        this.applicationContext.log().info("Error manager initialised");
    }

    private static ErrorManager instance() {
        if (instance == null)
            throw new UnsupportedOperationException(
                    "You're trying to access the ErrorManager instance before it's been created");
        return instance;
    }

    public static void handle(Throwable e) {
        handle(e, "Caught the error: ");
    }

    public static void handle(Throwable e, String message) {
        instance().applicationContext.log().error(message, e);
        sendToLoggerUser(null, e, message);
    }

    public static void handle(GenericMessageEvent event, Throwable e) {
        handle(event, e, "Caught the error: ");
    }

    public static void handle(GenericMessageEvent event, Throwable e, String message) {

        if (e instanceof UnimplementedFeatureException) {
            unimplementedFeatureEmbed(event, e.getMessage());
        }
        else if (e instanceof ErrorMessageException) {
            String warningMessage = ":warning: " + e.getMessage();
            //HalpbotUtils.logger().warn(warningMessage);
            event.getChannel().sendMessage(warningMessage).queue();
        }
        else {
            sendToLoggerUser(event, e, message);
        }
    }

    public static void unimplementedFeatureEmbed(GenericMessageEvent event, String message) {
        event.getChannel().sendMessageEmbeds(
            new EmbedBuilder().setTitle(":confounded: Sorry...")
                .setColor(Color.red)
                .addField("This feature is not implemented yet", message, false)
                .build())
            .queue();
    }

    private static void sendToLoggerUser(@Nullable GenericMessageEvent event, Throwable e, String message) {
//        EmbedBuilder error = new EmbedBuilder();
//        error.setTitle(message);
//        error.setColor(Color.red);
//
//        Throwable displayThrowable = null == e.getCause() ? e : e.getCause();
//
//        String stackTrace = HalpbotUtils.getStackTrace(displayThrowable);
//        error.setDescription(HalpbotUtils.limitEmbedDescriptionLength(stackTrace));
//
//        if (null != event) {
//            error.setFooter(event.getGuild().getName(), event.getGuild().getBannerUrl());
//        }
//
//        instance().halpbotCore.jda().retrieveUserById(instance().halpbotCore.ownerId())
//            .flatMap(User::openPrivateChannel)
//            .flatMap(channel -> channel.sendMessageEmbeds(error.build()))
//            .queue();
    }
}
