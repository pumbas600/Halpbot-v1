package nz.pumbas.halpbot.configurations;

import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.message.GenericMessageEvent;
import net.dv8tion.jda.api.interactions.Interaction;

public interface DisplayConfiguration
{
    void display(GenericMessageEvent event, String message);

    void display(GenericMessageEvent event, MessageEmbed embed);

    void display(Interaction interaction, String message);

    void display(Interaction interaction, MessageEmbed embed);
}
