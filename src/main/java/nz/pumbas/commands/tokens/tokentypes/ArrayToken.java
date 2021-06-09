package nz.pumbas.commands.tokens.tokentypes;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import nz.pumbas.commands.tokens.TokenManager;
import nz.pumbas.commands.tokens.tokensyntax.InvocationTokenInfo;
import nz.pumbas.utilities.Reflect;

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

    public boolean matches(@NotNull InvocationTokenInfo invocationToken)
    {
        Optional<String> oArrayParameters = invocationToken.getNextSurrounded("[", "]");
        if (oArrayParameters.isPresent()) {
            InvocationTokenInfo subInvocationToken = InvocationTokenInfo.of(oArrayParameters.get());

            while (subInvocationToken.hasNext()) {
                if (!this.commandToken.matches(subInvocationToken))
                    return false;
            }
            return !subInvocationToken.hasNext();
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
        Optional<String> oArrayParameters = invocationToken.getNextSurrounded("[", "]");
        if (oArrayParameters.isEmpty())
            return null;

        InvocationTokenInfo subInvocationToken = InvocationTokenInfo.of(oArrayParameters.get());

        List<Object> parsedArray = new ArrayList<>();
        while (subInvocationToken.hasNext()) {
            parsedArray.add(this.commandToken.parse(subInvocationToken));
        }

        return Reflect.toArray(Reflect.getArrayType(this.type), parsedArray);
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
