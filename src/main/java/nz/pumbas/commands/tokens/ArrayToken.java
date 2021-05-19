package nz.pumbas.commands.tokens;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ArrayToken implements ParsingToken {

    private final boolean isOptional;
    private final Class<?> type;
    private final ParsingToken commandToken;
    private final Object defaultValue;

    public ArrayToken(boolean isOptional, Class<?> type, @Nullable String defaultValue) {
        if (!type.isArray())
            throw new IllegalArgumentException(
                    String.format("The type %s, must be an array to be used in an ArrayToken.", type.getSimpleName()));

        this.isOptional = isOptional;
        this.type = type;
        this.commandToken = TokenManager.BuiltInTypes.contains(this.type.getComponentType())
                ? new BuiltInTypeToken(false, this.type.getComponentType(), null)
                : new ObjectTypeToken(false, this.type.getComponentType(), null);
        this.defaultValue = this.parseDefaultValue(defaultValue);
    }

    /**
     * @return If this {@link CommandToken} is optional or not.
     */
    @Override
    public boolean isOptional() {
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
    public boolean matches(@NotNull String invocationToken) {
        if (invocationToken.matches(TokenSyntax.ARRAY.getSyntax())) {
            return TokenManager.splitInvocationTokens(invocationToken.substring(1, invocationToken.length() - 1))
                    .stream().allMatch(this.commandToken::matches);
        }
        return false;
    }

    @Override
    public Class<?> getType() {
        return this.type;
    }

    /**
     * Parses an {@link String invocation token} to an array of the type specified.
     *
     * @param invocationToken
     *      The {@link String} to be parsed into the array of the type specified
     *
     * @return An {@link Object} of the {@link String invocation token} parsed to the correct type
     */
    @Override
    public Object[] parse(@NotNull String invocationToken) {
        List<String> invocationTokens = TokenManager.splitInvocationTokens(invocationToken.substring(1, invocationToken.length() - 1));
        Object[] array = new Object[invocationTokens.size()];

        for (int i = 0; i < invocationTokens.size(); i++) {
            array[i] = this.commandToken.parse(invocationTokens.get(i));
        }

        return array;
    }

    /**
     * @return Retrieves the default value for this {@link ParsingToken} if this is optional, otherwise it returns null.
     */
    @Override
    public @Nullable Object getDefaultValue() {
        return this.defaultValue;
    }
}
