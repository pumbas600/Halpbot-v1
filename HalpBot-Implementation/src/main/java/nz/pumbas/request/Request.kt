package nz.pumbas.request

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import nz.pumbas.utilities.enums.StatusCode
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class Request(url: String, requestMethod: RequestMethod = RequestMethod.GET) {

    val connection: HttpURLConnection = URL(url).openConnection() as HttpURLConnection

    init {
        connection.setRequestProperty("Content-Type", "application/json")
        connection.requestMethod = requestMethod.toString()
    }

    /**
     * Retrieves the [StatusCode] of the response
     */
    fun responseCode(): StatusCode {
        return StatusCode.of(connection.responseCode)
    }

    /**
     * Parses the response into the specified type.
     */
    inline fun <reified T> parseResponse(debug: Boolean = false): T {
        val json = InputStreamReader(connection.inputStream).readText()
        if (debug) println(json)
        return Json.decodeFromString(json)
    }
}