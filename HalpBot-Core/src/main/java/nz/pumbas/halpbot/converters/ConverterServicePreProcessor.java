package nz.pumbas.halpbot.converters;

import org.dockbox.hartshorn.core.annotations.service.AutomaticActivation;
import org.dockbox.hartshorn.core.context.ApplicationContext;
import org.dockbox.hartshorn.core.context.element.FieldContext;
import org.dockbox.hartshorn.core.context.element.TypeContext;
import org.dockbox.hartshorn.core.services.ServicePreProcessor;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import nz.pumbas.halpbot.commands.annotations.UseCommands;
import nz.pumbas.halpbot.converters.annotations.Ignore;
import nz.pumbas.halpbot.converters.annotations.NonCommandParameters;

@AutomaticActivation
public class ConverterServicePreProcessor implements ServicePreProcessor<UseCommands>
{
    @Override
    public Class<UseCommands> activator() {
        return UseCommands.class;
    }

    @Override
    public boolean preconditions(ApplicationContext context, TypeContext<?> type) {
        return type.annotation(NonCommandParameters.class).present()
            || !type.fieldsOf(Converter.class).isEmpty();
    }

    @Override
    public <T> void process(ApplicationContext context, TypeContext<T> type) {
        final ConverterHandler handler = context.get(ConverterHandler.class);
        handler.register(type);
    }
}
