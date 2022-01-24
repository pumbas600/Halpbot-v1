package nz.pumbas.halpbot.common;

import net.dv8tion.jda.api.hooks.EventListener;

import org.dockbox.hartshorn.core.Key;
import org.dockbox.hartshorn.core.annotations.activate.AutomaticActivation;
import org.dockbox.hartshorn.core.context.ApplicationContext;
import org.dockbox.hartshorn.core.services.ServicePreProcessor;

import nz.pumbas.halpbot.HalpbotCore;

@AutomaticActivation
public class EventListenerServicePreProcessor implements ServicePreProcessor<UseDefault>
{

    @Override
    public boolean preconditions(ApplicationContext context, Key<?> key) {
        return key.type().childOf(EventListener.class);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> void process(ApplicationContext context, Key<T> key) {
        context.get(HalpbotCore.class).registerEventListener(context.get((Key<? extends EventListener>) key));
    }

    @Override
    public Class<UseDefault> activator() {
        return UseDefault.class;
    }
}
