package nz.pumbas.commands.tokens;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import org.jetbrains.annotations.NotNull;

import nz.pumbas.commands.Annotations.Command;
import nz.pumbas.commands.Annotations.CustomParameter;
import nz.pumbas.commands.Annotations.ParameterConstruction;
import nz.pumbas.commands.Annotations.Unrequired;
import nz.pumbas.commands.CommandManager;
import nz.pumbas.commands.ConstructorPair;
import nz.pumbas.commands.CustomParameterType;
import nz.pumbas.commands.Exceptions.IllegalCommandException;
import nz.pumbas.commands.Exceptions.IllegalCustomParameterException;
import nz.pumbas.objects.Tuple;
import nz.pumbas.utilities.Utilities;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
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
            int.class, float.class, double.class, char.class, String.class, boolean.class
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
        char.class,   Tuple.of("[a-zA-Z]", s -> s.charAt(0)),
        boolean.class,Tuple.of("true|yes|false|no", s -> "true".equalsIgnoreCase(s) || "yes".equalsIgnoreCase(s))
    );

    /**
     * An {@link Map} which maps custom classes to their parsed {@link Constructor}.
     */
    private static final Map<Class<?>, List<Tuple<Constructor<?>, List<CommandToken>>>> CustomClassConstructors =
        new HashMap<>();

    /**
     * Retrieves the {@link List} of parsed {@link Constructor constructors} and their {@link CommandToken command
     * tokens} for the specified {@link Class}. If the specified {@link Class} hasn't had their {@link Constructor
     * constructors} parsed, then it calls {@link TokenManager#parseCustomClassConstructors(Class)} and automatically
     * returns the result.
     *
     * @param customClass
     *      The {@link Class} to retrieve the parsed data from.
     *
     * @return The {@link List} of parsed {@link Constructor constructors} and their {@link CommandToken command tokens}
     */
    public static List<Tuple<Constructor<?>, List<CommandToken>>> getParsedConstructors(Class<?> customClass)
    {
        if (!CustomClassConstructors.containsKey(customClass))
            parseCustomClassConstructors(customClass);
        return CustomClassConstructors.get(customClass);
    }

    /**
     * Generates the {@link CommandToken command tokens} for the {@link Constructor constructors} of the
     * {@link Class custom class} and adds it to {@link TokenManager#CustomClassConstructors}.
     *
     * @param customClass
     *      The {@link Class custom class} to parse the {@link Constructor constructors} for
     */
    public static void parseCustomClassConstructors(Class<?> customClass)
    {
        if (BuiltInTypes.contains(customClass))
            throw new IllegalArgumentException(
                String.format("The class %s is a built in type.", customClass.getSimpleName()));

        if (CustomClassConstructors.containsKey(customClass))
            return;

        Constructor<?>[] constructors = customClass.getDeclaredConstructors();
        if (0 == constructors.length)
            throw new IllegalCustomParameterException(
                String.format("The custom class %s, must define a constructor", customClass.getSimpleName()));

        List<Tuple<Constructor<?>, List<CommandToken>>> parsedConstructors = new ArrayList<>();
        List<Constructor<?>> customConstructors = Utilities.filterReflections(constructors,
            c -> c.isAnnotationPresent(ParameterConstruction.class));

        if (customConstructors.isEmpty()) {
            customConstructors.add(constructors[0]);
        }
        customConstructors.forEach(c -> parsedConstructors.add(Tuple.of(c, parseConstructor(c))));
        CustomClassConstructors.put(customClass, parsedConstructors);
    }

    /**
     * Generates the {@link CommandToken command tokens} for the specified {@link Constructor}.
     *
     * @param constructor
     *      The {@link Constructor} to generate the {@link CommandToken command tokens} for
     *
     * @return The generated {@link CommandToken command tokens}
     */
    private static List<CommandToken> parseConstructor(Constructor<?> constructor)
    {
        constructor.setAccessible(true);
        String constructorCommand = Utilities.getAnnotationFieldElse(constructor, ParameterConstruction.class,
            ParameterConstruction::constructor, "");

        if (constructorCommand.isEmpty())
            constructorCommand = generateCommand(constructor.getParameterAnnotations(),
                constructor.getParameterTypes());

        return parseCommand(constructorCommand, constructor.getParameterTypes());
    }

    /**
     * Generates the {@link String command} from an array of {@link Class parameter types} and a 2D
     * array of {@link Annotation annotations}.
     *
     * @param parameterAnnotations
     *      A 2D array of {@link Annotation annotations}, with an array for each of the {@link Class} parameter types
     * @param parameterTypes
     *      The {@link Class parameter types} for the command {@link Method}
     *
     * @return The generating {@link String command}
     */
    public static String generateCommand(Annotation[][] parameterAnnotations, Class<?>[] parameterTypes)
    {
        List<String> commandString = new ArrayList<>();
        for (int parameterIndex = 0; parameterIndex < parameterTypes.length; parameterIndex++) {
            Class<?> parameterType = parameterTypes[parameterIndex];

            if (parameterType.isAssignableFrom(MessageReceivedEvent.class)) continue;

            String command = "#" + parameterType.getSimpleName();

            if (Utilities.retrieveAnnotation(
                parameterAnnotations[parameterIndex], Unrequired.class).isPresent()) {
                command = "<" + command + ">";
            }

            commandString.add(command);
        }

        return String.join(" ", commandString);
    }

    public static List<CommandToken> parseCommand(Method method)
    {
        if (!method.isAnnotationPresent(Command.class))
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
            if (!isValidCommandTypeToken(token, type))
                throw new IllegalCommandException(
                    String.format("The token %s doesn't match the corresponding parameter type of %s", token, type));

            boolean isBuiltInType = BuiltInTypes.contains(type);
            if (isBuiltInType || type.isEnum()) {
                commandTokens.add(new BuiltInTypeToken(isOptional, type));
            }
            else {
                commandTokens.add(new ObjectTypeToken(isOptional, type));
            }
            currentTypeIndex++;
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
