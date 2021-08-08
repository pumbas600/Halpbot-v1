package nz.pumbas.halpbot.commands.tokens;

import org.jetbrains.annotations.NotNull;

import nz.pumbas.halpbot.commands.ErrorManager;
import nz.pumbas.halpbot.commands.annotations.Source;
import nz.pumbas.halpbot.commands.annotations.Command;
import nz.pumbas.halpbot.commands.annotations.CustomParameter;
import nz.pumbas.halpbot.commands.annotations.ParameterConstruction;
import nz.pumbas.halpbot.commands.exceptions.IllegalCustomParameterException;
import nz.pumbas.halpbot.commands.exceptions.IllegalFormatException;
import nz.pumbas.halpbot.commands.exceptions.TokenCommandException;
import nz.pumbas.halpbot.commands.tokens.context.InvocationContext;
import nz.pumbas.halpbot.commands.tokens.context.MethodContext;
import nz.pumbas.halpbot.commands.tokens.tokentypes.PlaceholderToken;
import nz.pumbas.halpbot.commands.tokens.tokentypes.SimpleParsingToken;
import nz.pumbas.halpbot.commands.tokens.tokentypes.Token;
import nz.pumbas.halpbot.commands.tokens.tokentypes.ParsingToken;
import nz.pumbas.halpbot.utilities.Exceptional;
import nz.pumbas.halpbot.utilities.Reflect;
import nz.pumbas.halpbot.utilities.enums.Modifiers;

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
public final class TokenManager
{

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
     *     The {@link Class} to retrieve the parsed {@link TokenCommand token commands} from.
     *
     * @return The {@link List} of {@link TokenCommand token commands} for the passed {@link Class}
     */
    public static List<TokenCommand> getParsedConstructors(Class<?> customClass) {
        if (!CustomClassConstructors.containsKey(customClass))
            parseCustomClassConstructors(customClass);
        return CustomClassConstructors.get(customClass);
    }

