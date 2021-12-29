package nz.pumbas.halpbot.buttons;

import org.dockbox.hartshorn.core.context.element.ParameterContext;
import org.dockbox.hartshorn.core.context.element.TypeContext;
import org.dockbox.hartshorn.core.domain.Exceptional;

import java.util.List;

import nz.pumbas.halpbot.actions.exceptions.ActionException;
import nz.pumbas.halpbot.actions.invokable.ActionInvokable;
import nz.pumbas.halpbot.converters.tokens.ParsingToken;

public interface ButtonInvokable extends ActionInvokable<ButtonInvocationContext>
{
    @Override
    default Exceptional<Object[]> parameters(ButtonInvocationContext invocationContext) {
        final Object[] parameters = new Object[this.executable().parameterCount()];
        final Object[] passedParameters = invocationContext.passedParameters();
        final List<ParsingToken> nonCommandParameterTokens = invocationContext.nonCommandParameterTokens();

        int passedParameterIndex = 0;
        int nonCommandParameterIndex = 0;
        int parameterIndex = 0;

        while (parameterIndex < parameters.length) {
            final ParameterContext<?> parameterContext = this.executable().parameters().get(parameterIndex);
            TypeContext<?> targetType = parameterContext.type();
            invocationContext.currentType(targetType);

            // Attempt to find matching parameter
            if (passedParameterIndex < passedParameters.length && targetType.parentOf(passedParameters[passedParameterIndex].getClass()))
                parameters[parameterIndex++] = passedParameters[passedParameterIndex++];
            else if (nonCommandParameterIndex < nonCommandParameterTokens.size()) {
                ParsingToken token = nonCommandParameterTokens.get(nonCommandParameterIndex++);
                if (token.parameterContext().equals(parameterContext)) {
                    parameters[parameterIndex++] = token.converter().apply(invocationContext).orNull();
                }
            }
            else return Exceptional.of(
                    new ActionException("There didn't appear to be a value for the parameter %s in the button %s"
                            .formatted(targetType.qualifiedName(), this.executable().qualifiedName())));
        }
        return Exceptional.of(parameters);
    }
}
