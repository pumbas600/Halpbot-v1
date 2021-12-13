package nz.pumbas.halpbot.commands.preprocessors;

import org.dockbox.hartshorn.core.annotations.service.AutomaticActivation;
import org.dockbox.hartshorn.core.context.ApplicationContext;
import org.dockbox.hartshorn.core.context.element.TypeContext;
import org.dockbox.hartshorn.core.services.ServicePreProcessor;

import javax.inject.Inject;

import nz.pumbas.halpbot.commands.annotations.CustomConstructor;
import nz.pumbas.halpbot.commands.annotations.UseCommands;
import nz.pumbas.halpbot.commands.commandadapters.CommandAdapter;

@AutomaticActivation
public class CustomConstructorServicePreProcessor implements ServicePreProcessor<UseCommands>
{
    @Override
    public boolean preconditions(ApplicationContext context, TypeContext<?> type) {
        return type.constructors()
                .stream()
                .anyMatch(constructorContext -> constructorContext.annotation(CustomConstructor.class).present());
    }

    @Override
    public <T> void process(ApplicationContext context, TypeContext<T> type) {
        context.get(CommandAdapter.class).registerCustomConstructors(type);
    }

    @Override
    public Class<UseCommands> activator() {
        return UseCommands.class;
    }
}
