package nz.pumbas.halpbot.actions.invokable;

import org.dockbox.hartshorn.core.context.element.ParameterContext;
import org.dockbox.hartshorn.core.context.element.TypeContext;
import org.dockbox.hartshorn.core.domain.Exceptional;

import nz.pumbas.halpbot.actions.exceptions.ActionException;
import nz.pumbas.halpbot.converters.tokens.ParsingToken;
import nz.pumbas.halpbot.utilities.Int;

public interface SourceInvokable<C extends SourceInvocationContext> extends ActionInvokable<C>
{
    @Override
    default Exceptional<Object[]> parameters(C invocationContext) {
        final Object[] parameters = new Object[this.executable().parameterCount()];

        Int nonCommandParameterIndex = new Int(0);
        int parameterIndex = 0;

        while (parameterIndex < parameters.length) {
            final ParameterContext<?> parameterContext = this.executable().parameters().get(parameterIndex);
            TypeContext<?> targetType = parameterContext.type();
            invocationContext.currentType(targetType);
            Exceptional<Object> parameter = this.parameter(invocationContext, parameterContext, nonCommandParameterIndex);
            if (parameter.caught())
                return Exceptional.of(parameter.error());
            parameters[parameterIndex++] = parameter.orNull();
        }

        return Exceptional.of(parameters);
    }

    @SuppressWarnings("unchecked")
    default Exceptional<Object> parameter(C invocationContext, ParameterContext<?> parameterContext,
                                          Int nonCommandParameterIndex)
    {
        if (nonCommandParameterIndex.lessThen(invocationContext.nonCommandParameterTokens().size())) {
            ParsingToken token = invocationContext.nonCommandParameterTokens().get(nonCommandParameterIndex.incrementAfter());
            if (token.parameterContext().equals(parameterContext)) {
                return (Exceptional<Object>) token.converter().apply(invocationContext);
            }
        }
        return Exceptional.of(
                new ActionException("There didn't appear to be a value for the parameter %s in the button %s"
                        .formatted(invocationContext.currentType().qualifiedName(), this.executable().qualifiedName())));
    }
}
