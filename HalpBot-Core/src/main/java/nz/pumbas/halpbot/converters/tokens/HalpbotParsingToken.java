package nz.pumbas.halpbot.converters.tokens;

import org.dockbox.hartshorn.core.context.ApplicationContext;
import org.dockbox.hartshorn.core.context.element.ParameterContext;
import org.dockbox.hartshorn.core.domain.Exceptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import nz.pumbas.halpbot.converters.annotations.parameter.Unrequired;
import nz.pumbas.halpbot.commands.context.InvocationContext;
import nz.pumbas.halpbot.converters.Converter;
import nz.pumbas.halpbot.converters.ConverterHandler;

public record HalpbotParsingToken(@NotNull ParameterContext<?> parameterContext,
                                  @NotNull Converter<?> converter,
                                  @Nullable Object defaultValue,
                                  boolean isCommandParameter,
                                  boolean isOptional)
    implements ParsingToken
{

    public static HalpbotParsingToken of(@NotNull ApplicationContext applicationContext,
                                         @NotNull ParameterContext<?> parameterContext)
    {
        final ConverterHandler converterHandler = applicationContext.get(ConverterHandler.class);

        Converter<?> converter = converterHandler.from(parameterContext);
        boolean isCommandParameter = converterHandler.isCommandParameter(parameterContext);
        Object defaultValue = null;
        boolean isOptional = false;

        Exceptional<Unrequired> unrequired = parameterContext.annotation(Unrequired.class);

        if (unrequired.present()) {
            isOptional = true;
            InvocationContext invocationContext = new InvocationContext(applicationContext, unrequired.get().value());
            invocationContext.currentParameter(parameterContext);

            defaultValue = ParsingToken.parseDefaultValue(converter, invocationContext);
        }

        return new HalpbotParsingToken(parameterContext, converter, defaultValue, isCommandParameter, isOptional);
    }
}
