package nz.pumbas.halpbot.converters.tokens;

import org.dockbox.hartshorn.core.context.ApplicationContext;
import org.dockbox.hartshorn.core.context.element.ParameterContext;
import org.dockbox.hartshorn.core.context.element.TypeContext;
import org.dockbox.hartshorn.core.domain.Exceptional;
import org.jetbrains.annotations.Nullable;

import java.lang.annotation.Annotation;
import java.util.List;

import nz.pumbas.halpbot.converters.annotations.parameter.Unrequired;
import nz.pumbas.halpbot.commands.context.InvocationContext;
import nz.pumbas.halpbot.converters.Converter;
import nz.pumbas.halpbot.converters.ConverterHandler;
import nz.pumbas.halpbot.converters.parametercontext.ParameterAnnotationService;

public record HalpbotParsingToken(ParameterContext<?> parameterContext,
                                  List<TypeContext<? extends Annotation>> sortedAnnotations,
                                  Converter<?> converter,
                                  @Nullable Object defaultValue,
                                  boolean isCommandParameter,
                                  boolean isOptional)
    implements ParsingToken
{

    public static HalpbotParsingToken of(ApplicationContext applicationContext,
                                         ParameterContext<?> parameterContext)
    {
        final ConverterHandler converterHandler = applicationContext.get(ConverterHandler.class);
        final ParameterAnnotationService annotationService = applicationContext.get(ParameterAnnotationService.class);

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
            InvocationContext invocationContext = new InvocationContext(applicationContext, unrequired.get().value());
            invocationContext.update(parameterContext, sortedAnnotations);

            defaultValue = ParsingToken.parseDefaultValue(converter, invocationContext);
        }

        return new HalpbotParsingToken(
                parameterContext, sortedAnnotations, converter, defaultValue, isCommandParameter, isOptional);
    }
}
