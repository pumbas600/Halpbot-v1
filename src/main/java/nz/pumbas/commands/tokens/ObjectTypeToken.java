package nz.pumbas.commands.tokens;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Executable;
import java.util.List;

import nz.pumbas.commands.exceptions.OutputException;

public class ObjectTypeToken implements ParsingToken
{
    private final boolean isOptional;
    private final Class<?> type;
    private final Object defaultValue;

    public ObjectTypeToken(boolean isOptional, Class<?> type, @Nullable String defaultValue) {
        this.isOptional = isOptional;
        this.type = type;
        this.defaultValue = this.parseDefaultValue(defaultValue);
    }

    /**
     * @return If this {@link CommandToken} is optional or not.
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
     *      An individual element in the invocation of an {@link nz.pumbas.commands.annotations.Command}.
     *
     * @return If the {@link String invocation token} matches this {@link CommandToken}
     */
    @Override
    public boolean matches(@NotNull String invocationToken)
    {
        if (!TokenSyntax.OBJECT.matches(invocationToken))
            return false;

        String typeAlias = invocationToken.substring(1, invocationToken.indexOf('['));
        if (!typeAlias.equalsIgnoreCase(TokenManager.getTypeAlias(this.type)))
            return false;

        List<String> parameterInvocationTokens = this.getParamaterInvocationTokens(invocationToken);
        return TokenManager.getParsedConstructors(this.getType())
            .stream()
            .anyMatch(tokenCommand -> tokenCommand.matches(parameterInvocationTokens));

    }

    /**
     * @return The {@link Class type} of this {@link ParsingToken}
     */
    @Override
    public Class<?> getType()
    {
        return this.type;
    }

    /**
     * Parses the {@link String invocation token} into the type of this {@link ObjectTypeToken}. If there is no
     * matching {@link TokenCommand} for the type of this token, then it returns null.
     *
     * @param invocationToken
     *      The {@link String} to be parsed into the type of the {@link ParsingToken}
     *
     * @return The parsed {@link String invocation token}
     * @throws OutputException Any {@link OutputException} thrown within the {@link Executable} when parsing
     */
    @Override
    @Nullable
    public Object parse(@NotNull String invocationToken) throws OutputException
    {
        List<String> parameterInvocationTokens = this.getParamaterInvocationTokens(invocationToken);

        for (TokenCommand tokenCommand : TokenManager.getParsedConstructors(this.getType())) {
            if (tokenCommand.matches(parameterInvocationTokens)) {
                return tokenCommand.invoke(parameterInvocationTokens, null).orElse(null);
            }
        }
        return null;
    }

    /**
     * @return Retrieves the default value for this {@link ParsingToken} if this is optional, otherwise it returns null.
     */
    @Override
    @Nullable
    public Object getDefaultValue()
    {
        return this.defaultValue;
    }

    /**
     * Retrieves the {@link List} of invocation tokens from the original {@link String invocation token}.
     *
     * @param invocationToken
     *      The {@link String} which is being used to invoke this {@link ObjectTypeToken}
     *
     * @return The parsed {@link List} of invocation tokens
     */
    private List<String> getParamaterInvocationTokens(String invocationToken)
    {
        String parameters = invocationToken.substring(invocationToken.indexOf("[") + 1, invocationToken.lastIndexOf("]"));
        return TokenManager.splitInvocationTokens(parameters);
    }

    @Override
    public String toString() {
        return String.format("ObjectTypeToken{isOptional=%s, type=%s, defaultValue=%s}",
                this.isOptional, this.type.getSimpleName(), this.defaultValue);
    }
}
