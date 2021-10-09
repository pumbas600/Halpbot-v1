package nz.pumbas.halpbot.commands

import net.dv8tion.jda.api.events.ReadyEvent
import nz.pumbas.halpbot.commands.annotations.Command
import nz.pumbas.halpbot.commands.annotations.Unrequired
import nz.pumbas.halpbot.permissions.HalpbotPermissions
import nz.pumbas.halpbot.utilities.HalpbotUtils
import org.springframework.core.io.ClassPathResource

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
        comfortingMessages = HalpbotUtils.getAllLinesFromFile(ClassPathResource("static/ComfortingMessages.txt").inputStream)
        insultJokes = HalpbotUtils.getAllLinesFromFile(ClassPathResource("static/InsultJokes.txt").inputStream)
    }

    @Command(alias = "GoodBot", description = "Allows you to praise the bot.")
    fun goodBot(): String {
        return HalpbotUtils.randomChoice(listOf("Thank you!", "I try my best :)", ":heart:"))
    }

    @Command(alias = "Is", command = "#Integer <in> #Integer[]",
            description = "Tests if the element is contained within the array")
    fun kotlinTesting(num: Int, @Unrequired("[]") array: Array<Int>?): String {
        return if (null != array && num in array) "That number is in the array! :tada:"
        else "Sorry, it seems that number isn't in the array. :point_right: :point_left:"
    }

    @Command(alias = "Creator", description = "Creator only command :eyes:", permissions = [HalpbotPermissions.BOT_OWNER])
    fun creator(): String {
        return "Hello there creator :wave:"
    }

    @Command(alias = "Comfort", description = "Sends a comforting message")
    fun comfort(): String {
        return HalpbotUtils.randomChoice(this.comfortingMessages)
    }
//
//    @Command(alias = "Joke", description = "Sends a random joke")
//    fun joke(@Unrequired("") category: String): String {
//        var loweredCategory = category.lowercase()
//        if (loweredCategory.isNotEmpty() && loweredCategory !in jokeCategories)
//            return "You can only specify the one of the following categories: ${jokeCategories.contentToString()}"
//        else if (loweredCategory.isEmpty())
//            loweredCategory = HalpbotUtils.randomChoice(jokeCategories)
//
//        val url = "https://official-joke-api.appspot.com/jokes/$loweredCategory/random"
//        val request = Request(url)
//
//        if (!request.responseCode().isSuccessful)
//            return "Jokes on you - there was an error trying to contact the API!"
//
//        val joke = request.parseResponse<List<Joke>>()[0].toString()
//        return joke.replace("â€™", "'")
//    }

    @Command(alias = "Insult", description = "Sends a joking insult")
    fun insult(): String {
        return HalpbotUtils.randomChoice(insultJokes)
    }
}