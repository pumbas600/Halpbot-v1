package nz.pumbas.halpbot.configurations;

import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.requests.restaction.MessageAction;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

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
        if (object instanceof RestAction<?> restAction)
            restAction.queue();
        else if (object instanceof MessageEmbed messageEmbed)
            this.display(event, messageEmbed);
        else if (object instanceof DiscordString discordString)
            this.display(event, discordString.toDiscordString());
        else
            this.display(event, object.toString());
    }

    void displayTemporary(HalpbotEvent event, String message, long seconds);

    void displayTemporary(HalpbotEvent event, MessageEmbed embed, long seconds);

    default void displayTemporary(HalpbotEvent event, Object object, long seconds) {
        if (object instanceof MessageAction action)
            action.queue((m) -> m.delete().queueAfter(seconds, TimeUnit.SECONDS));
        else if (object instanceof RestAction<?> action)
            action.queue();
        else if (object instanceof MessageEmbed messageEmbed)
            this.displayTemporary(event, messageEmbed, seconds);
        else if (object instanceof DiscordString discordString)
            this.displayTemporary(event, discordString.toDiscordString(), seconds);
        else
            this.displayTemporary(event, object.toString(), seconds);
    }
}
