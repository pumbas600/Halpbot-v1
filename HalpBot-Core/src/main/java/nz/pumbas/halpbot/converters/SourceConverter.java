package nz.pumbas.halpbot.converters;

import net.dv8tion.jda.api.interactions.commands.OptionType;

import org.dockbox.hartshorn.core.context.element.TypeContext;
import org.dockbox.hartshorn.core.domain.Exceptional;

import java.lang.annotation.Annotation;
import java.util.function.Function;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import nz.pumbas.halpbot.actions.invokable.InvocationContext;

@Getter
@RequiredArgsConstructor
public class SourceConverter<T> implements Converter<InvocationContext, T>
{
    private final TypeContext<T> type;
    private final TypeContext<? extends Annotation> annotationType;
    private final Function<InvocationContext, Exceptional<T>> mapper;
    private final OptionType optionType;

    public static <T> SourceConverterBuilder<T> builder(TypeContext<T> type) {
        return new SourceConverterBuilder<>(type);
    }

    public static <T> SourceConverterBuilder<T> builder(Class<T> type) {
        return builder(TypeContext.of(type));
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
                    this.optionType);
        }
    }
}
