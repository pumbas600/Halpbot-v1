package nz.pumbas.halpbot.buttons;

import org.dockbox.hartshorn.core.annotations.activate.AutomaticActivation;
import org.dockbox.hartshorn.core.context.ApplicationContext;
import org.dockbox.hartshorn.core.context.element.TypeContext;
import org.dockbox.hartshorn.core.services.ServicePreProcessor;

@AutomaticActivation
public class ButtonServicePreProcessor implements ServicePreProcessor<UseButtons>
{
    @Override
    public boolean preconditions(ApplicationContext context, TypeContext<?> type) {
        return !type.methods(ButtonAction.class).isEmpty();
    }

    @Override
    public <T> void process(ApplicationContext context, TypeContext<T> type) {
        final ButtonAdapter buttonAdapter = context.get(ButtonAdapter.class);
        buttonAdapter.registerButtons(type);
    }

    @Override
    public Class<UseButtons> activator() {
        return UseButtons.class;
    }
}
