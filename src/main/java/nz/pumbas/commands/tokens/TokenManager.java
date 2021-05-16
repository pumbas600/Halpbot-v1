package nz.pumbas.commands.tokens;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import nz.pumbas.commands.Annotations.Command;
import nz.pumbas.commands.Annotations.CustomParameter;
import nz.pumbas.commands.Exceptions.IllegalCommandException;
import nz.pumbas.utilities.Utilities;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * A static {@link CommandToken} manager, that handles the parsing of commands into {@link CommandToken command tokens}.
 */
public final class TokenManager {

    private TokenManager() {}

    /**
     * A {@link List} of the built-in {@link Class classes}.
     */
    public static final List<Class<?>> BuiltInTypes = List.of(
            int.class, float.class, double.class, char.class, String.class
    );

    public static List<GenericCommandToken> parseCommand(Method method) {
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

    public static List<GenericCommandToken> parseCommand(String command, Class<?>[] parameterTypes) {
        return parseCommand(command, parameterTypes, 0);
    }

    public static List<GenericCommandToken> parseCommand(String command, Class<?>[] parameterTypes, int startParameterIndex) {
        String[] tokens = command.split(" ");

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

    public static List<GenericCommandToken> parseCommandTokens(String[] tokens, Class<?>[] parameterTypes, int currentTypeIndex) {
        return parseCommandTokens(new ArrayList<>(), tokens, 0, parameterTypes, currentTypeIndex);
    }

    private static List<GenericCommandToken> parseCommandTokens(List<GenericCommandToken> genericCommandTokens, String[] tokens, int currentTokenIndex, Class<?>[] parameterTypes, int currentTypeIndex) {
        if (currentTokenIndex >= tokens.length)
            return genericCommandTokens;

        String token = tokens[currentTokenIndex];
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
            boolean isBuiltInType = BuiltInTypes.contains(type);

            if (!isValidCommandTypeToken(token, type))
                throw new IllegalCommandException(
                        String.format("The token %s doesn't match the corresponding parameter type of %s", token, type));

            TokenType tokenType = isBuiltInType ? TokenType.TYPE : TokenType.OBJECT;
            currentTypeIndex++;

            genericCommandTokens.add(new GenericCommandToken(token, tokenType, type, isOptional));
        }
        else {
            //Just text formatting.
            genericCommandTokens.add(new GenericCommandToken(token, TokenType.TEXT, String.class, isOptional));
        }

        return parseCommandTokens(genericCommandTokens, tokens, currentTokenIndex + 1, parameterTypes, currentTypeIndex);
    }

    /**
     * Returns if the {@link String parsing token} matches the required {@link Class type} of the method parameter. This will check the {@link Class} for an {@link CustomParameter}
     * and use the specified identifier if present, otherwise, it will check that the {@link Class type's} name matches the {@link String parsing token}.
     *
     * @param token
     *      The {@link String} representation of an {@link ParsingToken}, in the format #type
     *
     * @param type
     *      The required {@link Class type} of the {@link ParsingToken}
     *
     * @return if the {@link String parsing token} is valid
     */
    public static boolean isValidCommandTypeToken(String token, Class<?> type) {
        String tokenIdentifier = token.substring(1);

        return Utilities.getAnnotation(type, CustomParameter.class)
                .map(customParameter -> customParameter.identifier().equalsIgnoreCase(tokenIdentifier))
                .orElseGet(() -> type.getSimpleName().equalsIgnoreCase(tokenIdentifier));
    }
}
