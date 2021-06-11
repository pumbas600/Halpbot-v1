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
            return ResourceManager.getOrCreate(key, translation, language)
        }
    }

    /**
     * Adds a translation for this [Resource] and returns itself.
     */
    fun addTranslation(language: Language, translation: String) : Resource {
        resourceMappings[language] = translation
        return this
    }

    /**
     * Adds a [Map] of translations for this [Resource] and returns itself.
     */
    fun addTranslations(translations: Map<Language, String>) : Resource {
        resourceMappings.putAll(translations)
        return this
    }

    /**
     * Retrieves a translation for a specified [Language] for this [Resource].
     */
    fun getTranslation(language: Language): String {
        if (language in resourceMappings && null != resourceMappings[language])
            //Retrieves non-null values, otherwise throws NoSuchElementException
            return resourceMappings.getValue(language)

        throw IllegalArgumentException("The resource $key, doesn't have a translation for $language")
    }

    /**
     * Returns if this [Resource] has a translation for the specified [Language].
     */
    fun hasTranslation(language: Language): Boolean {
        return language in resourceMappings
    }
}

