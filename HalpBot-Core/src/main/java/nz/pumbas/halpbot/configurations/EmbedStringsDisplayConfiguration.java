package nz.pumbas.halpbot.configurations;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.message.GenericMessageEvent;
import net.dv8tion.jda.api.interactions.Interaction;

import java.awt.Color;

import nz.pumbas.halpbot.utilities.HalpbotUtils;

public class EmbedStringsDisplayConfiguration implements DisplayConfiguration
{
    private Color embedColour = Color.YELLOW;

    public EmbedStringsDisplayConfiguration() { }

    public EmbedStringsDisplayConfiguration(Color embedColour) {
        this.embedColour = embedColour;
    }

    @Override
    public void display(GenericMessageEvent event, String message) {
        this.display(event, this.createEmbed(message));
    }

    @Override
    public void display(GenericMessageEvent event, MessageEmbed embed) {
        event.getChannel().sendMessageEmbeds(embed).queue();
    }

    @Override
    public void display(Interaction interaction, String message) {
        this.display(interaction, this.createEmbed(message));
    }

    @Override
    public void display(Interaction interaction, MessageEmbed embed) {
        interaction.replyEmbeds(embed);
    }

    private MessageEmbed createEmbed(String message) {
        EmbedBuilder builder = new EmbedBuilder();
        builder.setDescription(HalpbotUtils.checkEmbedDesciptionLength(message));
        builder.setColor(this.embedColour);
        return builder.build();
    }
}
