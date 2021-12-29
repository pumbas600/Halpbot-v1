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

import org.dockbox.hartshorn.core.domain.Exceptional;
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

    default <T> Exceptional<T> eventSafely(Class<T> type) {
        return Exceptional.of(() -> this.event(type));
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
