package nz.pumbas.commands.tokens;

import org.jetbrains.annotations.NotNull;

public class BuiltInTypeToken implements CommandToken, ParsingToken
{
    private final boolean isOptional;
    private final Class<?> type;

    public BuiltInTypeToken(boolean isOptional, Class<?> type) {
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
        return invocationToken.matches(TokenManager.TypeParsers.get(this.type).getKey());
    }

    @Override
    public Class<?> getType()
    {
        return this.type;
    }

    @Override
    public Object parse(@NotNull String invocationToken)
    {
        return TokenManager.TypeParsers.get(this.type)
            .getValue()
            .apply(invocationToken);
    }

    @Override
    public String toString()
    {
        return String.format("BuiltInTypeToken{isOptional=%s, type=%s}",
            this.isOptional, this.type.getSimpleName());
    }
}
