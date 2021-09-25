package nz.pumbas.halpbot.commands

import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.TextChannel
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.events.ReadyEvent
import nz.pumbas.halpbot.commands.annotations.Command
import nz.pumbas.halpbot.commands.annotations.Remaining
import nz.pumbas.halpbot.commands.annotations.Unrequired
import nz.pumbas.halpbot.commands.annotations.Implicit
import nz.pumbas.halpbot.Halpbot
import nz.pumbas.halpbot.customparameters.Joke
import nz.pumbas.halpbot.utilities.HalpbotUtils
import nz.pumbas.halpbot.request.Request

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
        comfortingMessages = HalpbotUtils.getAllLinesFromFile("ComfortingMessages.txt")
        insultJokes = HalpbotUtils.getAllLinesFromFile("InsultJokes.txt")
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
        return HalpbotUtils.randomChoice(listOf("Thank you!", "I try my best :)", "Don't worry about it"))
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

    @Command(alias = "Creator", description = "Creator only command :eyes:", restrictedTo = [Halpbot.CREATOR_ID])
    fun creator(): String {
        return "Hello there creator :wave:"
    }

    @Command(alias = "Comfort", description = "Sends a comforting message")
    fun comfort(): String {
        return HalpbotUtils.randomChoice(this.comfortingMessages)
    }

    @Command(alias = "Joke", description = "Sends a random joke")
    fun joke(@Unrequired("") category: String): String {
        var loweredCategory = category.lowercase()
        if (loweredCategory.isNotEmpty() && loweredCategory !in jokeCategories)
            return "You can only specify the one of the following categories: ${jokeCategories.contentToString()}"
        else if (loweredCategory.isEmpty())
            loweredCategory = HalpbotUtils.randomChoice(jokeCategories)

        val url = "https://official-joke-api.appspot.com/jokes/$loweredCategory/random"
        val request = Request(url)

        if (!request.responseCode().isSuccessful)
            return "Jokes on you - there was an error trying to contact the API!"

        val joke = request.parseResponse<List<Joke>>()[0].toString()
        return joke.replace("â€™", "'")
    }

    @Command(alias = "Insult", description = "Sends a joking insult")
    fun insult(): String {
        return HalpbotUtils.randomChoice(insultJokes)
    }

    @Command(alias = "List", description = "Creates a simple list of integers")
    fun list(@Implicit list: List<Int>): List<Int> {
        return list
    }
}