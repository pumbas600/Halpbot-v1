package nz.pumbas.halpbot.commands.preprocessors;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.dockbox.hartshorn.core.annotations.service.AutomaticActivation;
import org.dockbox.hartshorn.core.context.ApplicationContext;
import org.dockbox.hartshorn.core.context.element.MethodContext;
import org.dockbox.hartshorn.core.context.element.TypeContext;
import org.dockbox.hartshorn.core.services.ServiceOrder;
import org.dockbox.hartshorn.core.services.ServicePreProcessor;

import javax.inject.Inject;

import nz.pumbas.halpbot.commands.annotations.Command;
import nz.pumbas.halpbot.commands.annotations.ReflectiveCommand;
import nz.pumbas.halpbot.commands.annotations.SlashCommand;
import nz.pumbas.halpbot.commands.annotations.UseCommands;
import nz.pumbas.halpbot.commands.commandadapters.CommandAdapter;

@AutomaticActivation
public class CommandServicePreProcessor implements ServicePreProcessor<UseCommands>
{
    @Override
    public ServiceOrder order() {
        return ServiceOrder.LATE;
    }

    @Override
    public Class<UseCommands> activator() {
        return UseCommands.class;
    }

    @Override
    public boolean preconditions(ApplicationContext context, TypeContext<?> type) {
        return !type.methods(Command.class).isEmpty();
    }

    @Override
    public <T> void process(ApplicationContext context, TypeContext<T> type) {
        context.log().debug("Processing %s".formatted(type.qualifiedName()));
        T instance = context.get(type);
        CommandAdapter commandAdapter = context.get(CommandAdapter.class);
        commandAdapter.registerCommands(instance);
    }
}
