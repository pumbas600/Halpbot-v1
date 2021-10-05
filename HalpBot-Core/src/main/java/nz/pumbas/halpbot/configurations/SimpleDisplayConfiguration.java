package nz.pumbas.halpbot.configurations;

import net.dv8tion.jda.api.entities.MessageEmbed;

import nz.pumbas.halpbot.commands.events.HalpbotEvent;

public class SimpleDisplayConfiguration implements DisplayConfiguration
{
    @Override
    public void display(HalpbotEvent event, String message) {
        event.reply(message);
    }

    @Override
    public void display(HalpbotEvent event, MessageEmbed embed) {
        event.reply(embed);
    }

    @Override
    public void displayTemporary(HalpbotEvent event, String message, long seconds) {
        event.replyTemporarily(message, seconds);
    }

    @Override
    public void displayTemporary(HalpbotEvent event, MessageEmbed embed, long seconds) {
        event.replyTemporarily(embed, seconds);
    }
}
