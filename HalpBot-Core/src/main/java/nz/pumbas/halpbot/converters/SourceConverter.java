package nz.pumbas.halpbot.converters;

import net.dv8tion.jda.api.interactions.commands.OptionType;

import org.dockbox.hartshorn.core.context.element.TypeContext;
import org.dockbox.hartshorn.core.domain.Exceptional;

import java.lang.annotation.Annotation;
import java.util.function.Function;

import nz.pumbas.halpbot.actions.invokable.InvocationContext;

public record SourceConverter<T>(TypeContext<T> type,
                                 TypeContext<? extends Annotation> annotationType,
                                 Function<InvocationContext, Exceptional<T>> mapper,
                                 OptionType optionType,
                                 boolean requiresHalpbotEvent)
        implements Converter<InvocationContext, T>
{
    @Override
    public Exceptional<T> apply(InvocationContext invocationContext) {
        if (this.requiresHalpbotEvent() && invocationContext.halpbotEvent() == null)
            return Exceptional.of(
                    new NullPointerException("The halpbot event is null but it is required to convert this type"));

        return this.mapper().apply(invocationContext);
    }

    public static class SourceConverterBuilder<T> extends ConverterBuilder<SourceConverter<T>, InvocationContext, T> {

        protected SourceConverterBuilder(TypeContext<T> type) {
            super(type);
        }

        @Override
        public SourceConverter<T> build() {
            this.assertConverterSet();
            return new SourceConverter<>(
                    this.type,
                    TypeContext.of(this.annotation),
                    this.converter,
                    this.optionType,
                    this.requiresHalpbotEvent);
        }
    }
}
