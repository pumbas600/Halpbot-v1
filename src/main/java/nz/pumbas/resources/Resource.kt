package nz.pumbas.resources

import java.lang.IllegalArgumentException
import java.util.*

data class Resource(val key: String)
{
    private var resourceMappings = HashMap<Language, String>()

    init {
        ResourceManager.add(key, this)
    }

    companion object {

        @JvmStatic
        fun get(key: String) : Resource {
            return ResourceManager.get(key)
        }

        @JvmStatic
        fun getOrCreate(key: String, translation: String, language: Language, ) : Resource {
            return ResourceManager.get(key, true, translation, language)
        }
    }

    /**
     * Adds a translation for this [Resource].
     *
     * @param language
     *      The [Language] of the translation
     * @param translation
     *      The translation
     */
    fun addTranslation(language: Language, translation: String) {
        resourceMappings[language] = translation
    }

    /**
     * Adds a [Map] of translations for this [Resource].
     *
     * @param translations
     *      The map of translations
     */
    fun addTranslations(translations: Map<Language, String>) {
        resourceMappings.putAll(translations)
    }

    /**
     * Retrieves a translation for a specified [Language] for this [Resource].
     *
     * @param language
     *      The language you want the translation in
     *
     * @return The translation for this resource in the specified language
     */
    fun getTranslation(language: Language): String {
        if (language in resourceMappings && null != resourceMappings[language])
            //Retrieves non-null values, otherwise throws NoSuchElementException
            return resourceMappings.getValue(language)

        throw IllegalArgumentException("The resource $key, doesn't have a translation for $language")
    }
}

