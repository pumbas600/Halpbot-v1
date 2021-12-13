package nz.pumbas.halpbot.commands.factory;

import org.dockbox.hartshorn.core.annotations.service.AutomaticActivation;
import org.dockbox.hartshorn.core.context.ApplicationContext;
import org.dockbox.hartshorn.core.context.element.TypeContext;
import org.dockbox.hartshorn.core.services.ServicePreProcessor;

import javax.inject.Inject;

@AutomaticActivation
public class PersonServicePreProcessor implements ServicePreProcessor<Demo>
{
    //@Inject PersonFactory factory;

    @Override
    public boolean preconditions(ApplicationContext context, TypeContext<?> type) {
        return false;
    }

    @Override
    public <T> void process(ApplicationContext context, TypeContext<T> type) {

    }

    @Override
    public Class<Demo> activator() {
        return Demo.class;
    }
}
