package nz.pumbas.halpbot.commands

import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.MessageChannel
import net.dv8tion.jda.api.entities.TextChannel
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.events.ReadyEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import nz.pumbas.commands.OnReady
import nz.pumbas.commands.annotations.Command
import nz.pumbas.commands.annotations.Remaining
import nz.pumbas.commands.annotations.Source
import nz.pumbas.commands.annotations.Unrequired
import nz.pumbas.commands.commandadapters.AbstractCommandAdapter
import nz.pumbas.halpbot.HalpBot
import nz.pumbas.halpbot.customparameters.Joke
import nz.pumbas.utilities.Utils
import nz.pumbas.utilities.request.Request
import java.awt.Color

class KotlinCommands : OnReady {

    private lateinit var comfortingMessages: List<String>
    private lateinit var insultJokes: List<String>

    private val jokeCategories = arrayOf("knock-knock", "general", "programming")

    /**
     * A method that is called once after the bot has been initialised.
     *
     * @param event
     *      The JDA [ReadyEvent].
     */
    override fun onReady(event: ReadyEvent) {
        comfortingMessages = Utils.getAllLinesFromFile("ComfortingMessages.txt")
        insultJokes = Utils.getAllLinesFromFile("InsultJokes.txt")
    }

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
                    .append(command.value.displayCommand.ifEmpty { "N/A" })
                    .append("\n**Description**\n")
                    .append(command.value.description.ifEmpty { "N/A" })

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

    @Command(alias = "Repeat", description = "Repeats back what you said")
    fun repeat(@Remaining text: String): String {
        return text
    }

    @Command(alias = "Channel", description = "Returns the text channel specified")
    fun channel(channel: TextChannel): TextChannel {
        return channel
    }

    @Command(alias = "Member", description = "Returns the member specified")
    fun member(member: Member): Member {
        return member
    }

    @Command(alias = "User", description = "Returns the user specified")
    fun user(user: User): User {
        return user
    }

    @Command(alias = "GoodBot", description = "Allows you to praise the bot.")
    fun goodBot(): String {
        return Utils.randomChoice(listOf("Thank you!", "I try my best :)", "Don't worry about it"))
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

    @Command(alias = "Is", command = "#Integer <in> #Integer[]",
            description = "Tests if the element is contained within the array")
    fun kotlinTesting(num: Int, @Unrequired("[]") array: Array<Int>?): String {
        return if (null != array && num in array) "That number is in the array! :tada:"
        else "Sorry, it seems that number isn't in the array. :point_right: :point_left:"
    }

    @Command(alias = "Creator", description = "Creator only command :eyes:", restrictedTo = [HalpBot.CREATOR_ID])
    fun creator(): String {
        return "Hello there creator :wave:"
    }

    @Command(alias = "Comfort", description = "Sends a comforting message")
    fun comfort(): String {
        return Utils.randomChoice(this.comfortingMessages)
    }

    @Command(alias = "Joke", description = "Sends a random joke")
    fun joke(@Unrequired("") category: String): String {
        var loweredCategory = category.lowercase()
        if (loweredCategory.isNotEmpty() && loweredCategory !in jokeCategories)
            return "You can only specify the one of the following categories: ${jokeCategories.contentToString()}"
        else if (loweredCategory.isEmpty())
            loweredCategory = Utils.randomChoice(jokeCategories)

        val url = "https://official-joke-api.appspot.com/jokes/$loweredCategory/random"
        val request = Request(url)

        if (!request.responseCode().isSuccessful)
            return "Jokes on you - there was an error trying to contact the API!"

        val joke = request.parseResponse<List<Joke>>()[0].toString()
        return joke.replace("â€™", "'")
    }

    @Command(alias = "Insult", description = "Sends a joking insult")
    fun insult(): String {
        return Utils.randomChoice(insultJokes)
    }
}