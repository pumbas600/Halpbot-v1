package nz.pumbas.halpbot.buttons;

import org.dockbox.hartshorn.core.Key;
import org.dockbox.hartshorn.core.annotations.activate.AutomaticActivation;
import org.dockbox.hartshorn.core.context.ApplicationContext;
import org.dockbox.hartshorn.core.context.element.TypeContext;
import org.dockbox.hartshorn.core.services.ProcessingOrder;
import org.dockbox.hartshorn.core.services.ServicePreProcessor;

@AutomaticActivation
public class ButtonServicePreProcessor implements ServicePreProcessor<UseButtons>
{
    @Override
    public ProcessingOrder order() {
        return ProcessingOrder.LATE;
    }

    @Override
    public boolean preconditions(ApplicationContext context, Key<?> key) {
        return !key.type().methods(ButtonAction.class).isEmpty();
    }

    @Override
    public <T> void process(ApplicationContext context, Key<T> key) {
        final ButtonAdapter buttonAdapter = context.get(ButtonAdapter.class);
        buttonAdapter.registerButtons(key.type());
    }

    @Override
    public Class<UseButtons> activator() {
        return UseButtons.class;
    }
}
