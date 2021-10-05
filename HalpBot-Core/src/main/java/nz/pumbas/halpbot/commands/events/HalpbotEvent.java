package nz.pumbas.halpbot.commands.events;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.PrivateChannel;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;

import nz.pumbas.halpbot.objects.Exceptional;

public interface HalpbotEvent
{
    Object getRawEvent();

    @SuppressWarnings("unchecked")
    default <T> T getEvent(Class<T> type) {
        if (type.isAssignableFrom(this.getRawEvent().getClass())) {
            return (T) this.getRawEvent();
        }
        throw new UnsupportedOperationException("The raw event is not of the specified type: " + type.getSimpleName());
    }

    default <T> Exceptional<T> safelyGetEvent(Class<T> type) {
        return Exceptional.of(() -> this.getEvent(type));
    }

    MessageChannel getMessageChannel();

    TextChannel getTextChannel();

    PrivateChannel getPrivateChannel();
    
    ChannelType getChannelType();

    Guild getGuild();

    User getUser();

    JDA getJDA();
}
