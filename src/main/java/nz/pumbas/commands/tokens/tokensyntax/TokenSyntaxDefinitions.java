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
    public static void optionalTokenSyntax(CommandTokenInfo commandTokenInfo)
    {
        String token = commandTokenInfo.getCurrentToken();
        boolean isOptional = false;

        if (token.startsWith("<") && token.endsWith(">")) {
            commandTokenInfo.setCurrentToken(token.substring(1, token.length() -1));
            isOptional = true;
        }

        commandTokenInfo.setCurrentTokenBinding(TokenSyntaxIdentifiers.OPTIONAL, isOptional);
    }

    @Nullable
    @TokenSyntaxDefinition
    public static CommandToken tokenSyntax(CommandTokenInfo commandTokenInfo)
    {
        if (!TokenSyntax.TYPE.matches(commandTokenInfo.getCurrentToken()))
            return null;

        String defaultValue = getDefaultValue(commandTokenInfo);
        Class<?> parameterType = commandTokenInfo.getCurrentParameterType();
        boolean isOptional = commandTokenInfo.getCurrentTokenBinding(TokenSyntaxIdentifiers.OPTIONAL);

        if (!TokenManager.isValidCommandTypeToken(commandTokenInfo.getCurrentToken(), parameterType)) {
            throw new IllegalCommandException(
                String.format("The token %s doesn't match the corresponding parameter type of %s",
                    commandTokenInfo.getCurrentToken(), parameterType));
        }

        else if (TokenManager.isBuiltInType(parameterType)) {
            return new BuiltInTypeToken(isOptional, parameterType, defaultValue, commandTokenInfo.getAnnotations());
        }
        else if (parameterType.isArray()) {
            return new ArrayToken(isOptional, parameterType, defaultValue, commandTokenInfo.getAnnotations());
        }
        else {
            return new ObjectTypeToken(isOptional, parameterType, defaultValue, commandTokenInfo.getAnnotations());
        }
    }

    @Nullable
    @TokenSyntaxDefinition
    public static CommandToken multiChoiceTokenSyntax(CommandTokenInfo commandTokenInfo)
    {
        String token = commandTokenInfo.getCurrentToken();
        if (!TokenSyntax.MULTICHOICE.matches(token))
            return null;

        String defaultValue = getDefaultValue(commandTokenInfo);
        Class<?> parameterType = commandTokenInfo.getCurrentParameterType();
        boolean isOptional = commandTokenInfo.getCurrentTokenBinding(TokenSyntaxIdentifiers.OPTIONAL);

        if (!TokenManager.isBuiltInType(parameterType))
            throw new IllegalArgumentException("The type of a multichoice token must be a built in type");

        //Substring removes surrounding [...]
        List<String> options = List.of(token.substring(1, token.length() -1).split("\\|"));
        return new MultiChoiceToken(isOptional, parameterType, defaultValue, options, commandTokenInfo.getAnnotations());
    }

    @TokenSyntaxDefinition(Order.LAST)
    public static CommandToken placeholderTokenSyntax(CommandTokenInfo commandTokenInfo)
    {
        return new PlaceholderToken(
            commandTokenInfo.getCurrentTokenBinding(TokenSyntaxIdentifiers.OPTIONAL),
            commandTokenInfo.getCurrentToken());
    }

    @Nullable
    private static String getDefaultValue(CommandTokenInfo commandTokenInfo)
    {
        Optional<Unrequired> oUnrequired = commandTokenInfo.getAttribute(Unrequired.class);
        if (oUnrequired.isPresent()) {
            commandTokenInfo.setCurrentTokenBinding(TokenSyntaxIdentifiers.OPTIONAL, true);
            return oUnrequired.get().value();
        }
        return null;
    }
}
