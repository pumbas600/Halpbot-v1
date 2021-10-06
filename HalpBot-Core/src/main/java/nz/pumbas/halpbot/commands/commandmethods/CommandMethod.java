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

package nz.pumbas.halpbot.commands.commandmethods;

import net.dv8tion.jda.api.entities.User;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import nz.pumbas.halpbot.commands.exceptions.OutputException;
import nz.pumbas.halpbot.permissions.PermissionManager;
import nz.pumbas.halpbot.commands.tokens.Token;
import nz.pumbas.halpbot.objects.Exceptional;
import nz.pumbas.halpbot.utilities.HalpbotUtils;

public interface CommandMethod
{

    /**
     * @return The {@link String alias} for this command.
     */
    @NotNull
    String getAlias();

    /**
     * @return The {@link String description} if present, otherwise null
     */
    @NotNull
    String getDescription();

    /**
     * @return The {@link String} representation of the command
     */
    @NotNull
    String getDisplayCommand();

    /**
     * @return The {@link Object} instance for this {@link CommandMethod}. Note for static methods this will be null
     */
    @Nullable
    Object getInstance();

    /**
     * @return The {@link Executable} that's invoked by this command
     */
    @NotNull
    Executable getExecutable();

    /**
     * @return The {@link String permissions} for this command. If there is no permission, this will an empty string
     */
    @NotNull
    String[] getPermissions();

    /**
     * @return The {@link Set<Long> ids} of who this command is restricted to
     */
    @NotNull
    Set<Long> getRestrictedTo();

    /**
     * @return The {@link Class classes} that can have static methods invoked from
     */
    @NotNull
    Set<Class<?>> getReflections();

    /**
     * @return The {@link String usage} for this command
     */
    @NotNull
    String getUsage();

    /**
     * @return The {@link Token tokens} making up this command.
     */
    @NotNull
    List<Token> getTokens();

    /**
     * Returns true if the {@link User author} has all the necessary permissions to use this command.
     *
     * @param author
     *      The author of the commmand
     *
     * @return If the author had permission
     */
    default boolean hasPermission(User author) {
        if (null == author)
            return this.getRestrictedTo().isEmpty() && 0 == this.getPermissions().length;

        return (this.getRestrictedTo().isEmpty() ||
                this.getRestrictedTo().contains(author.getIdLong())) &&
                HalpbotUtils.context().get(PermissionManager.class).hasPermissions(author, this.getPermissions());
    }

    /**
     * Invokes the {@link Method} for this {@link CommandMethod}.
     *
     * @param args
     *     The {@link Object} arguments to be used when invoking this command
     *
     * @return An {@link Optional} containing the output of the {@link Method}
     * @throws OutputException
     *     Any {@link OutputException} thrown during the invocation of the method
     */
    Exceptional<Object> invoke(Object... args) throws OutputException;
}
