package nz.pumbas.halpbot.parsers;

import java.util.function.Function;

import nz.pumbas.halpbot.commands.tokens.context.MethodContext;
import nz.pumbas.halpbot.objects.Exceptional;
import nz.pumbas.halpbot.utilities.enums.Priority;

public interface Parser<T>
{
    /**
     * @return The {@link Function mapper} for this {@link Parser}
     */
    Function<MethodContext, Exceptional<T>> getMapper();

    /**
     * @return The {@link Priority} associated with this {@link Parser}
     */
    Priority getPriority();
}
