package nz.pumbas.halpbot.converters;

import net.dv8tion.jda.api.interactions.commands.OptionType;

import org.dockbox.hartshorn.core.context.element.TypeContext;
import org.dockbox.hartshorn.core.domain.Exceptional;
import org.jetbrains.annotations.Nullable;

import java.lang.annotation.Annotation;
import java.util.function.Function;

import nz.pumbas.halpbot.actions.invokable.InvocationContext;
import nz.pumbas.halpbot.converters.exceptions.IllegalConverterException;

public abstract class ConverterBuilder<R extends Converter<C, T>, C extends InvocationContext, T>
{
    protected final TypeContext<T> type;
    protected @Nullable Function<C, Exceptional<T>> converter;
    protected @Nullable Class<? extends Annotation> annotation;

    protected OptionType optionType = OptionType.STRING;

    protected ConverterBuilder(TypeContext<T> type) {
        this.type = type;
    }

    /**
     * Specifies the parsing {@link Function}.
     *
     * @param mapper
     *     The {@link Function} to be used to parse the type
     *
     * @return Itself for chaining
     */
    public ConverterBuilder<R, C, T> convert(Function<C, Exceptional<T>> mapper) {
        this.converter = mapper;
        return this;
    }

    /**
     * The {@link Class type} of the annotation which needs to be present on the type for this {@link TypeConverter}
     * to be called. If no annotation is specified, then this type parser can be called irrespective of the
     * annotations present.
     *
     * @param annotation
     *     The {@link Class type} of the annotation that needs to be present for this type parser to be called
     *
     * @return Itself for chaining
     */
    public ConverterBuilder<R, C, T> annotation(Class<? extends Annotation> annotation) {
        this.annotation = annotation;
        return this;
    }

    /**
     * Specifies the JDA {@link OptionType} to map this type to, when building slash commands. If no option type
     * is specified, then it will default to {@link OptionType#STRING}.
     *
     * @param optionType
     *     The JDA {@link OptionType}
     *
     * @return Itself for chaining
     */
    public ConverterBuilder<R, C, T> optionType(OptionType optionType) {
        this.optionType = optionType;
        return this;
    }

    protected void assertConverterSet() throws IllegalConverterException {
        if (this.converter == null)
            throw new IllegalConverterException(
                    "You must specify a converting function before building the converter");
    }

    /**
     * Builds the {@link Converter} with the specified information.
     *
     * @return The built {@link Converter}
     */
    public abstract R build();
}
