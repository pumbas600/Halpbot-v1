package nz.pumbas.commands.tokens;

import org.jetbrains.annotations.NotNull;

public class ObjectTypeToken implements CommandToken, ParsingToken
{
    private final boolean isOptional;
    private final Class<?> type;

    public ObjectTypeToken(boolean isOptional, Class<?> type) {
        this.isOptional = isOptional;
        this.type = type;
    }

    @Override
    public boolean isOptional()
    {
        return this.isOptional;
    }

    @Override
    public boolean matches(@NotNull String invocationToken)
    {
        return false;
    }

    @Override
    public Class<?> getType()
    {
        return this.type;
    }

    @Override
    public Object parse(@NotNull String invocationToken)
    {
        return null;
    }
}
