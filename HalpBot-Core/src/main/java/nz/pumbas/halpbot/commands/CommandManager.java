/*
 * MIT License
 *
 * Copyright (c) 2021 pumbas600
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package nz.pumbas.halpbot.commands;

import org.dockbox.hartshorn.core.context.element.AccessModifier;
import org.dockbox.hartshorn.core.context.element.MethodContext;
import org.dockbox.hartshorn.core.context.element.TypeContext;
import org.dockbox.hartshorn.core.domain.Exceptional;
import org.jetbrains.annotations.NotNull;

import nz.pumbas.halpbot.commands.context.InvocationContext;
import nz.pumbas.halpbot.commands.context.parsing.CommandParsingContext;
import nz.pumbas.halpbot.commands.exceptions.IllegalFormatException;
import nz.pumbas.halpbot.commands.exceptions.CommandException;
import nz.pumbas.halpbot.converters.tokens.HalpbotPlaceholderToken;
import nz.pumbas.halpbot.converters.tokens.Token;
import nz.pumbas.halpbot.converters.tokens.ParsingToken;

import java.lang.annotation.Annotation;
import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A static {@link Token} manager, that handles the parsing of commands into {@link Token command tokens}.
 */
public final class CommandManager
{

    private CommandManager() {}

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
        Parameter[] parameters = executable.getParameters();
        Type[] parameterTypes = executable.getGenericParameterTypes();
        Annotation[][] parameterAnnotations = executable.getParameterAnnotations();
        int parameterIndex = 0;
        int itemIndex = 0;

        while (itemIndex < splitCommand.size()) {
            ParsingToken currentToken = null;
//                new SimpleParsingToken(parameterTypes[parameterIndex], parameterAnnotations[parameterIndex],
//                    parameters[parameterIndex].getName());
            String item = splitCommand.get(itemIndex);

            if (item.startsWith("<")) {
                tokens.add(new HalpbotPlaceholderToken(true, item.substring(1, item.length() - 1)));
                itemIndex++;
            } else if (item.startsWith("#")) {
                String alias = item.substring(1);
//                if (alias.equalsIgnoreCase(getTypeAlias(parameterClasses[parameterIndex]))) {
//                    tokens.add(currentToken);
//                    itemIndex++;
////                } else if (currentToken.annotationTypes().contains(Source.class)) {
////                    tokens.add(currentToken);
//                } else throw new IllegalFormatException(
//                    "The alias '" + alias + "', doesn't match the expected " + getTypeAlias(parameterClasses[parameterIndex]));
                parameterIndex++;
            } else {
                tokens.add(new HalpbotPlaceholderToken(false, item));
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

    public static String generateUsage(Executable executable) {
        StringBuilder builder = new StringBuilder();
        Parameter[] parameters = executable.getParameters();
        Annotation[][] annotations = executable.getParameterAnnotations();

        for (int i = 0; i < parameters.length; i++) {
//            if (isCommandParameter(parameters[i].getType(), annotations[i])) {
//
//                builder.append('<')
//                    .append(parameters[i].getName())
//                    .append('{')
//                    .append(getTypeAlias(parameters[i].getType()))
//                    .append("}> ");
//            }
        }
        // Removes the ending space.
        if (!builder.isEmpty())
            builder.deleteCharAt(builder.length() - 1);

        return builder.toString();
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

    //TODO: Move this somewhere else and rework it
    /**
     * Retrieves an {@link Exceptional} containing the result of any reflection syntax.
     *
     * @param invocationContext
     *     The {@link MethodContext}
     *
     * @return An {@link Exceptional} containing the result of any reflection syntax.
     */
    public static Exceptional<Object> handleReflectionSyntax(InvocationContext invocationContext) {
        return Exceptional.of(CommandParsingContext.IGNORE_RESULT);

//        if (invocationContext.reflections().isEmpty()) return Exceptional.empty();
//
//        Exceptional<String> type = invocationContext.next(".");
//        if (type.present()) {
//
//            Optional<TypeContext<?>> oClass = invocationContext.reflections()
//                .stream()
//                //.filter(typeContext -> CommandManager.getTypeAlias(typeContext.type()).equalsIgnoreCase(type.get()))
//                .findFirst();
//
//            if (oClass.isPresent()) {
//                TypeContext<?> reflectionType = oClass.get();
//
//                Exceptional<String> methodName = invocationContext.next("[", false);
//
//                return methodName
//                    .flatMap(name -> handleMethodReflectionSyntax(invocationContext, name, reflectionType))
//                    .orElse(() -> handleFieldReflectionSyntax(invocationContext, reflectionType));
//            }
//        }
//
//        return Exceptional.empty();
    }

    /**
     * Handles {@link Method} reflection syntax invocation and parsing. Note: Only public, static methods may be invoked.
     *
     * @param invocationContext
     *     The {@link MethodContext}
     * @param methodName
     *     The {@link String name} of the method
     * @param reflectionType
     *     The {@link Class} that is being referenced in the reflection syntax
     *
     * @return An {@link Exceptional} containing the result of the invoked method
     */
    private static Exceptional<Object> handleMethodReflectionSyntax(InvocationContext invocationContext,
                                                                    String methodName,
                                                                    TypeContext<?> reflectionType) {
        if (!invocationContext.isNext('[', true))
            return Exceptional.of(new IllegalFormatException("Expected the character ["));

        Exceptional<? extends MethodContext<?, ?>> eMethodContext = reflectionType.method(methodName);
        if (eMethodContext.absent())
            return Exceptional.of(new CommandException("There were no methods with the name " + methodName));

        MethodContext<?, ?> methodContext = eMethodContext.get();
        if (methodContext.isPublic() && methodContext.has(AccessModifier.STATIC)
                && methodContext.returnType().childOf(invocationContext.currentType()))
        {

        }


        Exceptional<Object> result = Exceptional.empty();
//        for (SimpleCommand simpleCommand : simpleCommands) {
//            result = simpleCommand.parse(invocationContext, true);
//            if (result.present())
//                break;
//        }

        if (!invocationContext.isNext(']', true))
            return Exceptional.of(new IllegalFormatException("Expected the character ]"));
        return result;
    }

    /**
     * Handles {@link java.lang.reflect.Field} reflection syntax invocation and parsing. Note: Only public, static, final fields can be
     * referenced.
     *
     * @param invocationContext
     *     The {@link InvocationContext}
     * @param reflectionClass
     *     The {@link Class} that is being referenced in the reflection syntax
     *
     * @return An {@link Exceptional} containing the specified field.
     */
    private static Exceptional<Object> handleFieldReflectionSyntax(@NotNull InvocationContext invocationContext,
                                                                   @NotNull TypeContext<?> reflectionClass) {
        if (!invocationContext.hasNext()) return Exceptional.empty();
        String fieldName = invocationContext.next();
        return Exceptional.empty();

//        return Exceptional.of(Reflect.getFields(reflectionClass, fieldName)
//            .stream()
//            .filter(f -> invocationContext.currentType().type().isAssignableFrom(f.getType()))
//            .filter(f -> Reflect.hasModifiers(f, Modifiers.PUBLIC, Modifiers.STATIC, Modifiers.FINAL))
//            .map(f -> {
//                try {
//                    return f.get(null);
//                } catch (IllegalAccessException e) {
//                    ErrorManager.handle(e);
//                    return new Object();
//                }
//            })
//            .findFirst());
    }
}
