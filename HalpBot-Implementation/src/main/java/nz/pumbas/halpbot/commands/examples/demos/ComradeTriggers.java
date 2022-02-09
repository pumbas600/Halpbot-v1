package nz.pumbas.halpbot.commands.examples.demos;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import org.dockbox.hartshorn.core.annotations.stereotype.Service;

import java.util.Locale;
import java.util.regex.Pattern;

import nz.pumbas.halpbot.triggers.Trigger;
import nz.pumbas.halpbot.triggers.TriggerStrategy;

@Service
public class ComradeTriggers
{
    private static final Pattern MY_PATTERN = Pattern.compile("my", Pattern.CASE_INSENSITIVE);

    @Trigger(value = "my", strategy = TriggerStrategy.ANYWHERE)
    public String myTrigger(MessageReceivedEvent event) {
        String message = event.getMessage().getContentRaw();

        // As this method has been triggered, we know that 'my' is present in this message so index will never be -1
        int index = message.toLowerCase(Locale.ROOT).indexOf("my");
        String remainingMessage = message.substring(index);

        return "You mean, " + MY_PATTERN.matcher(remainingMessage).replaceAll("***our***");
    }
}
