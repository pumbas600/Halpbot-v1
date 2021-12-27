package nz.pumbas.halpbot.converters;

import org.dockbox.hartshorn.core.annotations.activate.AutomaticActivation;
import org.dockbox.hartshorn.core.context.ApplicationContext;
import org.dockbox.hartshorn.core.context.element.TypeContext;
import org.dockbox.hartshorn.core.services.ServicePreProcessor;

import nz.pumbas.halpbot.commands.annotations.UseCommands;

@AutomaticActivation
public class ConverterServicePreProcessor implements ServicePreProcessor<UseCommands>
{
    @Override
    public Class<UseCommands> activator() {
        return UseCommands.class;
    }

    @Override
    public boolean preconditions(ApplicationContext context, TypeContext<?> type) {
        return !type.fieldsOf(Converter.class).isEmpty();
    }

    @Override
    public <T> void process(ApplicationContext context, TypeContext<T> type) {
        final ConverterHandler handler = context.get(ConverterHandler.class);
        handler.register(type);
    }
}
