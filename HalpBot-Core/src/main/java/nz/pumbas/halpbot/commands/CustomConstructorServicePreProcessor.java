package nz.pumbas.halpbot.commands;

import org.dockbox.hartshorn.core.Key;
import org.dockbox.hartshorn.core.annotations.activate.AutomaticActivation;
import org.dockbox.hartshorn.core.context.ApplicationContext;
import org.dockbox.hartshorn.core.context.element.TypeContext;
import org.dockbox.hartshorn.core.services.ProcessingOrder;
import org.dockbox.hartshorn.core.services.ServicePreProcessor;

import nz.pumbas.halpbot.commands.annotations.CustomConstructor;
import nz.pumbas.halpbot.commands.annotations.UseCommands;

@AutomaticActivation
public class CustomConstructorServicePreProcessor implements ServicePreProcessor<UseCommands>
{
    @Override
    public Integer order() {
        return 1;
    }

    @Override
    public boolean preconditions(ApplicationContext context, Key<?> key) {
        return key.type().constructors()
                .stream()
                .anyMatch(constructorContext -> constructorContext.annotation(CustomConstructor.class).present());
    }

    @Override
    public <T> void process(ApplicationContext context, Key<T> key) {
        context.get(CommandAdapter.class).registerCustomConstructors(key.type());
    }

    @Override
    public Class<UseCommands> activator() {
        return UseCommands.class;
    }
}
