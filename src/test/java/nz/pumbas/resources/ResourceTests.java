package nz.pumbas.resources;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ResourceTests
{

    @Test
    public void loadingTranslationsTest()
    {
        Resource resource = Resource.get("halpbot.test");
        Assertions.assertEquals("This is a test resource", resource.getTranslation(Language.EN_UK));
    }
}
