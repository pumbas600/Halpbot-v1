package nz.pumbas.resources;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ResourceTests
{

    @Test
    public void loadingTranslationsTest() {
        Resource resource = Resource.get("halpbot.test");
        Assertions.assertEquals("This is a test resource", resource.getTranslation(Language.EN_UK));
    }

    @Test
    public void automaticResourceRegistrationTest() {
        Resource resource = Resource.getOrCreate("halpbot.test.1", "This is a test :)", Language.EN_UK);
        Assertions.assertEquals(resource, Resource.get("halpbot.test.1"));
    }

    @Test
    public void formattingTranslationsTest() {
        Resource raw = Resource.get("halpbot.test.formatting");
        Resource formatted = Resource.get("halpbot.test.formatting", "formatted");

        Assertions.assertFalse(raw.hasTranslation(Language.FR));
        Assertions.assertFalse(formatted.hasTranslation(Language.FR));

        raw.addTranslation(Language.FR, "Il s'agit d'une ressource de test %s.");

        Assertions.assertEquals("This is a %s test resource.", raw.getTranslation(Language.EN_UK));
        Assertions.assertEquals("This is a formatted test resource.", formatted.getTranslation(Language.EN_UK));
        Assertions.assertTrue(raw.hasTranslation(Language.FR));
        Assertions.assertTrue(formatted.hasTranslation(Language.FR));
        Assertions.assertEquals("Il s'agit d'une ressource de test %s.", raw.getTranslation(Language.FR));
        Assertions.assertEquals("Il s'agit d'une ressource de test formatted.", formatted.getTranslation(Language.FR));

    }

    @Test
    public void formattingTest() {
        Object[] args = { "test", "another" };
        Assertions.assertEquals("This is a test and another", String.format("This is a %s and %s", args));
    }
}
