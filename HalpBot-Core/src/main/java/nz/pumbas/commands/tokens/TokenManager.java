package nz.pumbas.commands.tokens;

import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import nz.pumbas.commands.ErrorManager;
import nz.pumbas.commands.annotations.Command;
import nz.pumbas.commands.annotations.CustomParameter;
import nz.pumbas.commands.annotations.ParameterConstruction;
import nz.pumbas.commands.annotations.Source;
import nz.pumbas.commands.annotations.Unrequired;
import nz.pumbas.commands.exceptions.IllegalCustomParameterException;
import nz.pumbas.commands.exceptions.IllegalFormatException;
import nz.pumbas.commands.tokens.context.InvocationContext;
import nz.pumbas.commands.tokens.tokentypes.PlaceholderToken;
import nz.pumbas.commands.tokens.tokentypes.SimpleParsingToken;
import nz.pumbas.commands.tokens.tokentypes.Token;
import nz.pumbas.commands.tokens.tokentypes.ParsingToken;
import nz.pumbas.objects.Result;
import nz.pumbas.resources.Resource;
import nz.pumbas.utilities.Reflect;
import nz.pumbas.utilities.enums.Modifiers;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * A static {@link Token} manager, that handles the parsing of commands into {@link Token command tokens}.
 */
public final class TokenManager {

    //TODO: Generate TokenCommand from array of parameter types for custom parameters if no constructor string is
    //      specified.

    private TokenManager() {}

