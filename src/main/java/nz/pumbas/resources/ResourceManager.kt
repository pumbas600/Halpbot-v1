package nz.pumbas.resources

import nz.pumbas.utilities.Utilities
import java.io.File
import java.lang.IllegalArgumentException
import java.util.*
import java.util.function.Consumer
import kotlin.collections.HashMap

object ResourceManager {

    private var translations = HashMap<String, Resource>()

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
        val resource: Resource = if (key in translations) {
            translations.getValue(key)
        } else  {
            Resource(key) //Automatically calls add(key, resource)
        }

        resource.addTranslation(language, translation)
    }

    fun add(key: String, resource: Resource) {
        if (key in translations)
            throw IllegalArgumentException("The key $key is already being used. Consider using addTranslation instead")
        translations[key] = resource
    }

    fun get(key: String) : Resource {
        if (key in translations)
            return translations.getValue(key)
        throw IllegalArgumentException("There is no resource defined for the key $key")
    }

    fun getOrCreate(key: String, translation: String, language: Language) : Resource {
        if (key !in translations || translations.getValue(key).hasTranslation(language))
            createOrAdd(key, translation, language)

        return get(key)
    }
}