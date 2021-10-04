package nz.pumbas.halpbot.configurations;

import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.message.GenericMessageEvent;
import net.dv8tion.jda.api.interactions.Interaction;

public class SimpleDisplayConfiguration implements DisplayConfiguration
{
    @Override
    public void display(GenericMessageEvent event, String message) {
        event.getChannel().sendMessage(message).queue();
    }

    @Override
    public void display(GenericMessageEvent event, MessageEmbed embed) {
        event.getChannel().sendMessageEmbeds(embed).queue();
    }

    @Override
    public void display(Interaction interaction, String message) {
        interaction.reply(message).queue();
    }

    @Override
    public void display(Interaction interaction, MessageEmbed embed) {
        interaction.replyEmbeds(embed).queue();
    }
}
