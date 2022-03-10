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
import net.dv8tion.jda.api.events.message.GenericMessageEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.react.GenericMessageReactionEvent;

import org.jetbrains.annotations.Nullable;

import java.util.concurrent.TimeUnit;

public class MessageEvent implements HalpbotEvent
{
    private final GenericMessageEvent event;

    public MessageEvent(GenericMessageEvent event) {
        this.event = event;
    }

    @Override
    public Object rawEvent() {
        return this.event;
    }

    @Override
    public MessageChannel messageChannel() {
        return this.event.getChannel();
    }

    @Override
    public TextChannel textChannel() {
        return this.event.getTextChannel();
    }

    @Override
    public PrivateChannel privateChannel() {
        return this.event.getPrivateChannel();
    }

    @Nullable
    @Override
    public AbstractChannel channel() {
        return this.event.getChannel();
    }

    @Override
    public ChannelType channelType() {
        return this.event.getChannelType();
    }

    @Override
    public Guild guild() {
        return this.event.getGuild();
    }

    @Override
    public User user() {
        if (this.event instanceof MessageReceivedEvent messageReceivedEvent)
            return messageReceivedEvent.getAuthor();
        else if (this.event instanceof GenericMessageReactionEvent messageReactionEvent)
            return messageReactionEvent.retrieveUser().complete();
        throw new UnsupportedOperationException(
            "The event " + this.event.getClass().getSimpleName() + " doesn't support this operation");
    }

    @Override
    public JDA jda() {
        return this.event.getJDA();
    }

    //TODO: Make this return CompletableFuture
    @Override
    @Nullable
    public Member member() {
        if (this.event instanceof MessageReceivedEvent messageReceivedEvent)
            return messageReceivedEvent.getMember();
        else if (this.event instanceof GenericMessageReactionEvent messageReactionEvent)
            return messageReactionEvent.retrieveMember().complete();
        throw new UnsupportedOperationException(
            "The event " + this.event.getClass().getSimpleName() + " doesn't support this operation");
    }

    @Override
    public void reply(String message) {
        this.event.getChannel().sendMessage(message).queue();
    }

    @Override
    public void reply(MessageEmbed embed) {
        this.event.getChannel().sendMessageEmbeds(embed).queue();
    }

    @Override
    public void replyTemporarily(String message, long seconds) {
        this.event.getChannel().sendMessage(message)
            .queue(m -> m.delete().queueAfter(Math.abs(seconds), TimeUnit.SECONDS));
    }

    @Override
    public void replyTemporarily(MessageEmbed embed, long seconds) {
        this.event.getChannel().sendMessageEmbeds(embed)
            .queue(m -> m.delete().queueAfter(Math.abs(seconds), TimeUnit.SECONDS));
    }
}
