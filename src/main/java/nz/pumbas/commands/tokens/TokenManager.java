package nz.pumbas.commands.tokens;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import org.jetbrains.annotations.NotNull;

import nz.pumbas.commands.Annotations.Command;
import nz.pumbas.commands.Annotations.CustomParameter;
import nz.pumbas.commands.Exceptions.IllegalCommandException;
import nz.pumbas.objects.Tuple;
import nz.pumbas.utilities.Utilities;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

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

    /**
     * An {@link Map} of the built-in {@link Class classes} and a {@link Tuple} of a regex syntax along with their
     * corresponding parsers.
     */
    public static final Map<Class<?>, Tuple<String, Function<String, Object>>> TypeParsers = Map.of(
        String.class, Tuple.of(".+", s -> s),
        int.class,    Tuple.of("-?\\d+", Integer::parseInt),
        float.class,  Tuple.of("-?\\d+\\.?\\d*", Float::parseFloat),
        double.class, Tuple.of("-?\\d+\\.?\\d*", Double::parseDouble),
        char.class,   Tuple.of("[a-zA-Z]", s -> s.charAt(0))
    );

    public static List<CommandToken> parseCommand(Method method)
    {
        if (!Utilities.hasAnnotation(method, Command.class))
            throw new IllegalCommandException(
                    String.format("Cannot parse the method %s as it isn't annotated with Command", method.getName()));

        Command command = method.getAnnotation(Command.class);
        Class<?>[] parameterTypes = method.getParameterTypes();
        int startParameterIndex = 0;

        if (0 < parameterTypes.length && parameterTypes[0].isAssignableFrom(MessageReceivedEvent.class))
            startParameterIndex = 1;

        //TODO: Automatically generate command if not present
        return parseCommand(command.command(), parameterTypes, startParameterIndex);
    }

    public static List<CommandToken> parseCommand(String command, Class<?>[] parameterTypes)
    {
        return parseCommand(command, parameterTypes, 0);
    }

    public static List<CommandToken> parseCommand(String command, Class<?>[] parameterTypes, int startParameterIndex)
    {
        List<String> tokens = splitCommandTokens(command);

        return parseCommandTokens(tokens, parameterTypes, startParameterIndex);
    }

    public static List<String> splitInvocationTokens(@NotNull String command)
    {
        List<String> tokens = new ArrayList<>();

        if (command.isEmpty())
            return tokens;

        int startIndex = 0;
        int openBracketCount = 0;
        int currentIndex;

        for (currentIndex = 1; currentIndex < command.length(); currentIndex++) {
            char character = command.charAt(currentIndex);
            if ('[' == character) openBracketCount++;
            else if (']' == character) openBracketCount--;

            else if (0 == openBracketCount && ' ' == character) {
                tokens.add(command.substring(startIndex, currentIndex));
                startIndex = currentIndex + 1;
            }
        }

        //Add the final word, if there is one.
        if (startIndex != currentIndex)
            tokens.add(command.substring(startIndex, currentIndex));

        return tokens;
    }

    public static List<String> splitCommandTokens(@NotNull String command)
    {
        List<String> tokens = new ArrayList<>();

        if (command.isEmpty())
            return tokens;

        String[] temporaryTokens = command.split(" ");

        //Converts <my name is> to: <my> <name> <is> when splitting the command into tokens.
        boolean isOptional = false;
        for (String token : temporaryTokens) {
            if (isOptional) {
                if (!token.startsWith("<"))
                    token = '<' + token;
                if (!token.endsWith(">"))
                    token += '>';
                else
                    isOptional = false;
            }
            else if (token.startsWith("<") && !token.endsWith(">")) {
                isOptional = true;
                token += '>';
            }
            tokens.add(token);
        }

        return tokens;
    }

    public static List<CommandToken> parseCommandTokens(List<String> tokens, Class<?>[] parameterTypes, int currentTypeIndex)
    {
        return parseCommandTokens(new ArrayList<>(), tokens, 0, parameterTypes, currentTypeIndex);
    }

    private static List<CommandToken> parseCommandTokens(List<CommandToken> commandTokens,
                                                         List<String> tokens, int currentTokenIndex,
                                                         Class<?>[] parameterTypes, int currentTypeIndex)
    {
        if (currentTokenIndex >= tokens.size())
            return commandTokens;

        String token = tokens.get(currentTokenIndex);
        boolean isOptional = false;

        if (token.matches(TokenSyntax.OPTIONAL.getSyntax())) {
            isOptional = true;
            token = token.substring(1, token.length() - 1);
        }

//        if (token.matches(TokenType.OBJECT.getSyntax())) {
//            String objectType = token.substring(1, token.indexOf("["));
//            String parameters = token.substring(token.indexOf("[") + 1, token.lastIndexOf("]"));
//
//            List<CommandToken> parameterTokens = parseCommandTokens(parameters);
//        }

        if (token.matches(TokenSyntax.TYPE.getSyntax())) {
            if (currentTypeIndex >= parameterTypes.length)
                throw new IllegalCommandException(
                        String.format("The token %s doesn't have a corresponding parameter in the method.", token));

            Class<?> type = parameterTypes[currentTypeIndex];
            boolean isBuiltInType = BuiltInTypes.contains(type);

            if (!isValidCommandTypeToken(token, type))
                throw new IllegalCommandException(
                        String.format("The token %s doesn't match the corresponding parameter type of %s", token, type));

            TokenSyntax tokenSyntax = isBuiltInType ? TokenSyntax.TYPE : TokenSyntax.OBJECT;
            currentTypeIndex++;

            commandTokens.add(new GenericCommandToken(token, tokenSyntax, type, isOptional));
        }
        else {
            //Just text formatting.
            commandTokens.add(new PlaceholderToken(isOptional, token));
        }

        return parseCommandTokens(commandTokens, tokens, currentTokenIndex + 1, parameterTypes, currentTypeIndex);
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
