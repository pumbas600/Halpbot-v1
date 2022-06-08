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

package net.pumbas.halpbot.events;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.AbstractChannel;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.PrivateChannel;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;

import org.dockbox.hartshorn.util.Result;
import org.jetbrains.annotations.Nullable;

public interface HalpbotEvent
{
    Object rawEvent();

    @SuppressWarnings("unchecked")
    default <T> T event(Class<T> type) {
        if (type.isAssignableFrom(this.rawEvent().getClass())) {
            return (T) this.rawEvent();
        }
        throw new UnsupportedOperationException("The raw event is not of the specified type: " + type.getSimpleName());
    }

    default <T> Result<T> eventSafely(Class<T> type) {
        return Result.of(() -> this.event(type));
    }

    MessageChannel messageChannel();

    TextChannel textChannel();

    PrivateChannel privateChannel();

    /**
     * This is currently never nullable, but may be nullable in the future according to the Javadocs.
     *
     * @return The channel the event was created in
     */
    @Nullable
    AbstractChannel channel();

    ChannelType channelType();

    /**
     * This can be null if the event was created in a private message.
     *
     * @return The guild this event was created in
     */
    @Nullable
    Guild guild();

    User user();

    JDA jda();

    /**
     * The {@link User} of the event received as Member object. This will be null in case of Message being received in a
     * PrivateChannel or isWebhookMessage() returning true.
     *
     * @return The author as a Member object
     */
    @Nullable
    Member member();

    void reply(String message);

    void reply(MessageEmbed embed);

    /**
     * Replies temporarily by deleting the response after the specified number of seconds. If the duration is less than
     * 1 second, it will set the reply to ephemeral for an {@link InteractionEvent}, however, if it's not an interation
     * event, then this will cause an {@link IllegalArgumentException} to be thrown.
     *
     * @param message
     *     The message to send
     * @param seconds
     *     The number of seconds to wait before deleting the response
     */
    void replyTemporarily(String message, long seconds);

    /**
     * Replies temporarily by deleting the response after the specified number of seconds. If the duration is less than
     * 1 second, it will set the reply to ephemeral for an {@link InteractionEvent}, however, if it's not an interation
     * event, then this will cause an {@link IllegalArgumentException} to be thrown.
     *
     * @param embed
     *     The embed to send
     * @param seconds
     *     The number of seconds to wait before deleting the response
     */
    void replyTemporarily(MessageEmbed embed, long seconds);
}
