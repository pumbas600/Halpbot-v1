package nz.pumbas.commands.tokens.context;

import org.jetbrains.annotations.NotNull;

public class ParsingContext extends InvocationContext
{
    private Class<?> type;

    protected ParsingContext(@NotNull String context)
    {
        super(context);
    }

    public Class<?> getType() {
        return this.type;
    }
}
