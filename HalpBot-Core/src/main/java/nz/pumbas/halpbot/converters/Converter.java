package nz.pumbas.halpbot.converters;

import net.dv8tion.jda.api.interactions.commands.OptionType;

import org.dockbox.hartshorn.core.context.element.TypeContext;
import org.dockbox.hartshorn.core.domain.Exceptional;

import java.lang.annotation.Annotation;
import java.util.function.Function;

import nz.pumbas.halpbot.actions.invokable.InvocationContext;

public interface Converter<C extends InvocationContext, T>
{
    TypeContext<T> type();

    TypeContext<? extends Annotation> annotationType();

    /**
     * @return The {@link Function mapper} for this {@link ParameterConverter}
     */
    Function<C, Exceptional<T>> mapper();

    OptionType optionType();

    default Exceptional<T> apply(C invocationContext) {
        return this.mapper().apply(invocationContext);
    }
}
