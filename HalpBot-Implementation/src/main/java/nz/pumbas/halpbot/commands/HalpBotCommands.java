package nz.pumbas.halpbot.commands;

import net.dv8tion.jda.api.Permission;

import nz.pumbas.halpbot.commands.annotations.Command;
import nz.pumbas.halpbot.commands.annotations.Implicit;

public class HalpBotCommands
{

    @Command(alias = "source")
    public String source() {
        return "You can see the source code for me here: https://github.com/pumbas600/HalpBot :kissing_heart:";
    }

    @Command(alias = "suggestion")
    public String suggestion() {
        return "You can note issues and suggestions for me here: https://github.com/pumbas600/HalpBot/issues";
    }

    @Command(alias = "choose", description = "Randomly chooses one of the items")
    public String choose(@Implicit String[] choices) {
        // Use of @Implicit means that its not necessary to surround the choices with [...]
        return choices[(int)(Math.random() * choices.length)];
    }

    @Command(alias = "permission",
             description = "Tests command permissions (If you have permission to view audit logs)",
             permissions = Permission.VIEW_AUDIT_LOGS)
    public String permission() {
        return "You have permission to use this command!";
    }
}
