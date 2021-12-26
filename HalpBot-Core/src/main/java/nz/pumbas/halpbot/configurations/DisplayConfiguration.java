package nz.pumbas.halpbot.configurations;

import net.dv8tion.jda.api.entities.MessageEmbed;

import java.time.Duration;

import nz.pumbas.halpbot.objects.DiscordString;
import nz.pumbas.halpbot.events.HalpbotEvent;

public interface DisplayConfiguration
{
    void display(HalpbotEvent event, String message);

    void display(HalpbotEvent event, MessageEmbed embed);

    default void display(HalpbotEvent event, Object object, Duration duration) {
        if (duration.isNegative())
            this.display(event, object);
        else
            this.displayTemporary(event, object, duration.getSeconds());
    }

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
