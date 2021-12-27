package nz.pumbas.halpbot.events;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.AbstractChannel;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Guild;
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
        if (this.event instanceof MessageReceivedEvent)
            return ((MessageReceivedEvent) this.event).getAuthor();
        else if (this.event instanceof GenericMessageReactionEvent)
            return ((GenericMessageReactionEvent) this.event).retrieveUser().complete();
        throw new UnsupportedOperationException(
            "The event " + this.event.getClass().getSimpleName() + " doesn't support this operation");
    }

    @Override
    public JDA jda() {
        return this.event.getJDA();
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
        if (0 >= seconds)
            throw new IllegalArgumentException("The number of seconds for a temporary response must be greater than 0");
        this.event.getChannel().sendMessage(message)
            .queue(m -> m.delete().queueAfter(seconds, TimeUnit.SECONDS));
    }

    @Override
    public void replyTemporarily(MessageEmbed embed, long seconds) {
        if (0 >= seconds)
            throw new IllegalArgumentException("The number of seconds for a temporary response must be greater than 0");
        this.event.getChannel().sendMessageEmbeds(embed)
            .queue(m -> m.delete().queueAfter(seconds, TimeUnit.SECONDS));
    }
}
