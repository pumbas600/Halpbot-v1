package nz.pumbas.halpbot.commands.tokens;

import org.dockbox.hartshorn.core.context.ApplicationContext;
import org.dockbox.hartshorn.core.context.element.ParameterContext;
import org.dockbox.hartshorn.core.domain.Exceptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import nz.pumbas.halpbot.commands.CommandManager;
import nz.pumbas.halpbot.commands.annotations.Unrequired;
import nz.pumbas.halpbot.commands.context.MethodContext;
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
        Converter<?> converter = applicationContext.get(ConverterHandler.class).from(parameterContext);
        boolean isCommandParameter = CommandManager.isCommandParameter(parameterContext);
        Object defaultValue = null;
        boolean isOptional = false;

        Exceptional<Unrequired> unrequired = parameterContext.annotation(Unrequired.class);

        if (unrequired.present()) {
            isOptional = true; //TODO: Make converters support use of $Default
            //TODO: Remove MethodContext -> Combine it with InvocationContext
            //TODO: This currently doesn't work - Make methodContext be able to take in a ParameterContext
            defaultValue = ParsingToken.parseDefaultValue(converter, MethodContext.of(unrequired.type()));
        }

        return new HalpbotParsingToken(parameterContext, converter, defaultValue, isCommandParameter, isOptional);
    }

}
