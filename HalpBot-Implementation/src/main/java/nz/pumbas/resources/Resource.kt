package nz.pumbas.resources

import java.lang.IllegalArgumentException
import java.util.*

data class Resource(val key: String, val formatting: Array<out Any> = emptyArray() )
{
    private var resourceMappings: EnumMap<Language, String> =
        if (!ResourceManager.isRegistered(key)) {
            ResourceManager.add(key, this)
            EnumMap(Language::class.java)
        } else {
            ResourceManager.get(key).resourceMappings
        }

    companion object {

        @JvmStatic
        fun get(key: String, vararg formatting: Any): Resource {
            return ResourceManager.get(key, *formatting)
        }

        @JvmStatic
        fun getOrCreate(key: String, translation: String, language: Language): Resource {
            return ResourceManager.getOrCreate(key, translation, language)
        }
    }

    /**
     * Adds a translation for this [Resource] and returns itself.
     */
    fun addTranslation(language: Language, translation: String): Resource {
        resourceMappings[language] = translation
        return this
    }

    /**
     * Adds a [Map] of translations for this [Resource] and returns itself.
     */
    fun addTranslations(translations: Map<Language, String>): Resource {
        resourceMappings.putAll(translations)
        return this
    }

    /**
     * Retrieves a translation for a specified [Language] for this [Resource]. If there are formatting parameters, it
     * will automatically call [String.format].
     */
    fun getTranslation(language: Language): String {
        if (language in resourceMappings && null != resourceMappings[language]) {
            val translation = resourceMappings.getValue(language)
            return if (formatting.isEmpty()) translation else String.format(Locale.getDefault(), translation, *formatting)
        }

        throw IllegalArgumentException("The resource $key, doesn't have a translation for $language")
    }

    /**
     * Returns if this [Resource] has a translation for the specified [Language].
     */
    fun hasTranslation(language: Language): Boolean {
        return language in resourceMappings
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Resource

        if (key != other.key) return false

        return true
    }

    override fun hashCode(): Int {
        return key.hashCode()
    }
}

