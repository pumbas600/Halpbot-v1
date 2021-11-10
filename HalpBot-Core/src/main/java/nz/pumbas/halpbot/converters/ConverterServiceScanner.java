package nz.pumbas.halpbot.converters;

import org.dockbox.hartshorn.core.context.ApplicationContext;
import org.dockbox.hartshorn.core.context.element.FieldContext;
import org.dockbox.hartshorn.core.context.element.TypeContext;
import org.dockbox.hartshorn.core.services.ServiceOrder;
import org.dockbox.hartshorn.core.services.ServiceProcessor;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import nz.pumbas.halpbot.commands.annotations.UseCommands;
import nz.pumbas.halpbot.converters.annotations.NonCommandParameters;

public class ConverterServiceScanner implements ServiceProcessor<UseCommands>
{
    @Override
    public Class<UseCommands> activator() {
        return UseCommands.class;
    }

    @Override
    public ServiceOrder order() {
        return ServiceOrder.EARLY;
    }

    @Override
    public boolean preconditions(ApplicationContext context, TypeContext<?> type) {
        return type.annotation(NonCommandParameters.class).present()
            || !type.fieldsOf(Converter.class).isEmpty();
    }

    @SuppressWarnings("rawtypes")
    @Override
    public <T> void process(ApplicationContext context, TypeContext<T> type) {
        final T instance = context.get(type);
        final ConverterHandler handler = context.get(ConverterHandler.class);

        type.annotation(NonCommandParameters.class)
            .present(nonCommandParameters -> {
                handler.addNonCommandTypes(
                    Stream.of(nonCommandParameters.types())
                        .map(TypeContext::of)
                        .collect(Collectors.toSet()));
                handler.addNonCammandAnnotations(
                    Stream.of(nonCommandParameters.annotations())
                        .map(TypeContext::of)
                        .collect(Collectors.toSet()));
            });

        List<FieldContext<Converter>> converters = type.fieldsOf(Converter.class);

        for (FieldContext<Converter> fieldContext : converters) {
            if (!fieldContext.isStatic() || !fieldContext.isPublic() || !fieldContext.isFinal())
                context.log().warn("The converter %s in %s needs to be static, public and final"
                    .formatted(fieldContext.name(), type.qualifiedName()));

            else fieldContext.get(instance).present(handler::registerConverter);
        }
    }
}
