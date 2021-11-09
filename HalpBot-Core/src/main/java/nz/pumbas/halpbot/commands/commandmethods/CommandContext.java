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

import org.dockbox.hartshorn.core.context.element.TypeContext;
import org.jetbrains.annotations.NotNull;
import java.util.List;
import java.util.Set;

import nz.pumbas.halpbot.actions.methods.Invokable;
import nz.pumbas.halpbot.commands.tokens.Token;
import nz.pumbas.halpbot.permissions.Permissive;

public interface CommandContext extends Invokable, Permissive
{
    /**
     * @return The {@link String alias} for this command.
     */
    @NotNull
    List<String> aliases();

    /**
     * @return The {@link String description} if present, otherwise null
     */
    @NotNull
    String description();

    /**
     * @return The {@link String usage} for this command
     */
    @NotNull
    String usage();

    /**
     * @return The {@link Class classes} that can have static methods invoked from
     */
    @NotNull
    Set<TypeContext<?>> reflections();

    /**
     * @return The {@link Token tokens} making up this command.
     */
    @NotNull
    List<Token> tokens();
}
