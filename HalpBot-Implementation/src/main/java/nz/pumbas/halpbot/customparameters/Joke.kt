package nz.pumbas.halpbot.customparameters

import kotlinx.serialization.Serializable

@Serializable
data class Joke(val id: Int, val type: String, val setup: String, val punchline: String) {

    override fun toString(): String {
        return setup + "\n" + punchline
    }
}