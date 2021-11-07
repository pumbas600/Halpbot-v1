package nz.pumbas.halpbot.commands;

import net.dv8tion.jda.api.events.ReadyEvent;

import org.jetbrains.annotations.NotNull;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.util.List;

import nz.pumbas.halpbot.commands.annotations.Command;
import nz.pumbas.halpbot.commands.annotations.Unrequired;
import nz.pumbas.halpbot.utilities.ErrorManager;
import nz.pumbas.halpbot.utilities.HalpbotUtils;

public class SimpleCommands implements OnReady
{
    private List<String> comfortingMessages;
    private List<String> insultJokes;

    public void onReady(@NotNull ReadyEvent event) {
        try {
            this.comfortingMessages =
                HalpbotUtils.getAllLinesFromFile((new ClassPathResource("static/ComfortingMessages.txt")).getInputStream());
            this.insultJokes =
                HalpbotUtils.getAllLinesFromFile((new ClassPathResource("static/InsultJokes.txt")).getInputStream());
        } catch (IOException e) {
            ErrorManager.handle(e);
        }
    }

    @Command(description = "Allows you to praise the bot")
    public String goodBot() {
        return HalpbotUtils.randomChoice(new String[]{ "Thank you!", "I try my best :)", ":heart:" });
    }

    @Command(alias = "Is", command = "#Integer <in> #List",
             description = "Tests if the element is contained within the array")
    public String testing(int num, @Unrequired("[]") List<Integer> array) {
        return array.contains(num)
            ? "That number is in the array! :tada:"
            : "Sorry, it seems that number isn't in the array. :point_right: :point_left:";
    }

    @Command(description = "Sends a comforting message")
    public String comfort() {
        return HalpbotUtils.randomChoice(this.comfortingMessages);
    }

    @Command(description = "Sends a joking insult")
    public String insult() {
        return HalpbotUtils.randomChoice(this.insultJokes);
    }
}

