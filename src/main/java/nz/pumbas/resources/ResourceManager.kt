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
                            get(translation.key, true, translation.value, language)
                        }
                    }
                }
            })
    }

    /**
     * Retrieves the [Resource] for the specified key. If the resource doesn't exist, then you can specify to create
     * a new resource for it.
     *
     * @param key
     *      The key for the resource
     * @param createIfAbsent
     *      Should the resource be created if it doesn't exist
     * @param language
     *      The language to be used if a resource needs to be created
     * @param translation
     *      The translation for the resource
     *
     * @return The resource for the specified key
     */
    fun get(key: String, createIfAbsent: Boolean = false,
            translation: String = "", language: Language = Language.EN_UK): Resource
    {
        if (key in translations && null != translations[key])
            return translations.getValue(key)
        else if (createIfAbsent) {
            val resource = Resource(key) //Automatically calls add(key, resource)
            resource.addTranslation(language, translation)
            return resource
        }
        throw IllegalArgumentException("There is no resource for the key $key")
    }

    fun add(key: String, resource: Resource) {
        if (key in translations)
            throw IllegalArgumentException("The key $key is already being used. Consider using addTranslation instead")
        translations[key] = resource
    }
}