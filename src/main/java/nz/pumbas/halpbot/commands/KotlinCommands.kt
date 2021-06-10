package nz.pumbas.halpbot.commands

import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.User
import nz.pumbas.commands.annotations.Command
import nz.pumbas.commands.annotations.Unrequired
import nz.pumbas.commands.commandadapters.AbstractCommandAdapter
import nz.pumbas.halpbot.HalpBot
import java.awt.Color

class KotlinCommands {

    @Command(alias = "Halp", description = "Displays the help information for the specified command")
    fun halp(commandAdapter: AbstractCommandAdapter, @Unrequired commandAlias: String): Any {
        if (commandAlias.isEmpty()) {
            val embedBuilder = EmbedBuilder()
                .setColor(Color.ORANGE)
                .setTitle("HALP - Commands")

            val registeredCommands = commandAdapter.registeredCommands;
            val stringBuilder = StringBuilder()
            for (command in registeredCommands) {
                stringBuilder.append("\n**Usage**\n")
                    .append(if (command.value.displayCommand?.isEmpty() == true) "N/A" else command.value.displayCommand)
                    .append("\n**Description**\n")
                    .append(if (command.value.description?.isEmpty() == true) "N/A" else command.value.description)

                embedBuilder.addField(command.key, stringBuilder.toString(), true)
                stringBuilder.clear()
            }

            return embedBuilder.build()
        }

        var alias = commandAlias.lowercase();
        if (!alias.startsWith(commandAdapter.commandPrefix))
            alias = commandAdapter.commandPrefix + alias

        val commandMethod = commandAdapter.getCommandMethod(alias)
        if (commandMethod.isEmpty)
            return "That doesn't seem to be a registered command :sob:"

        return AbstractCommandAdapter.buildHelpMessage(alias, commandMethod.get(), "Here's the overview")
    }

    @Command(alias = "Id", description = "Returns the users discord id")
    fun id(author: User): String {
        return author.id
    }

    @Command(alias = "Calc", description = "Simple calculation operations in Kotlin")
    fun kotlinCalculator(num1: Double, operator: Char, num2: Double): Any {
        return when (operator) {
            '+'  -> num1 + num2
            '-'  -> num1 - num2
            '/'  -> if (0.0 != num2) num1 / num2 else "You can't divide by 0!"
            '*'  -> num1 * num2
            else -> "That's an unsupported operator sorry."
        }
    }

    @Command(alias = "Is", command = "#Int <in> #Int[]",
            description = "Tests if the element is contained within the array")
    fun kotlinTesting(num: Int, @Unrequired("[]") array: Array<Int>?): String {
        return if (null != array && num in array) "That number is in the array! :tada:"
        else "Sorry, it seems that number isn't in the array. :point_right: :point_left:"
    }

    @Command(alias = "Creator", description = "Creator only command :eyes:", restrictedTo = [HalpBot.CREATOR_ID])
    fun creator(): String {
        return "Hello there creator :wave:"
    }
}