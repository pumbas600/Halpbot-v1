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

import org.jetbrains.annotations.Nullable;

public class InternalEvent implements HalpbotEvent
{
    //TODO: Mock these objects?
    @Override
    public Object rawEvent() {
        throw new UnsupportedOperationException("This cannot be retrieved from an internal event");
    }

    @Override
    public MessageChannel messageChannel() {
        throw new UnsupportedOperationException("This cannot be retrieved from an internal event");
    }

    @Override
    public TextChannel textChannel() {
        throw new UnsupportedOperationException("This cannot be retrieved from an internal event");
    }

    @Override
    public PrivateChannel privateChannel() {
        throw new UnsupportedOperationException("This cannot be retrieved from an internal event");
    }

    @Override
    public @Nullable AbstractChannel channel() {
        throw new UnsupportedOperationException("This cannot be retrieved from an internal event");
    }

    @Override
    public ChannelType channelType() {
        return ChannelType.UNKNOWN;
    }

    @Override
    public @Nullable Guild guild() {
        throw new UnsupportedOperationException("This cannot be retrieved from an internal event");
    }

    @Override
    public User user() {
        throw new UnsupportedOperationException("This cannot be retrieved from an internal event");
    }

    @Override
    public JDA jda() {
        throw new UnsupportedOperationException("This cannot be retrieved from an internal event");
    }

    @Override
    public void reply(String message) {
    }

    @Override
    public void reply(MessageEmbed embed) {
    }

    @Override
    public void replyTemporarily(String message, long seconds) {
    }

    @Override
    public void replyTemporarily(MessageEmbed embed, long seconds) {
    }

    @Override
    public boolean isInternal() {
        return true;
    }
}
