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
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

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

    private static final Map<Class<?>, Class<?>> WrapperToPrimitive = Map.of(
            Integer.class,   int.class,
            Float.class,     float.class,
            Double.class,    double.class,
            Character.class, char.class,
            Boolean.class,   boolean.class
    );

    /**
     * An {@link Map} of the built-in {@link Class classes} and a {@link Tuple} of a regex syntax along with their
     * corresponding parsers.
     */
    public static final Map<Class<?>, Tuple<String, Function<String, Object>>> TypeParsers = Map.of(
        String.class,  Tuple.of(".+", s -> s),
        int.class,     Tuple.of("-?\\d+", Integer::parseInt),
        float.class,   Tuple.of("-?\\d+\\.?\\d*", Float::parseFloat),
        double.class,  Tuple.of("-?\\d+\\.?\\d*", Double::parseDouble),
        char.class,    Tuple.of("[a-zA-Z]", s -> s.charAt(0)),
        boolean.class, Tuple.of("true|yes|false|no", s -> "true".equalsIgnoreCase(s) || "yes".equalsIgnoreCase(s))
    );

    /**
     * An {@link Map} which maps custom classes to their parsed {@link Constructor} in an {@link TokenCommand}.
     */
    private static final Map<Class<?>, List<TokenCommand>> CustomClassConstructors = new HashMap<>();

    public static boolean isBuiltInType(Class<?> type)
    {
        type = WrapperToPrimitive.getOrDefault(type, type);
        return type.isEnum() || BuiltInTypes.contains(type);
    }

    /**
     * Retrieves the {@link List} of parsed {@link TokenCommand token commands} for the given {@link Class class's} constructors.
     * If the specified {@link Class} hasn't had their {@link Constructor constructors} parsed, then it calls {@link TokenManager#parseCustomClassConstructors(Class)}
     * and automatically returns the result.
     *
     * @param customClass
     *      The {@link Class} to retrieve the parsed {@link TokenCommand token commands} from.
     *
     * @return The {@link List} of {@link TokenCommand token commands} for the passed {@link Class}
     */
    public static List<TokenCommand> getParsedConstructors(Class<?> customClass)
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

        List<Constructor<?>> customConstructors = Utilities.filterReflections(constructors,
            c -> c.isAnnotationPresent(ParameterConstruction.class));

        if (customConstructors.isEmpty()) {
            customConstructors.add(constructors[0]);
        }

        CustomClassConstructors.put(customClass,
                customConstructors
                        .stream()
                        .map(c -> new TokenCommand(null, c, parseConstructor(c))) //For constructors, an instance isn't required to invoke it.
                        .collect(Collectors.toList()));
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
            constructorCommand = generateCommand(constructor.getParameterTypes(), constructor.getParameterAnnotations());

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
    public static String generateCommand(Class<?>[] parameterTypes, Annotation[][] parameterAnnotations)
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

        String tokenCommand = command.command().isEmpty()
                ? generateCommand(parameterTypes, method.getParameterAnnotations())
                : command.command();

        return parseCommand(tokenCommand, parameterTypes, startParameterIndex, method.getParameterAnnotations());
    }

    public static List<CommandToken> parseCommand(String command, Class<?>[] parameterTypes)
    {
        return parseCommand(command, parameterTypes, 0, new Annotation[parameterTypes.length][0]);
    }

    public static List<CommandToken> parseCommand(String command, Class<?>[] parameterTypes,
                                                  int startParameterIndex, Annotation[][] parameterAnnotations)
    {
        List<String> tokens = splitCommandTokens(command);

        return parseCommandTokens(tokens, parameterTypes, startParameterIndex, parameterAnnotations);
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

    public static List<CommandToken> parseCommandTokens(List<String> tokens, Class<?>[] parameterTypes,
                                                        int currentTypeIndex, Annotation[][] parameterAnnotations)
    {
        return parseCommandTokens(new ArrayList<>(), tokens, 0, parameterTypes, currentTypeIndex, parameterAnnotations);
    }

    private static List<CommandToken> parseCommandTokens(List<CommandToken> commandTokens,
                                                         List<String> tokens, int currentTokenIndex,
                                                         Class<?>[] parameterTypes, int currentTypeIndex,
                                                         Annotation[][] parameterAnnotations)
    {
        if (currentTokenIndex >= tokens.size())
            return commandTokens;

        String token = tokens.get(currentTokenIndex);
        boolean isOptional = false;

        if (token.matches(TokenSyntax.OPTIONAL.getSyntax())) {
            isOptional = true;
            token = token.substring(1, token.length() - 1);
        }

        boolean isMultiChoice = token.matches(TokenSyntax.MULTICHOICE.getSyntax());

        if (token.matches(TokenSyntax.TYPE.getSyntax()) || isMultiChoice) {
            if (currentTypeIndex >= parameterTypes.length)
                throw new IllegalCommandException(
                        String.format("The token %s doesn't have a corresponding parameter in the method.", token));

            Class<?> type = parameterTypes[currentTypeIndex];
            type = WrapperToPrimitive.getOrDefault(type, type);

            String defaultValue = null;
            Optional<Unrequired> oUnrequired = Utilities.retrieveAnnotation(parameterAnnotations[currentTypeIndex], Unrequired.class);
            if (oUnrequired.isPresent()) {
                isOptional = true;
                defaultValue = oUnrequired.get().value();
            }

            if (isMultiChoice) {
                List<String> options = List.of(token.split("\\|"));
                commandTokens.add(new MultiChoiceToken(isOptional, type, defaultValue, options));
            }
            else if (!isValidCommandTypeToken(token, type)) { //Only check this for non-multichoice tokens
                throw new IllegalCommandException(
                        String.format("The token %s doesn't match the corresponding parameter type of %s", token, type));
            }
            else if (isBuiltInType(type)) {
                commandTokens.add(new BuiltInTypeToken(isOptional, type, defaultValue));
            }
            else if (type.isArray()) {
                commandTokens.add(new ArrayToken(isOptional, type, defaultValue));
            }
            else {
                commandTokens.add(new ObjectTypeToken(isOptional, type, defaultValue));
            }
            currentTypeIndex++;
        }

        else {
            //Just text formatting.
            commandTokens.add(new PlaceholderToken(isOptional, token));
        }

        return parseCommandTokens(commandTokens, tokens, currentTokenIndex + 1, parameterTypes, currentTypeIndex, parameterAnnotations);
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
        return getTypeAlias(type).equalsIgnoreCase(tokenIdentifier);
    }

    /**
     * Gets the {@link String alias} of the specified {@link Class type} by first checking if it has the {@link CustomParameter} annotation. If it does, it
     * retrieves the specified {@link String identifier}, otherwise it just gets the name of the {@link Class type}.
     *
     * @param type
     *      The {@link Class type} to get the alias of
     *
     * @return The {@link String alias} of the specified {@link Class type}
     */
    public static String getTypeAlias(Class<?> type)
    {
        return type.isAnnotationPresent(CustomParameter.class)
                ? type.getAnnotation(CustomParameter.class).identifier()
                : type.getSimpleName();
    }
}
