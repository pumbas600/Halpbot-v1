package nz.pumbas.halpbot.converters;

import net.dv8tion.jda.api.interactions.commands.OptionType;

import org.dockbox.hartshorn.core.context.element.TypeContext;
import org.dockbox.hartshorn.core.domain.Exceptional;

import java.lang.annotation.Annotation;
import java.util.function.Function;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import nz.pumbas.halpbot.actions.invokable.InvocationContext;
import nz.pumbas.halpbot.events.HalpbotEvent;

@Getter
@RequiredArgsConstructor
public class SourceConverter<T> implements Converter<InvocationContext, T>
{
    private final TypeContext<T> type;
    private final TypeContext<? extends Annotation> annotationType;
    private final Function<InvocationContext, Exceptional<T>> mapper;
    private final OptionType optionType;
    private final boolean requiresHalpbotEvent;

    @Override
    public Exceptional<T> apply(InvocationContext invocationContext) {
        if (this.requiresHalpbotEvent() && invocationContext.halpbotEvent() == null)
            return Exceptional.of(
                    new NullPointerException("The halpbot event is null but it is required to convert this type"));

        return this.mapper().apply(invocationContext);
    }

    public static <T> SourceConverterBuilder<T> builder(TypeContext<T> type) {
        return new SourceConverterBuilder<>(type);
    }

    public static <T> SourceConverterBuilder<T> builder(Class<T> type) {
        return builder(TypeContext.of(type));
    }

    public static class SourceConverterBuilder<T> extends ConverterBuilder<SourceConverter<T>, InvocationContext, T> {

        protected SourceConverterBuilder(TypeContext<T> type) {
            super(type);
            this.requiresHalpbotEvent = true;
        }

        /**
         * Specifies that this converter requires there to be a {@link HalpbotEvent}. If this event is null in the
         * {@link InvocationContext} then it will automatically return an exceptional containing a {@link
         * NullPointerException} and the converter function will NOT be called. By default, this is true for source
         * converters.
         *
         * @param isRequired
         *         If the halpbot event is required to be present
         *
         * @return Itself for chaining
         */
        @Override
        public ConverterBuilder<SourceConverter<T>, InvocationContext, T> requiresHalpbotEvent(boolean isRequired) {
            return super.requiresHalpbotEvent(isRequired);
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
