package nz.pumbas.halpbot.converters.tokens;

import org.dockbox.hartshorn.core.annotations.Factory;
import org.dockbox.hartshorn.core.annotations.proxy.Provided;
import org.dockbox.hartshorn.core.annotations.service.Service;
import org.dockbox.hartshorn.core.context.element.ParameterContext;
import org.dockbox.hartshorn.core.context.element.TypeContext;
import org.dockbox.hartshorn.core.domain.Exceptional;
import org.jetbrains.annotations.Nullable;

import java.lang.annotation.Annotation;
import java.util.List;

import nz.pumbas.halpbot.commands.context.CommandInvocationContext;
import nz.pumbas.halpbot.commands.context.InvocationContextFactory;
import nz.pumbas.halpbot.converters.Converter;
import nz.pumbas.halpbot.converters.ConverterHandler;
import nz.pumbas.halpbot.converters.annotations.parameter.Unrequired;
import nz.pumbas.halpbot.converters.parametercontext.ParameterAnnotationService;

@Service
public interface TokenFactory
{
    @Provided
    ConverterHandler converterHandler();

    @Provided
    ParameterAnnotationService parameterAnnotationService();

    @Provided
    InvocationContextFactory invocationContexFactory();

    @Factory
    PlaceholderToken createPlaceholder(boolean isOptional, String placeholder);

    @Factory
    ParsingToken createParsing(ParameterContext<?> parameterContext,
                               List<TypeContext<? extends Annotation>> sortedAnnotations,
                               Converter<?> converter,
                               @Nullable Object defaultValue,
                               boolean isCommandParameter,
                               boolean isOptional);

    default ParsingToken createParsing(ParameterContext<?> parameterContext)
    {
        final ConverterHandler converterHandler = this.converterHandler();
        final ParameterAnnotationService annotationService = this.parameterAnnotationService();

        boolean isCommandParameter = converterHandler.isCommandParameter(parameterContext);
        boolean isOptional = false;
        @Nullable Object defaultValue = null;

        List<TypeContext<? extends Annotation>> sortedAnnotations =
                annotationService.sortAndFilter(
                        parameterContext.annotations()
                                .stream()
                                .map(annotation -> TypeContext.of(annotation.annotationType())));
        Converter<?> converter = converterHandler.from(parameterContext, sortedAnnotations);

        Exceptional<Unrequired> unrequired = parameterContext.annotation(Unrequired.class);

        if (unrequired.present()) {
            isOptional = true;
            CommandInvocationContext invocationContext = this.invocationContexFactory().create(unrequired.get().value());
            invocationContext.update(parameterContext, sortedAnnotations);

            defaultValue = ParsingToken.parseDefaultValue(converter, invocationContext);
        }

        return this.createParsing(
                parameterContext,
                sortedAnnotations,
                converter,
                defaultValue,
                isCommandParameter,
                isOptional);
    }
}
