package nz.pumbas.halpbot.configurations;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

import java.awt.Color;

import nz.pumbas.halpbot.events.HalpbotEvent;
import nz.pumbas.halpbot.utilities.HalpbotUtils;

public class EmbedStringsDisplayConfiguration implements DisplayConfiguration
{
    @Override
    public void display(HalpbotEvent event, String message) {
        this.display(event, this.createEmbed(message));
    }

    @Override
    public void display(HalpbotEvent event, MessageEmbed embed) {
        event.reply(embed);
    }

    @Override
    public void displayTemporary(HalpbotEvent event, String message, long seconds) {
        this.displayTemporary(event, this.createEmbed(message), seconds);
    }

    @Override
    public void displayTemporary(HalpbotEvent event, MessageEmbed embed, long seconds) {
        event.replyTemporarily(embed, seconds);
    }

    private MessageEmbed createEmbed(String message) {
        EmbedBuilder builder = new EmbedBuilder();
        builder.setDescription(HalpbotUtils.checkEmbedDesciptionLength(message));
        builder.setColor(Color.ORANGE);
        return builder.build();
    }
}
