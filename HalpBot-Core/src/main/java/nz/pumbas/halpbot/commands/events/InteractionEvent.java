package nz.pumbas.halpbot.commands.events;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.PrivateChannel;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.Interaction;

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
}