    /**
     * A {@link Map} which maps custom classes to their parsed {@link Constructor} in an {@link TokenCommand}.
     */
    private static final Map<Class<?>, List<TokenCommand>> CustomClassConstructors = new HashMap<>();

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
     * Generates the {@link Token command tokens} for the {@link Constructor constructors} of the
     * {@link Class custom class} and adds it to {@link TokenManager#CustomClassConstructors}.
     *
     * @param customClass
     *      The {@link Class custom class} to parse the {@link Constructor constructors} for
     */
    public static void parseCustomClassConstructors(Class<?> customClass)
    {
        if (CustomClassConstructors.containsKey(customClass))
            return;

        Constructor<?>[] constructors = customClass.getDeclaredConstructors();
        if (0 == constructors.length)
            throw new IllegalCustomParameterException(
                String.format("The custom class %s, must define a constructor", customClass.getSimpleName()));

        List<Constructor<?>> customConstructors = Reflect.filterReflections(constructors,
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
     * Generates the {@link Token command tokens} for the specified {@link Constructor}.
     *
     * @param constructor
     *      The {@link Constructor} to generate the {@link Token command tokens} for
     *
     * @return The generated {@link Token command tokens}
     */
    private static List<Token> parseConstructor(Constructor<?> constructor)
    {
        constructor.setAccessible(true);
        String constructorCommand = Reflect.getAnnotationFieldElse(
            constructor, ParameterConstruction.class,
            ParameterConstruction::constructor, "");

        if (constructorCommand.isEmpty())
            return generateTokens(constructor);
        else
            return generateTokens(constructorCommand, constructor);
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

            //if (Reflect.isAssignableTo(parameterType, CustomParameterMappings.keySet())) continue;

            String command = "#" + getTypeAlias(parameterType);

            if (Reflect.retrieveAnnotation(
                parameterAnnotations[parameterIndex], Unrequired.class).isPresent()) {
                command = "<" + command + ">";
            }

            commandString.add(command);
        }

        return String.join(" ", commandString);
    }

    /**
     * Generates an {@link TokenCommand} from the passed in {@link Object instance} and {@link Method}.
     *
     * @param instance
     *      The {@link Object} that the {@link Method} belongs to
     * @param method
     *      The {@link Method} to make the command from
     *
     * @return A {@link TokenCommand} representing the specified {@link Method}
     */
    public static TokenCommand generateTokenCommand(@NotNull Object instance, @NotNull Method method)
    {
        Set<Class<?>> reflections;
        List<Token> tokens;

        if (method.isAnnotationPresent(Command.class)) {
            Command command = method.getAnnotation(Command.class);
            reflections = Set.of(command.reflections());

            tokens = command.command().isEmpty()
                ? generateTokens(method)
                : generateTokens(command.command(), method);
        }
        else {
            reflections = Collections.emptySet();
            tokens = generateTokens(method);
        }

        return new TokenCommand(instance, method, tokens, reflections);
    }

    //region TypeParsers

    /**
     * Generates a token based on the specified {@link Type} and {@link Annotation annotations}.
     *
     * @param type
     *      The {@link Type} of the token
     * @param annotations
     *      The {@link Annotation annotations} attached to this token
     *
     * @return The generated {@link ParsingToken}
     */
    public static ParsingToken generateToken(@NotNull Type type, @NotNull Annotation[] annotations) {
        return new SimpleParsingToken(type, annotations);
    }

    /**
     * Generates a token based on the specified {@link Type}.
     *
     * @param type
     *      The {@link Type} of the token
     *
     * @return The generated {@link ParsingToken}
     */
    public static ParsingToken generateToken(@NotNull Type type) {
        return new SimpleParsingToken(type, new Annotation[0]);
    }

    /**
     * Generates a {@link List} of {@link Token tokens} corresponding to the parameters in the specified
     * {@link Executable}.
     *
     * @param executable
     *      The {@link Executable} to generate the tokens for
     *
     * @return The generated list of tokens
     */
    public static List<Token> generateTokens(@NotNull Executable executable) {
        List<Token> tokens = new ArrayList<>();
        Type[] parameterTypes = executable.getGenericParameterTypes();
        Annotation[][] parameterAnnotations = executable.getParameterAnnotations();

        for (int i = 0; i < executable.getParameterCount(); i++) {
            tokens.add(generateToken(parameterTypes[i], parameterAnnotations[i]));
        }
        return tokens;
    }

    /**
     * Generates a {@link List} of {@link Token tokens} corresponding to the parameters in the specified
     * {@link Executable} and by the {@link String command}.
     *
     * @param executable
     *      The {@link Executable} to generate the tokens for
     *
     * @return The generated list of tokens
     */
    public static List<Token> generateTokens(@NotNull String command, @NotNull Executable executable) {
        List<Token> tokens = new ArrayList<>();
        List<String> splitCommand = splitCommand(command);

        Class<?>[] parameterClasses = executable.getParameterTypes();
        Type[] parameterTypes = executable.getGenericParameterTypes();
        Annotation[][] parameterAnnotations = executable.getParameterAnnotations();
        int parameterIndex = 0;
        int itemIndex = 0;

        ParsingToken currentToken =
            new SimpleParsingToken(parameterTypes[parameterIndex], parameterAnnotations[parameterIndex]);

        while (itemIndex < splitCommand.size()) {
            String item = splitCommand.get(itemIndex);

            if (item.startsWith("<"))
                tokens.add(new PlaceholderToken(true, item.substring(1, item.length() - 1)));
            else if (item.startsWith("#")) {
                String alias = item.substring(1);
                if (alias.equalsIgnoreCase(getTypeAlias(parameterClasses[parameterIndex]))) {
                    tokens.add(currentToken);
                    itemIndex++;
                }
                else if (currentToken.annotationTypes().contains(Source.class)) {
                    tokens.add(currentToken);
                }
                else throw new IllegalFormatException(
                    "The alias, " + alias + ", doesn't match the expected " + getTypeAlias(parameterClasses[parameterIndex]));

                parameterIndex++;
                currentToken = new SimpleParsingToken(
                    parameterTypes[parameterIndex], parameterAnnotations[parameterIndex]);
            }
            else tokens.add(new PlaceholderToken(false, item));
        }

        return tokens;
    }

    /**
     * Splits the {@link String command} by spaces and <...>.

     * @param command
     *      The {@link String command}
     *
     * @return The split command
     */
    public static List<String> splitCommand(@NotNull String command) {
        List<String> splitCommand = new ArrayList<>();
        int currentIndex = 0;

        while (currentIndex < command.length()) {
            if (' ' == command.charAt(currentIndex))
                currentIndex++;

            else if ('<' == command.charAt(currentIndex)) {
                int endIndex = command.indexOf('>', currentIndex);
                if (-1 == endIndex)
                    throw new IllegalFormatException("Expected ending '>' in command: " + command);

                splitCommand.add(command.substring(currentIndex, endIndex + 1));
                currentIndex = endIndex;
            }
            else {
                int spaceIndex = command.indexOf(' ', currentIndex);
                if (-1 == spaceIndex) {
                    splitCommand.add(command.substring(currentIndex));
                    break;
                }

                splitCommand.add(command.substring(currentIndex, spaceIndex));
                currentIndex = spaceIndex + 1;
            }
        }
        return splitCommand;
    }

    //endregion

    /**
     * Generates a {@link TokenCommand} from the passed in {@link Object instance} and {@link Method} and
     * {@link Command}.
     *
     * @param instance
     *      The {@link Object} that the {@link Method} belongs to
     * @param method
     *      The {@link Method} to make the command from
     * @param command
     *      The {@link Command} for this command
     *
     * @return A {@link TokenCommand} representing the specified {@link Method}
     */
    public static TokenCommand generateTokenCommand(Object instance, Method method, Command command)
    {
        Set<Long> restrictions = getRestrictedToList(command.restrictedTo());
        Set<Class<?>> reflections = new HashSet<>(Arrays.asList(command.reflections()));

        if (instance.getClass().isAnnotationPresent(Command.class)) {
            Command classCommand = instance.getClass().getAnnotation(Command.class);
            restrictions.addAll(getRestrictedToList(classCommand.restrictedTo()));
            reflections.addAll(Set.of(classCommand.reflections()));

        }
        String displayCommand = command.command();

        return new TokenCommand(instance, method,
            displayCommand.isEmpty() ? generateTokens(method) : generateTokens(displayCommand, method),
            displayCommand.isEmpty() ? "Not Defined" : displayCommand,
            command.description(), command.permission(), restrictions, reflections);
    }

    /**
     * Generates an {@link TokenCommand} from the passed in parameters.
     *
     * @param instance
     *      The {@link Object} that the {@link Method} belongs to
     * @param method
     *      The {@link Method} to make the command from
     * @param reflections
     *      The {@link Class classes} that the command can invoke methods from
     *
     * @return A {@link TokenCommand} representing the specified {@link Method}
     */
    public static TokenCommand generateTokenCommand(Object instance, Method method, Set<Class<?>> reflections)
    {
        return new TokenCommand(instance, method, generateTokens(method), reflections);
    }

    /**
     * Retrieves a {@link List} of the ids of who can use this command.
     *
     * @param restrictedTo
     *      The array of user ids of who can use this command
     *
     * @return A {@link List} of the ids of who can use this command.
     */
    private static Set<Long> getRestrictedToList(long[] restrictedTo)
    {
        Set<Long> restrictedToList = new HashSet<>();
        for (long user : restrictedTo)
            restrictedToList.add(user);

        return restrictedToList;
    }

    /**
     * Retrieves the {@link String display command} for the specified {@link Method}.
     *
     * @param method
     *      The {@link Method} to retrieve the display command for
     *
     * @return The {@link String} display command
     */
    public static String retrieveDisplayCommand(Method method)
    {
        Command command = method.getAnnotation(Command.class);
        return null == command || command.command().isEmpty()
            ? generateCommand(method.getParameterTypes(), method.getParameterAnnotations())
            : command.command();
    }

    /**
     * Retrieves an {@link Result} containing the result of any reflection syntax.
     *
     * @param invocationToken
     *      The {@link InvocationContext}
     * @param reflections
     *      The {@link List<Class>} of classes that the reflections can access
     * @param requiredReturnType
     *      The required return type of the reflection
     *
     * @return An {@link Result} containing the result of any reflection syntax.
     */
    public static Result<Object> handleReflectionSyntax(@NotNull InvocationContext invocationToken,
                                                        @NotNull Set<Class<?>> reflections,
                                                        @NotNull Class<?> requiredReturnType)
    {
        if (reflections.isEmpty()) return Result.empty();

        Optional<String> oType = invocationToken.getNext(".");
        if (oType.isEmpty()) return Result.empty();


        Optional<Class<?>> oClass = reflections
            .stream()
            .filter(c -> TokenManager.getTypeAlias(c).equalsIgnoreCase(oType.get()))
            .findFirst();

        if (oClass.isPresent()) {
            Class<?> reflectionClass = oClass.get();

            Optional<String> oMethodName = invocationToken.getNext("[", false);

            return oMethodName.isPresent()
                ? handleMethodReflectionSyntax(invocationToken, oMethodName.get(),
                    reflections, reflectionClass, requiredReturnType)
                : handleFieldReflectionSyntax(invocationToken, reflectionClass,
                requiredReturnType);

        }

        return Result.empty();
    }

    /**
     * Handles {@link Method} reflection syntax invocation and parsing. Note: Only public, static methods may be invoked.
     *
     * @param invocationToken
     *      The {@link InvocationContext}
     * @param methodName
     *      The {@link String name} of the method
     * @param reflections
     *      The {@link List} of classes that reflections can access
     * @param reflectionClass
     *      The {@link Class} that is being referenced in the reflection syntax
     * @param requiredReturnType
     *      The {@link Class} required to be returned
     *
     * @return An {@link Result} containing the result of the invoked method
     */
    private static Result<Object> handleMethodReflectionSyntax(@NotNull InvocationContext invocationToken,
                                                               @NotNull String methodName,
                                                               @NotNull Set<Class<?>> reflections,
                                                               @NotNull Class<?> reflectionClass,
                                                               @NotNull Class<?> requiredReturnType)
    {
        Optional<String> oParameters = invocationToken.getNextSurrounded("[", "]");
        if (oParameters.isEmpty())
            return Result.of(Resource.get("halpbot.commands.reflections.missingclosingbracket", methodName));

        InvocationContext parameters = InvocationContext.of(oParameters.get()).saveState(invocationToken);

        List<TokenCommand> tokenCommands =
            Reflect.getMethods(reflectionClass, methodName, true)
            .stream()
            .filter(m -> requiredReturnType.isAssignableFrom(m.getReturnType()))
            .filter(m -> Reflect.hasModifiers(m, Modifiers.PUBLIC, Modifiers.STATIC))
            .map(m -> generateTokenCommand(null, m, reflections))
            .collect(Collectors.toList());

        if (tokenCommands.isEmpty())
            return Result.of(Resource.get("halpbot.commands.reflections.nomethods", methodName));

        Result<Object> result = Result.empty();
        for (TokenCommand tokenCommand : tokenCommands) {
            result = tokenCommand.parse(parameters);
            if (result.hasValue())
                break;
        }

        return result;
    }

    /**
     * Handles {@link java.lang.reflect.Field} reflection syntax invocation and parsing. Note: Only public, static, final fields can be
     * referenced.
     *
     * @param invocationToken
     *      The {@link InvocationContext}
     * @param reflectionClass
     *      The {@link Class} that is being referenced in the reflection syntax
     * @param requiredReturnType
     *      The {@link Class} required to be returned
     *
     * @return An {@link Result} containing the specified field.
     */
    private static Result<Object> handleFieldReflectionSyntax(@NotNull InvocationContext invocationToken,
                                                              @NotNull Class<?> reflectionClass,
                                                              @NotNull Class<?> requiredReturnType)
    {
        if (!invocationToken.hasNext()) return Result.empty();
        String fieldName = invocationToken.getNext();

        return Result.of(Reflect.getFields(reflectionClass, fieldName)
            .stream()
            .filter(f -> requiredReturnType.isAssignableFrom(f.getType()))
            .filter(f -> Reflect.hasModifiers(f, Modifiers.PUBLIC, Modifiers.STATIC, Modifiers.FINAL))
            .map(f -> {
                try {
                    return f.get(null);
                } catch (IllegalAccessException e) {
                    ErrorManager.handle(e);
                    return new Object();
                }
            })
            .findFirst(), Resource.get("halpbot.commands.reflections.nofields", fieldName));
    }

    /**
     * Generates an individual {@link Token} for the specified {@link Class}.
     *
     * @param type
     *      The {@link Class type} of the command token
     * @param annotations
     *      The {@link Annotation annotations} on the command token
     *
     * @return The generated {@link Token}
     */
    public static Token generateCommandToken(Class<?> type, Annotation[] annotations)
    {
        //TODO: This
        return (Token) new Object();
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
        @NonNls String tokenIdentifier = token.substring(1);
        return token.startsWith("#") && getTypeAlias(type).equalsIgnoreCase(tokenIdentifier);
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
        if (type.isAnnotationPresent(CustomParameter.class))
            return type.getAnnotation(CustomParameter.class).identifier();

        String defaultAlias;
        if (type.isArray())
            defaultAlias = Reflect.wrapPrimative(Reflect.getArrayType(type))
                .getSimpleName() + "[]";
        else defaultAlias = Reflect.wrapPrimative(type).getSimpleName();

        return defaultAlias;
    }
}
