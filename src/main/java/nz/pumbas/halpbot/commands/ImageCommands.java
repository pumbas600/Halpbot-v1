package nz.pumbas.halpbot.commands;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.awt.Color;
import java.awt.image.BufferedImage;

import nz.pumbas.commands.Annotations.Command;
import nz.pumbas.commands.Annotations.CommandGroup;
import nz.pumbas.utilities.Utilities;
import nz.pumbas.utilities.io.ImageType;

@CommandGroup(defaultPrefix = "$")
public class ImageCommands
{
    public static final double LINE_THICKNESS = 4D;


    @Command(alias = "sine")
    public void onSine(MessageReceivedEvent event)
    {
        int width = 500;
        int height = 500;
        int halfHeight = height / 2;

        BufferedImage image = Utilities.generateImageByPosition(
            width, height, (x, y) -> {
                double value = halfHeight * Math.sin( 2 * Math.PI * x / width) + halfHeight;

                if (Utilities.approximatelyEqual(halfHeight, y, LINE_THICKNESS))
                    return Color.red.getRGB();

                return (Utilities.approximatelyEqual(value, y, LINE_THICKNESS))
                    ? Color.black.getRGB()
                    : Color.white.getRGB();
            });

        byte[] bytes = Utilities.toByteArray(image, ImageType.PNG);

        event.getChannel()
            .sendFile(bytes, "SineFunction.png")
            .queue();
    }
}
