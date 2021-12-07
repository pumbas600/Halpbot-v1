package nz.pumbas.halpbot.converters.parametercontext;

import org.dockbox.hartshorn.core.annotations.service.AutomaticActivation;
import org.dockbox.hartshorn.core.context.ApplicationContext;
import org.dockbox.hartshorn.core.context.element.TypeContext;
import org.dockbox.hartshorn.core.services.ServicePreProcessor;
import org.jetbrains.annotations.NotNull;

import java.lang.annotation.Annotation;

import nz.pumbas.halpbot.commands.annotations.UseCommands;
import nz.pumbas.halpbot.converters.annotations.ParameterAnnotation;

@AutomaticActivation
public class ParameterAnnotationServicePreProcessor implements ServicePreProcessor<UseCommands>
{
    @Override
    public boolean preconditions(@NotNull ApplicationContext context, @NotNull TypeContext<?> type) {
        return type.annotation(ParameterAnnotation.class).present();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> void process(@NotNull ApplicationContext context, @NotNull TypeContext<T> type) {
        context.get(ParameterAnnotationService.class)
                .register((TypeContext<? extends Annotation>) type);
        context.log().info("Registered the parameter annotation %s".formatted(type.qualifiedName()));
    }

    @Override
    public Class<UseCommands> activator() {
        return UseCommands.class;
    }
}
