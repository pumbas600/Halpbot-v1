package nz.pumbas.halpbot.common;

import org.dockbox.hartshorn.core.boot.HartshornApplicationFactory;
import org.dockbox.hartshorn.core.context.element.TypeContext;

public class HalpbotApplicationFactory extends HartshornApplicationFactory
{
    @Override
    public HartshornApplicationFactory activator(TypeContext<?> activator) {
        return super.activator(BotTypeContext.wrap(activator));
    }
}
