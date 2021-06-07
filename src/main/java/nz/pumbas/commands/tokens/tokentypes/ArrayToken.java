package nz.pumbas.commands.tokens.tokentypes;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

import nz.pumbas.commands.tokens.TokenManager;
import nz.pumbas.commands.tokens.tokensyntax.InvocationTokenInfo;
import nz.pumbas.commands.tokens.tokensyntax.TokenSyntax;
import nz.pumbas.utilities.Reflect;
import nz.pumbas.utilities.Utilities;

public class ArrayToken implements ParsingToken {

    public final static Pattern Syntax = Pattern.compile("\\[.*]");

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
        this.commandToken = TokenManager.isBuiltInType(Reflect.getArrayType(this.type))
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
     *      An individual element in the invocation of an {@link nz.pumbas.commands.annotations.Command}.
     *
     * @return If the {@link String invocation token} matches this {@link CommandToken}
     */
    @Override
    public boolean matches(@NotNull String invocationToken) {
        if (TokenSyntax.ARRAY.matches(invocationToken)) {
            return TokenManager.splitInvocationTokens(invocationToken.substring(1, invocationToken.length() - 1))
                    .stream().allMatch(this.commandToken::matches);
        }
        return false;
    }

    public boolean matches(@NotNull InvocationTokenInfo invocationTokenInfo)
    {
        Optional<String> oToken = invocationTokenInfo.getNextSurrounded("[", "]");
        if (oToken.isPresent()) {
            InvocationTokenInfo subTokenInfo = InvocationTokenInfo.of(oToken.get());

            while (subTokenInfo.hasNext()) {
                if (!this.commandToken.matches(subTokenInfo))
                    return false;
            }
            return !subTokenInfo.hasNext();
        }
        return false;
    }

    /**
     * @return The {@link Class type} of this {@link ParsingToken}
     */
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
    public Object parse(@NotNull String invocationToken)
    {
        List<String> invocationTokens = TokenManager.splitInvocationTokens(invocationToken.substring(1, invocationToken.length() - 1));
        Object array = Array.newInstance(Reflect.getArrayType(this.type), invocationTokens.size());

        for (int i = 0; i < invocationTokens.size(); i++) {
            Array.set(array, i, this.commandToken.parse(invocationTokens.get(i)));
        }

        return array;
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
        Optional<String> arrayToken = invocationToken.getNext(Syntax);
        if (arrayToken.isEmpty())
            return null;

        InvocationTokenInfo subTokenInfo =
            InvocationTokenInfo.of(arrayToken.get().substring(1, arrayToken.get().length() - 1));

        List<Object> parsedArray = new ArrayList<>();
        while (subTokenInfo.hasNext()) {
            parsedArray.add(this.commandToken.parse(subTokenInfo));
        }

        return Utilities.toArray(Reflect.getArrayType(this.type), parsedArray);
    }

    /**
     * @return Retrieves the default value for this {@link ParsingToken} if this is optional, otherwise it returns null.
     */
    @Override
    @Nullable
    public Object getDefaultValue() {
        return this.defaultValue;
    }

    @Override
    public String toString() {
        return String.format("ArrayToken{isOptional=%s, type=%s, defaultValue=%s}",
                this.isOptional, this.type.getSimpleName(), this.defaultValue);
    }
}