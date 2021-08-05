package nz.pumbas.commands;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.awt.Color;

import nz.pumbas.commands.exceptions.ErrorMessageException;
import nz.pumbas.commands.exceptions.UnimplementedFeatureException;

public final class ErrorManager
{
    private ErrorManager() {}

    public static void handle(Throwable e)
    {
        handle(null, e, null);
    }

    public static void handle(MessageReceivedEvent event, Throwable e)
    {
        handle(event, e, null);
    }

    public static void handle(Throwable e, String message)
    {
        handle(null, e, message);
    }

    public static void handle(MessageReceivedEvent event, Throwable e, String message)
    {
        if (null != message)
            System.out.println(message);

        if (e instanceof UnimplementedFeatureException) {
            unimplementedFeatureEmbed(event, e.getMessage());
        } else if (e instanceof ErrorMessageException) {
            event.getChannel().sendMessage(":warning: " + e.getMessage()).queue();
        } else e.printStackTrace();
    }

    public static void unimplementedFeatureEmbed(MessageReceivedEvent event, String message)
    {
        event.getChannel().sendMessageEmbeds(
            new EmbedBuilder().setTitle(":confounded: Sorry...")
                .setColor(Color.red)
                .addField("This feature is not implemented yet", message, false)
                .build())
            .queue();
    }
}
