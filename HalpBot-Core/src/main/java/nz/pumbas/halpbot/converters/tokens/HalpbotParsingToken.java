package nz.pumbas.halpbot.converters.tokens;

import org.dockbox.hartshorn.core.annotations.inject.Binds;
import org.dockbox.hartshorn.core.annotations.inject.Bound;
import org.dockbox.hartshorn.core.context.element.ParameterContext;
import org.dockbox.hartshorn.core.context.element.TypeContext;
import org.jetbrains.annotations.Nullable;

import java.lang.annotation.Annotation;
import java.util.List;

import nz.pumbas.halpbot.converters.Converter;
import nz.pumbas.halpbot.converters.ParameterConverter;

@Binds(ParsingToken.class)
public record HalpbotParsingToken(ParameterContext<?> parameterContext,
                                  List<TypeContext<? extends Annotation>> sortedAnnotations,
                                  Converter<?, ?> converter,
                                  @Nullable Object defaultValue,
                                  boolean isCommandParameter,
                                  boolean isOptional)
    implements ParsingToken
{
    @Bound
    public HalpbotParsingToken {}
}
