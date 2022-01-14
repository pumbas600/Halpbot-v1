package nz.pumbas.halpbot.triggers;

import org.dockbox.hartshorn.core.Key;
import org.dockbox.hartshorn.core.annotations.activate.AutomaticActivation;
import org.dockbox.hartshorn.core.context.ApplicationContext;
import org.dockbox.hartshorn.core.services.ProcessingOrder;
import org.dockbox.hartshorn.core.services.ServicePreProcessor;

@AutomaticActivation
public class TriggerServicePreProcessor implements ServicePreProcessor<UseTriggers>
{
    @Override
    public ProcessingOrder order() {
        return ProcessingOrder.LATE;
    }

    @Override
    public boolean preconditions(ApplicationContext context, Key<?> key) {
        return !key.type().methods(Trigger.class).isEmpty();
    }

    @Override
    public <T> void process(ApplicationContext context, Key<T> key) {
        context.get(TriggerAdapter.class).registerTriggers(key.type());
    }

    @Override
    public Class<UseTriggers> activator() {
        return UseTriggers.class;
    }
}
