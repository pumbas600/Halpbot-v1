package nz.pumbas.commands.tokens;

import org.jetbrains.annotations.NotNull;

public class ObjectTypeToken implements ParsingToken
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
        if (!invocationToken.matches(TokenSyntax.OBJECT.getSyntax()))
            return false;

        String typeAlias = invocationToken.substring(1, invocationToken.indexOf('['));
        if (!typeAlias.equalsIgnoreCase(TokenManager.getTypeAlias(this.type)))
            return false;

        String parameters = invocationToken.substring(invocationToken.indexOf("[") + 1, invocationToken.lastIndexOf("]"));
        return false; //TODO: getCommand and check it matches
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
