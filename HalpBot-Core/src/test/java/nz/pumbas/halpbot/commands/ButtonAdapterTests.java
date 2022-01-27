package nz.pumbas.halpbot.commands;

import org.dockbox.hartshorn.testsuite.HartshornTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;

import nz.pumbas.halpbot.buttons.ButtonAdapter;
import nz.pumbas.halpbot.buttons.UseButtons;

@UseButtons
@HartshornTest
public class ButtonAdapterTests
{
    @Inject
    private ButtonAdapter buttonAdapter;

    @Test
    public void isDynamicButtonTest() {
        String id = "halpbot:test:example";
        String dynamicId = this.buttonAdapter.generateDynamicId(id);

        Assertions.assertFalse(this.buttonAdapter.isDynamic(id));
        Assertions.assertTrue(this.buttonAdapter.isDynamic(dynamicId));
    }

    @Test
    public void dynamicIdExtractionTest() {
        String id = "halpbot:test:example";
        String dynamicId = this.buttonAdapter.generateDynamicId(id);
        String extractedId = this.buttonAdapter.extractOriginalId(dynamicId);

        Assertions.assertEquals(id, extractedId);
    }
}
