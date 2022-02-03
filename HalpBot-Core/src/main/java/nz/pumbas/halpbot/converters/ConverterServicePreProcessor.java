package nz.pumbas.halpbot.converters;

import org.dockbox.hartshorn.core.Key;
import org.dockbox.hartshorn.core.annotations.activate.AutomaticActivation;
import org.dockbox.hartshorn.core.context.ApplicationContext;
import org.dockbox.hartshorn.core.context.element.TypeContext;
import org.dockbox.hartshorn.core.services.ServicePreProcessor;

import nz.pumbas.halpbot.commands.annotations.UseCommands;

@AutomaticActivation
public class ConverterServicePreProcessor implements ServicePreProcessor<UseConverters>
{
    @Override
    public Class<UseConverters> activator() {
        return UseConverters.class;
    }

    @Override
    public boolean preconditions(ApplicationContext context, Key<?> key) {
        return !key.type().fieldsOf(Converter.class).isEmpty();
    }

    @Override
    public <T> void process(ApplicationContext context, Key<T> key) {
        final ConverterHandler handler = context.get(ConverterHandler.class);
        handler.register(key.type());
    }
}
