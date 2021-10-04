package nz.pumbas.halpbot.commands.events;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.PrivateChannel;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.GenericMessageEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.react.GenericMessageReactionEvent;

public class MessageEvent implements HalpbotEvent
{
    private final GenericMessageEvent event;

    public MessageEvent(GenericMessageEvent event) {
        this.event = event;
    }

    @Override
    public Object getRawEvent() {
        return this.event;
    }

    @Override
    public MessageChannel getMessageChannel() {
        return this.event.getChannel();
    }

    @Override
    public TextChannel getTextChannel() {
        return this.event.getTextChannel();
    }

    @Override
    public PrivateChannel getPrivateChannel() {
        return this.event.getPrivateChannel();
    }

    @Override
    public ChannelType getChannelType() {
        return this.event.getChannelType();
    }

    @Override
    public Guild getGuild() {
        return this.event.getGuild();
    }

    @Override
    public User getUser() {
        if (this.event instanceof MessageReceivedEvent)
            return ((MessageReceivedEvent) this.event).getAuthor();
        else if (this.event instanceof GenericMessageReactionEvent)
            return ((GenericMessageReactionEvent) this.event).getUser();
        throw new UnsupportedOperationException(
            "The event " + this.event.getClass().getSimpleName() + " doesn't support this operation");
    }

    @Override
    public JDA getJDA() {
        return this.event.getJDA();
    }
}
