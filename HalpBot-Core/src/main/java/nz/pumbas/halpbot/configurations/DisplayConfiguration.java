package nz.pumbas.halpbot.configurations;

import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.message.GenericMessageEvent;
import net.dv8tion.jda.api.interactions.Interaction;

import nz.pumbas.halpbot.commands.events.HalpbotEvent;

public interface DisplayConfiguration
{
    void display(GenericMessageEvent event, String message);

    void display(GenericMessageEvent event, MessageEmbed embed);

    void display(Interaction interaction, String message);

    void display(Interaction interaction, MessageEmbed embed);

    void display(HalpbotEvent event, Object message);

    void displayTemporary(HalpbotEvent event, Object message);
}
