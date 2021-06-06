package nz.pumbas.commands.tokens.tokensyntax;

import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

import nz.pumbas.commands.Order;
import nz.pumbas.commands.annotations.TokenSyntaxDefinition;
import nz.pumbas.commands.annotations.Unrequired;
import nz.pumbas.commands.exceptions.IllegalCommandException;
import nz.pumbas.commands.tokens.TokenManager;
import nz.pumbas.commands.tokens.tokentypes.ArrayToken;
import nz.pumbas.commands.tokens.tokentypes.BuiltInTypeToken;
import nz.pumbas.commands.tokens.tokentypes.CommandToken;
import nz.pumbas.commands.tokens.tokentypes.MultiChoiceToken;
import nz.pumbas.commands.tokens.tokentypes.ObjectTypeToken;
import nz.pumbas.commands.tokens.tokentypes.PlaceholderToken;

public final class TokenSyntaxDefinitions
{
    private TokenSyntaxDefinitions() {}

    @TokenSyntaxDefinition(Order.FIRST)
    public static void optionalTokenSyntax(TokenInfo tokenInfo)
    {
        String token = tokenInfo.getCurrentToken();
        boolean isOptional = false;

        if (token.startsWith("<") && token.endsWith(">")) {
            tokenInfo.setCurrentToken(token.substring(1, token.length() -1));
            isOptional = true;
        }

        tokenInfo.setCurrentTokenBinding(TokenSyntaxIdentifiers.OPTIONAL, isOptional);
    }

    @Nullable
    @TokenSyntaxDefinition
    public static CommandToken tokenSyntax(TokenInfo tokenInfo)
    {
        if (!TokenSyntax.TYPE.matches(tokenInfo.getCurrentToken()))
            return null;

        String defaultValue = getDefaultValue(tokenInfo);
        Class<?> parameterType = tokenInfo.getCurrentParameterType();
        boolean isOptional = tokenInfo.getCurrentTokenBinding(TokenSyntaxIdentifiers.OPTIONAL);

        if (!TokenManager.isValidCommandTypeToken(tokenInfo.getCurrentToken(), parameterType)) {
            throw new IllegalCommandException(
                String.format("The token %s doesn't match the corresponding parameter type of %s",
                    tokenInfo.getCurrentToken(), parameterType));
        }

        else if (TokenManager.isBuiltInType(parameterType)) {
            return new BuiltInTypeToken(isOptional, parameterType, defaultValue);
        }
        else if (parameterType.isArray()) {
            return new ArrayToken(isOptional, parameterType, defaultValue);
        }
        else {
            return new ObjectTypeToken(isOptional, parameterType, defaultValue);
        }
    }

    @Nullable
    @TokenSyntaxDefinition
    public static CommandToken multiChoiceTokenSyntax(TokenInfo tokenInfo)
    {
        String token = tokenInfo.getCurrentToken();
        if (!TokenSyntax.MULTICHOICE.matches(token))
            return null;

        String defaultValue = getDefaultValue(tokenInfo);
        Class<?> parameterType = tokenInfo.getCurrentParameterType();
        boolean isOptional = tokenInfo.getCurrentTokenBinding(TokenSyntaxIdentifiers.OPTIONAL);

        if (!TokenManager.isBuiltInType(parameterType))
            throw new IllegalArgumentException("The type of a multichoice token must be a built in type");

        //Substring removes surrounding [...]
        List<String> options = List.of(token.substring(1, token.length() -1).split("\\|"));
        return new MultiChoiceToken(isOptional, parameterType, defaultValue, options);
    }

    @TokenSyntaxDefinition(Order.LAST)
    public static CommandToken placeholderTokenSyntax(TokenInfo tokenInfo)
    {
        return new PlaceholderToken(
            tokenInfo.getCurrentTokenBinding(TokenSyntaxIdentifiers.OPTIONAL),
            tokenInfo.getCurrentToken());
    }

    @Nullable
    private static String getDefaultValue(TokenInfo tokenInfo)
    {
        Optional<Unrequired> oUnrequired = tokenInfo.getAttribute(Unrequired.class);
        if (oUnrequired.isPresent()) {
            tokenInfo.setCurrentTokenBinding(TokenSyntaxIdentifiers.OPTIONAL, true);
            return oUnrequired.get().value();
        }
        return null;
    }
}
