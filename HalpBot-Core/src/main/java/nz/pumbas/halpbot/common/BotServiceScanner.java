package nz.pumbas.halpbot.common;

import org.dockbox.hartshorn.core.context.ApplicationContext;
import org.dockbox.hartshorn.core.context.element.TypeContext;
import org.dockbox.hartshorn.core.services.ServiceProcessor;

import nz.pumbas.halpbot.common.annotations.Bot;

public class BotServiceScanner implements ServiceProcessor<Bot>
{
    @Override
    public boolean preconditions(ApplicationContext context, TypeContext<?> type) {
        return type.annotation(Bot.class).present();
    }

    @Override
    public <T> void process(ApplicationContext context, TypeContext<T> type) {
        // Create an instance of the class
        context.get(type);
    }

    @Override
    public Class<Bot> activator() {
        return Bot.class;
    }
}
