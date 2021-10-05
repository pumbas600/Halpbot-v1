package nz.pumbas.halpbot.configurations;

import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.message.GenericMessageEvent;
import net.dv8tion.jda.api.interactions.Interaction;

import nz.pumbas.halpbot.commands.DiscordString;
import nz.pumbas.halpbot.commands.events.HalpbotEvent;

public interface DisplayConfiguration
{
    void display(HalpbotEvent event, String message);

    void display(HalpbotEvent event, MessageEmbed embed);

    default void display(HalpbotEvent event, Object object) {
        if (object instanceof MessageEmbed) {
            this.display(event, (MessageEmbed) object);
        }
        else {
            String message = object instanceof DiscordString
                ? ((DiscordString) object).toDiscordString()
                : object.toString();
            this.display(event, message);
        }
    }

    void displayTemporary(HalpbotEvent event, String message, long seconds);

    void displayTemporary(HalpbotEvent event, MessageEmbed embed, long seconds);

    default void displayTemporary(HalpbotEvent event, Object object, long seconds) {
        if (object instanceof MessageEmbed) {
            this.displayTemporary(event, (MessageEmbed) object, seconds);
        }
        else {
            String message = object instanceof DiscordString
                ? ((DiscordString) object).toDiscordString()
                : object.toString();
            this.displayTemporary(event, message, seconds);
        }
    }
}
