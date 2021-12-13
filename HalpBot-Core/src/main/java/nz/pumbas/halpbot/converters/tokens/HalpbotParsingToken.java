package nz.pumbas.halpbot.converters.tokens;

import org.dockbox.hartshorn.core.annotations.inject.Binds;
import org.dockbox.hartshorn.core.annotations.inject.Bound;
import org.dockbox.hartshorn.core.context.ApplicationContext;
import org.dockbox.hartshorn.core.context.element.ParameterContext;
import org.dockbox.hartshorn.core.context.element.TypeContext;
import org.dockbox.hartshorn.core.domain.Exceptional;
import org.jetbrains.annotations.Nullable;

import java.lang.annotation.Annotation;
import java.util.List;

import nz.pumbas.halpbot.commands.context.InvocationContext;
import nz.pumbas.halpbot.converters.annotations.parameter.Unrequired;
import nz.pumbas.halpbot.commands.context.HalpbotInvocationContext;
import nz.pumbas.halpbot.converters.Converter;
import nz.pumbas.halpbot.converters.ConverterHandler;
import nz.pumbas.halpbot.converters.parametercontext.ParameterAnnotationService;

@Binds(ParsingToken.class)
public record HalpbotParsingToken(ParameterContext<?> parameterContext,
                                  List<TypeContext<? extends Annotation>> sortedAnnotations,
                                  Converter<?> converter,
                                  @Nullable Object defaultValue,
                                  boolean isCommandParameter,
                                  boolean isOptional)
    implements ParsingToken
{
    @Bound
    public HalpbotParsingToken {}
}
