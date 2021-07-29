package nz.pumbas.parsers;

import java.util.function.Function;

import nz.pumbas.commands.tokens.context.MethodContext;
import nz.pumbas.utilities.Exceptional;
import nz.pumbas.utilities.enums.Priority;

public interface Parser<T>
{
    /**
     * @return The {@link Function mapper} for this {@link Parser}
     */
    Function<MethodContext, Exceptional<T>> mapper();

    /**
     * @return The {@link Priority} associated with this {@link Parser}
     */
    Priority priority();
}
