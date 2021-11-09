package nz.pumbas.halpbot.events;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.PrivateChannel;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;

import org.dockbox.hartshorn.core.domain.Exceptional;

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

    void reply(String message);

    void reply(MessageEmbed embed);

    /**
     * Replies temporarily by deleting the response after the specified number of seconds. If the duration is less
     * than 1 second, it will set the reply to ephemeral for an {@link InteractionEvent}, however, if it's not an
     * interation event, then this will cause an {@link IllegalArgumentException} to be thrown.
     *
     * @param message
     *      The message to send
     * @param seconds
     *      The number of seconds to wait before deleting the response
     */
    void replyTemporarily(String message, long seconds);

    /**
     * Replies temporarily by deleting the response after the specified number of seconds. If the duration is less
     * than 1 second, it will set the reply to ephemeral for an {@link InteractionEvent}, however, if it's not an
     * interation event, then this will cause an {@link IllegalArgumentException} to be thrown.
     *
     * @param embed
     *      The embed to send
     * @param seconds
     *      The number of seconds to wait before deleting the response
     */
    void replyTemporarily(MessageEmbed embed, long seconds);
}
