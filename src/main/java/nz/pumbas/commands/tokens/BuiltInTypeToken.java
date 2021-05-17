package nz.pumbas.commands.tokens;

import org.jetbrains.annotations.NotNull;

import nz.pumbas.utilities.Utilities;

public class BuiltInTypeToken implements CommandToken, ParsingToken
{
    private final boolean isOptional;
    private final Class<?> type;

    public BuiltInTypeToken(boolean isOptional, Class<?> type) {
        this.isOptional = isOptional;
        this.type = type;
    }

    /**
     * @return If this {@link CommandToken} is optional or not
     */
    @Override
    public boolean isOptional()
    {
        return this.isOptional;
    }

    /**
     * Returns if the passed in {@link String invocation token} matches this {@link CommandToken}.
     *
     * @param invocationToken
     *      An individual element in the invocation of an {@link nz.pumbas.commands.Annotations.Command}.
     *
     * @return If the {@link String invocation token} matches this {@link CommandToken}
     */
    @Override
    public boolean matches(@NotNull String invocationToken)
    {
        if (this.type.isEnum()) {
            return Utilities.isValidValue(this.type, invocationToken.toUpperCase());
        }

        return invocationToken.matches(TokenManager.TypeParsers.get(this.type).getKey());
    }

    /**
     * @return The required {@link Class type} of this {@link ParsingToken}
     */
    @Override
    public Class<?> getType()
    {
        return this.type;
    }

    /**
     * Parses an {@link String invocation token} to the type of the {@link ParsingToken}.
     *
     * @param invocationToken
     *      The {@link String} to be parsed into the type of the {@link ParsingToken}
     *
     * @return An {@link Object} of the {@link String invocation token} parsed to the correct type
     */
    @Override
    @SuppressWarnings("unchecked")
    public Object parse(@NotNull String invocationToken)
    {
        if (this.type.isEnum()) {
            return Enum.valueOf((Class<? extends Enum>)this.type, invocationToken.toUpperCase());
        }
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
