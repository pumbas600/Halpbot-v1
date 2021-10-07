package nz.pumbas.halpbot.events;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.PrivateChannel;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.Interaction;
import net.dv8tion.jda.api.requests.restaction.interactions.ReplyAction;

import java.util.concurrent.TimeUnit;

public class InteractionEvent implements HalpbotEvent
{
    private final Interaction interaction;

    public InteractionEvent(Interaction interaction) {
        this.interaction = interaction;
    }

    @Override
    public Object getRawEvent() {
        return this.interaction;
    }

    @Override
    public MessageChannel getMessageChannel() {
        return this.interaction.getMessageChannel();
    }

    @Override
    public TextChannel getTextChannel() {
        return this.interaction.getTextChannel();
    }

    @Override
    public PrivateChannel getPrivateChannel() {
        return this.interaction.getPrivateChannel();
    }

    @Override
    public ChannelType getChannelType() {
        return this.interaction.getChannelType();
    }

    @Override
    public Guild getGuild() {
        return this.interaction.getGuild();
    }

    @Override
    public User getUser() {
        return this.interaction.getUser();
    }

    @Override
    public JDA getJDA() {
        return this.interaction.getChannel().getJDA();
    }

    @Override
    public void reply(String message) {
        this.interaction.reply(message).queue();
    }

    @Override
    public void reply(MessageEmbed embed) {
        this.interaction.replyEmbeds(embed).queue();
    }

    /**
     * Replies temporarily by deleting the response after the specified number of seconds. If the duration is less
     * than 1 second, it will set the reply to ephemeral.
     *
     * @param message
     *      The message to send
     * @param seconds
     *      The number of seconds to wait before deleting the response
     */
    @Override
    public void replyTemporarily(String message, long seconds) {
        ReplyAction replyAction = this.interaction.reply(message);
        if (1 > seconds)
            replyAction.setEphemeral(true).queue();
        else replyAction.queue(
            m -> m.deleteOriginal().queueAfter(seconds, TimeUnit.SECONDS));
    }

    /**
     * Replies temporarily by deleting the response after the specified number of seconds. If the duration is less
     * than 1 second, it will set the reply to ephemeral.
     *
     * @param embed
     *      The embed to send
     * @param seconds
     *      The number of seconds to wait before deleting the response
     */
    @Override
    public void replyTemporarily(MessageEmbed embed, long seconds) {
        ReplyAction replyAction = this.interaction.replyEmbeds(embed);
        if (1 > seconds)
            replyAction.setEphemeral(true).queue();
        else replyAction.queue(
            m -> m.deleteOriginal().queueAfter(seconds, TimeUnit.SECONDS));

    }
}
