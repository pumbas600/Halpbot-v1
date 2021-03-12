package nz.pumbas;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import nz.pumbas.commands.Annotations.Command;
import nz.pumbas.commands.Annotations.CommandGroup;

@CommandGroup(defaultPrefix = "!")
public class HalpBotCommands
{

    @Command(alias = "halp")
    public void onHalp(MessageReceivedEvent event) {
        event.getChannel().sendMessage("I will try my very best!").queue();
    }

}