    /**
     * Generates the {@link Token command tokens} for the {@link Constructor constructors} of the
     * {@link Class custom class} and adds it to {@link TokenManager#CustomClassConstructors}.
     *
     * @param customClass
     *     The {@link Class custom class} to parse the {@link Constructor constructors} for
     */
    public static void parseCustomClassConstructors(Class<?> customClass) {
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
     *     The {@link Constructor} to generate the {@link Token command tokens} for
     *
     * @return The generated {@link Token command tokens}
     */
    private static List<Token> parseConstructor(Constructor<?> constructor) {
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
     * Generates an {@link TokenCommand} from the passed in {@link Object instance} and {@link Method}.
     *
     * @param instance
     *     The {@link Object} that the {@link Method} belongs to
     * @param method
     *     The {@link Method} to make the command from
     *
     * @return A {@link TokenCommand} representing the specified {@link Method}
     */
    public static TokenCommand generateTokenCommand(@NotNull Object instance, @NotNull Method method) {
        Set<Class<?>> reflections;
        List<Token> tokens;

        if (method.isAnnotationPresent(Command.class)) {
            Command command = method.getAnnotation(Command.class);
            reflections = Set.of(command.reflections());

            tokens = command.command().isEmpty()
                ? generateTokens(method)
                : generateTokens(command.command(), method);
        } else {
            reflections = Collections.emptySet();
            tokens = generateTokens(method);
        }

        return new TokenCommand(instance, method, tokens, reflections);
    }

    /**
     * Generates a token based on the specified {@link Type} and {@link Annotation annotations}.
     *
     * @param type
     *     The {@link Type} of the token
     * @param annotations
     *     The {@link Annotation annotations} attached to this token
     *
     * @return The generated {@link ParsingToken}
     */
    public static ParsingToken generateToken(@NotNull Type type, @NotNull Annotation[] annotations) {
        return new SimpleParsingToken(type, annotations);
    }

    /**
     * Generates a {@link List} of {@link Token tokens} corresponding to the parameters in the specified
     * {@link Executable}.
     *
     * @param executable
     *     The {@link Executable} to generate the tokens for
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
     *     The {@link Executable} to generate the tokens for
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

        while (itemIndex < splitCommand.size()) {
            ParsingToken currentToken =
                new SimpleParsingToken(parameterTypes[parameterIndex], parameterAnnotations[parameterIndex]);
            String item = splitCommand.get(itemIndex);

            if (item.startsWith("<")) {
                tokens.add(new PlaceholderToken(true, item.substring(1, item.length() - 1)));
                itemIndex++;
            } else if (item.startsWith("#")) {
                String alias = item.substring(1);
                if (alias.equalsIgnoreCase(getTypeAlias(parameterClasses[parameterIndex]))) {
                    tokens.add(currentToken);
                    itemIndex++;
                } else if (currentToken.getAnnotationTypes().contains(Source.class)) {
                    tokens.add(currentToken);
                } else throw new IllegalFormatException(
                    "The alias '" + alias + "', doesn't match the expected " + getTypeAlias(parameterClasses[parameterIndex]));
                parameterIndex++;
            } else {
                tokens.add(new PlaceholderToken(false, item));
                itemIndex++;
            }
        }

        return tokens;
    }

    /**
     * Splits the {@link String command} by spaces and <...>.
     *
     * @param command
     *     The {@link String command}
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
                currentIndex = endIndex + 1;
            } else {
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

    /**
     * Generates a {@link TokenCommand} from the passed in {@link Object instance} and {@link Method} and
     * {@link Command}.
     *
     * @param instance
     *     The {@link Object} that the {@link Method} belongs to
     * @param method
     *     The {@link Method} to make the command from
     * @param command
     *     The {@link Command} for this command
     *
     * @return A {@link TokenCommand} representing the specified {@link Method}
     */
    public static TokenCommand generateTokenCommand(Object instance, Method method, Command command) {
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
     *     The {@link Object} that the {@link Method} belongs to
     * @param method
     *     The {@link Method} to make the command from
     * @param reflections
     *     The {@link Class classes} that the command can invoke methods from
     *
     * @return A {@link TokenCommand} representing the specified {@link Method}
     */
    public static TokenCommand generateTokenCommand(Object instance, Method method, Set<Class<?>> reflections) {
        return new TokenCommand(instance, method, generateTokens(method), reflections);
    }

    /**
     * Retrieves a {@link List} of the ids of who can use this command.
     *
     * @param restrictedTo
     *     The array of user ids of who can use this command
     *
     * @return A {@link List} of the ids of who can use this command.
     */
    private static Set<Long> getRestrictedToList(long[] restrictedTo) {
        Set<Long> restrictedToList = new HashSet<>();
        for (long user : restrictedTo)
            restrictedToList.add(user);

        return restrictedToList;
    }

    /**
     * Retrieves an {@link Exceptional} containing the result of any reflection syntax.
     *
     * @param ctx
     *     The {@link MethodContext}
     *
     * @return An {@link Exceptional} containing the result of any reflection syntax.
     */
    public static Exceptional<Object> handleReflectionSyntax(@NotNull MethodContext ctx) {
        if (ctx.getReflections().isEmpty()) return Exceptional.empty();

        Exceptional<String> type = ctx.getNext(".");
        if (type.present()) {

            Optional<Class<?>> oClass = ctx.getReflections()
                .stream()
                .filter(c -> TokenManager.getTypeAlias(c).equalsIgnoreCase(type.get()))
                .findFirst();

            if (oClass.isPresent()) {
                Class<?> reflectionClass = oClass.get();

                Exceptional<String> methodName = ctx.getNext("[", false);

                return methodName
                    .map(name -> handleMethodReflectionSyntax(ctx, name, reflectionClass))
                    .or(handleFieldReflectionSyntax(ctx, reflectionClass));
            }
        }

        return Exceptional.empty();
    }

    /**
     * Handles {@link Method} reflection syntax invocation and parsing. Note: Only public, static methods may be invoked.
     *
     * @param ctx
     *     The {@link MethodContext}
     * @param methodName
     *     The {@link String name} of the method
     * @param reflectionClass
     *     The {@link Class} that is being referenced in the reflection syntax
     *
     * @return An {@link Exceptional} containing the result of the invoked method
     */
    private static Exceptional<Object> handleMethodReflectionSyntax(@NotNull MethodContext ctx,
                                                                    @NotNull String methodName,
                                                                    @NotNull Class<?> reflectionClass) {
        if (!ctx.isNext('[', true))
            return Exceptional.of(new IllegalFormatException("Expected the character ["));

        List<TokenCommand> tokenCommands =
            Reflect.getMethods(reflectionClass, methodName, true)
                .stream()
                .filter(m -> ctx.getContextState().getClazz().isAssignableFrom(m.getReturnType()))
                .filter(m -> Reflect.hasModifiers(m, Modifiers.PUBLIC, Modifiers.STATIC))
                .map(m -> generateTokenCommand(null, m, ctx.getReflections()))
                .collect(Collectors.toList());

        if (tokenCommands.isEmpty())
            return Exceptional.of(() -> new TokenCommandException("There were no methods with the name " + methodName));

        Exceptional<Object> result = Exceptional.empty();
        for (TokenCommand tokenCommand : tokenCommands) {
            result = tokenCommand.parse(ctx, true);
            if (result.present())
                break;
        }

        if (!ctx.isNext(']', true))
            return Exceptional.of(new IllegalFormatException("Expected the character ]"));
        return result;
    }

    /**
     * Handles {@link java.lang.reflect.Field} reflection syntax invocation and parsing. Note: Only public, static, final fields can be
     * referenced.
     *
     * @param ctx
     *     The {@link InvocationContext}
     * @param reflectionClass
     *     The {@link Class} that is being referenced in the reflection syntax
     *
     * @return An {@link Exceptional} containing the specified field.
     */
    private static Exceptional<Object> handleFieldReflectionSyntax(@NotNull MethodContext ctx,
                                                                   @NotNull Class<?> reflectionClass) {
        if (!ctx.hasNext()) return Exceptional.empty();
        String fieldName = ctx.getNext();

        return Exceptional.of(Reflect.getFields(reflectionClass, fieldName)
            .stream()
            .filter(f -> ctx.getContextState().getClazz().isAssignableFrom(f.getType()))
            .filter(f -> Reflect.hasModifiers(f, Modifiers.PUBLIC, Modifiers.STATIC, Modifiers.FINAL))
            .map(f -> {
                try {
                    return f.get(null);
                } catch (IllegalAccessException e) {
                    ErrorManager.handle(e);
                    return new Object();
                }
            })
            .findFirst());
    }

    /**
     * Gets the {@link String alias} of the specified {@link Class type} by first checking if it has the {@link CustomParameter} annotation. If it does, it
     * retrieves the specified {@link String identifier}, otherwise it just gets the name of the {@link Class type}.
     *
     * @param type
     *     The {@link Class type} to get the alias of
     *
     * @return The {@link String alias} of the specified {@link Class type}
     */
    public static String getTypeAlias(Class<?> type) {
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
