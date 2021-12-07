package nz.pumbas.halpbot.commands.customconstructors;

import org.dockbox.hartshorn.core.context.ApplicationContext;
import org.dockbox.hartshorn.core.context.element.TypeContext;
import org.dockbox.hartshorn.core.services.ServicePreProcessor;

import javax.inject.Inject;

import nz.pumbas.halpbot.commands.annotations.CustomConstructor;
import nz.pumbas.halpbot.commands.annotations.UseCommands;
import nz.pumbas.halpbot.commands.commandadapters.CommandAdapter;

public class CustomConstructorServicePreProcessor implements ServicePreProcessor<UseCommands>
{
    @Inject
    private CommandAdapter commandAdapter;

    @Override
    public boolean preconditions(ApplicationContext context, TypeContext<?> type) {
        return type.constructors()
                .stream()
                .anyMatch(constructorContext -> constructorContext.annotation(CustomConstructor.class).present());
    }

    @Override
    public <T> void process(ApplicationContext context, TypeContext<T> type) {
        this.commandAdapter.registerCustomConstructors(type);
        context.log().info("Registering custom constructors found in %s".formatted(type.qualifiedName()));
    }

    @Override
    public Class<UseCommands> activator() {
        return UseCommands.class;
    }
}
