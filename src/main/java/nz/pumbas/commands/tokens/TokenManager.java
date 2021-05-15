package nz.pumbas.commands.tokens;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import nz.pumbas.commands.Annotations.Command;
import nz.pumbas.commands.Annotations.CustomParameter;
import nz.pumbas.commands.Exceptions.IllegalCommandException;
import nz.pumbas.utilities.Utilities;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TokenManager {

    private static final List<Class<?>> BuiltInTypes = List.of(
            int.class, float.class, double.class, char.class, String.class
    );

    public static boolean isBuiltInType(Class<?> type)
    {
        return BuiltInTypes.contains(type);
    }

    public static List<CommandToken> parseCommand(Method method) {
        if (!Utilities.hasAnnotation(method, Command.class))
            throw new IllegalCommandException(
                    String.format("Cannot parse the method %s as it isn't annotated with Command", method.getName()));

        Command command = method.getAnnotation(Command.class);
        Class<?>[] parameterTypes = method.getParameterTypes();
        int startParameterIndex = 0;

        if (parameterTypes.length > 0 && parameterTypes[0].isAssignableFrom(MessageReceivedEvent.class))
            startParameterIndex = 1;

        //TODO: Automatically generate command if not present
        return parseCommand(command.command(), parameterTypes, startParameterIndex);
    }

    public static List<CommandToken> parseCommand(String command, Class<?>[] parameterTypes) {
        return parseCommand(command, parameterTypes, 0);
    }

    public static List<CommandToken> parseCommand(String command, Class<?>[] parameterTypes, int startParameterIndex) {
        List<String> tokens = splitCommandTokens(command);

        return parseCommandTokens(tokens, parameterTypes, startParameterIndex);
    }

    public static List<String> splitCommandTokens(String command) {
        List<String> tokens = new ArrayList<>();

        if (command == null || command.isEmpty())
            return tokens;

        int startIndex = 0;
        int openBracketCount = 0;
        int currentIndex;

        for (currentIndex = 1; currentIndex < command.length(); currentIndex++) {
            char character = command.charAt(currentIndex);
            if (character == '[') openBracketCount++;
            else if (character == ']') openBracketCount--;

            else if (openBracketCount == 0 && character == ' ') {
                tokens.add(command.substring(startIndex, currentIndex));
                startIndex = currentIndex + 1;
            }
        }

        //Add the final word, if there is one.
        if (startIndex != currentIndex)
            tokens.add(command.substring(startIndex, currentIndex));

        return tokens;
    }

    public static List<CommandToken> parseCommandTokens(List<String> tokens, Class<?>[] parameterTypes, int currentTypeIndex) {
        return parseCommandTokens(new ArrayList<>(), tokens, 0, parameterTypes, currentTypeIndex);
    }

    private static List<CommandToken> parseCommandTokens(List<CommandToken> commandTokens, List<String> tokens, int currentTokenIndex, Class<?>[] parameterTypes, int currentTypeIndex) {
        if (currentTokenIndex >= tokens.size())
            return commandTokens;

        String token = tokens.get(currentTokenIndex);
        boolean isOptional = false;

        if (token.matches(TokenType.OPTIONAL.getSyntax())) {
            isOptional = true;
            token = token.substring(1, token.length() - 1);
        }

//        if (token.matches(TokenType.OBJECT.getSyntax())) {
//            String objectType = token.substring(1, token.indexOf("["));
//            String parameters = token.substring(token.indexOf("[") + 1, token.lastIndexOf("]"));
//
//            List<CommandToken> parameterTokens = parseCommandTokens(parameters);
//        }

        if (token.matches(TokenType.TYPE.getSyntax())) {
            if (currentTypeIndex >= parameterTypes.length)
                throw new IllegalCommandException(
                        String.format("The token %s doesn't have a corresponding parameter in the method.", token));

            Class<?> type = parameterTypes[currentTypeIndex];
            boolean isBuiltInType = isBuiltInType(type);

            if (!isValidCommandTypeToken(token, type, isBuiltInType))
                throw new IllegalCommandException(
                        String.format("The token %s doesn't match the corresponding parameter type of %s", token, type));

            TokenType tokenType = isBuiltInType ? TokenType.TYPE : TokenType.OBJECT;
            currentTypeIndex++;

            commandTokens.add(new CommandToken(token, tokenType, type, isOptional));
        }
        else {
            //Just text formatting.
            commandTokens.add(new CommandToken(token, TokenType.TEXT, String.class, isOptional));
        }

        return parseCommandTokens(commandTokens, tokens, currentTokenIndex + 1, parameterTypes, currentTypeIndex);
    }

    public static boolean isValidCommandTypeToken(String token, Class<?> type, boolean isBuiltInType) {
        String tokenIdentifier = token.substring(1);

        if (!isBuiltInType) {
            Optional<CustomParameter> oCustomParameter = Utilities.getAnnotation(type, CustomParameter.class);
            if (oCustomParameter.isPresent())
                return oCustomParameter.get().identifier().equalsIgnoreCase(tokenIdentifier);
        }

        return type.getSimpleName().equalsIgnoreCase(tokenIdentifier);
    }
}
