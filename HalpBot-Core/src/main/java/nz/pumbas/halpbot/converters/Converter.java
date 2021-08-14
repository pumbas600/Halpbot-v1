package nz.pumbas.halpbot.converters;

import java.util.function.Function;

import nz.pumbas.halpbot.commands.tokens.context.MethodContext;
import nz.pumbas.halpbot.objects.Exceptional;
import nz.pumbas.halpbot.utilities.enums.Priority;

public interface Converter<T>
{
    /**
     * @return The {@link Function mapper} for this {@link Converter}
     */
    Function<MethodContext, Exceptional<T>> getMapper();

    /**
     * @return The {@link Priority} associated with this {@link Converter}
     */
    Priority getPriority();

    /**
     * @return The {@link ConverterRegister} for this converter, describing how to register it to a {@link ConverterHandler}
     */
    ConverterRegister getRegister();

    /**
     * Registers itself to the {@link ConverterHandler} using the {@link ConverterRegister}.
     *
     * @param handler
     *      The {@link ConverterHandler} to register itself to.
     */
    default void register(ConverterHandler handler) {
        this.getRegister().register(handler, this);
    }
}
