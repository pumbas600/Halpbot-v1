package nz.pumbas.halpbot.commands;

import org.dockbox.hartshorn.core.Key;
import org.dockbox.hartshorn.core.annotations.activate.AutomaticActivation;
import org.dockbox.hartshorn.core.context.ApplicationContext;
import org.dockbox.hartshorn.core.context.element.TypeContext;
import org.dockbox.hartshorn.core.services.ProcessingOrder;
import org.dockbox.hartshorn.core.services.ServicePreProcessor;

import nz.pumbas.halpbot.commands.annotations.Command;
import nz.pumbas.halpbot.commands.annotations.UseCommands;

@AutomaticActivation
public class CommandServicePreProcessor implements ServicePreProcessor<UseCommands>
{
    @Override
    public ProcessingOrder order() {
        return ProcessingOrder.LATE;
    }

    @Override
    public Class<UseCommands> activator() {
        return UseCommands.class;
    }

    @Override
    public boolean preconditions(ApplicationContext context, Key<?> key) {
        return !key.type().methods(Command.class).isEmpty();
    }

    @Override
    public <T> void process(ApplicationContext context, Key<T> key) {
        context.log().debug("Processing %s".formatted(key.type().qualifiedName()));
        CommandAdapter commandAdapter = context.get(CommandAdapter.class);
        commandAdapter.registerCommands(key.type());
    }
}
