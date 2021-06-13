package nz.pumbas.resources

import nz.pumbas.utilities.Utilities
import java.io.File
import java.lang.IllegalArgumentException
import java.util.*
import java.util.function.Consumer
import kotlin.collections.HashMap

object ResourceManager {

    private var translations = HashMap<String, Resource>()

    /**
     * On initialisation, it will automatically search the translations directory for property files and load the
     * resources from them.
     */
    init {
        Utilities.retrieveResourcePath("translations")
            .ifPresent(Consumer<String>
            {
                val files = File(it).listFiles() ?: return@Consumer

                for (file in files) {
                    if (file.name.startsWith("translations_") && file.name.endsWith(".properties")) {
                        val language = Language.valueOf(file.name.removePrefix("translations_").removeSuffix(".properties").uppercase())

                        val translations = Utilities.parsePropertyFile("translations/${file.name}")
                        for (translation in translations) {
                            createOrAdd(translation.key, translation.value, language)
                        }
                    }
                }
            })
    }

    /**
     * Creates the [Resource] for the specified key. If the resource already exists, then it automatically adds it as a
     * translation.
     *
     * @param key
     *      The key for the resource
     * @param language
     *      The language
     * @param translation
     *      The translation for the resource
     */
    private fun createOrAdd(key: String, translation: String = "", language: Language = Language.EN_UK)
    {
        val resource: Resource = if (isRegistered(key)) {
            translations.getValue(key)
        } else  {
            Resource(key) //Automatically calls add(key, resource)
        }

        resource.addTranslation(language, translation)
    }

    /**
     * Registers the [Resource] under the specified key. If the key is already being used, this will throw an
     * [IllegalArgumentException].
     */
    fun add(key: String, resource: Resource) {
        if (isRegistered(key))
            throw IllegalArgumentException("The key $key is already being used. Consider using addTranslation instead")
        translations[key] = resource
    }

    /**
     * Retrieves the [Resource] for the specified key. If there is no resource for that key, an
     * [IllegalArgumentException] will be thrown.
     */
    fun get(key: String): Resource {
        if (isRegistered(key))
            return translations.getValue(key)
        throw IllegalArgumentException("There is no resource defined for the key $key")
    }

    /**
     * Retrieves the [Resource] for the specified key. If there is no resource for that key, an
     * [IllegalArgumentException] will be thrown. If formating objects have been specified, a
     * shallow copy of the [Resource] is returned with the specified formatting.
     */
    fun get(key: String, vararg formatting: Any): Resource {
        return if (formatting.isEmpty()) get(key) else Resource(key, formatting)
    }

    /**
     * Will retrieve the [Resource] for the specified key. If there is no resource for that key a new resource will be
     * created and the translation added. Alternatively, if the resource exists but there is no translation for the
     * specified language, then it will automatically add the translation for that language to that resource.
     */
    fun getOrCreate(key: String, translation: String, language: Language): Resource {
        if (!isRegistered(key) || translations.getValue(key).hasTranslation(language))
            createOrAdd(key, translation, language)

        return get(key)
    }

    /**
     * Returns if the key has been registered by the resource manager
     */
    fun isRegistered(key: String): Boolean {
        return key in translations
    }
}