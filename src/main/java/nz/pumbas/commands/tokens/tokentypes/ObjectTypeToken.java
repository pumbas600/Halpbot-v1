package nz.pumbas.commands.tokens.tokentypes;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import nz.pumbas.commands.tokens.TokenCommand;
import nz.pumbas.commands.tokens.TokenManager;
import nz.pumbas.commands.tokens.tokensyntax.InvocationTokenInfo;

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
     * Returns if the passed in @link InvocationTokenInfo invocation token} matches this {@link CommandToken}.
     *
     * @param invocationToken
     *     The {@link InvocationTokenInfo invocation token} containing the invoking information
     *
     * @return If the {@link InvocationTokenInfo invocation token} matches this {@link CommandToken}
     */
    @Override
    public boolean matches(@NotNull InvocationTokenInfo invocationToken)
    {
        Optional<String> oTypeAlias = invocationToken.getNextSurrounded("#", "[", false);
        if (oTypeAlias.isEmpty() || !oTypeAlias.get().equalsIgnoreCase(TokenManager.getTypeAlias(this.type)))
            return false;

        Optional<String> oParameters = invocationToken.getNextSurrounded("[", "]");
        if (oParameters.isPresent()) {
            InvocationTokenInfo subInvocationToken = InvocationTokenInfo.of(oParameters.get());
            subInvocationToken.saveState(this);

            for (TokenCommand tokenCommand : TokenManager.getParsedConstructors(this.getType())) {
                if (tokenCommand.matches(subInvocationToken.restoreState(this)))
                    return true;
            }
            return false;
        }
        return false;
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
     * Parses an {@link InvocationTokenInfo invocation token} to the type of the {@link ParsingToken}.
     *
     * @param invocationToken
     *     The {@link InvocationTokenInfo invocation token} to be parsed into the type of the {@link ParsingToken}
     *
     * @return An {@link Object} parsing the {@link InvocationTokenInfo invocation token} to the correct type
     */
    @Override
    @Nullable
    public Object parse(@NotNull InvocationTokenInfo invocationToken)
    {
        invocationToken.getNext("[", false);
        Optional<String> oParameters = invocationToken.getNextSurrounded("[", "]");

        if (oParameters.isPresent()) {
            InvocationTokenInfo subInvocationToken = InvocationTokenInfo.of(oParameters.get());
            subInvocationToken.saveState(this);
            for (TokenCommand tokenCommand : TokenManager.getParsedConstructors(this.getType())) {
                if (tokenCommand.matches(subInvocationToken.restoreState(this))) {
                    return tokenCommand.invoke(subInvocationToken.restoreState(this), null)
                        .orElse(null);
                }
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

    @Override
    public String toString() {
        return String.format("ObjectTypeToken{isOptional=%s, type=%s, defaultValue=%s}",
                this.isOptional, this.type.getSimpleName(), this.defaultValue);
    }
}
