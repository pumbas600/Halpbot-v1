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

package nz.pumbas.halpbot.commands.commandadapters;

import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;

import nz.pumbas.halpbot.commands.commandmethods.CommandMethod;
import nz.pumbas.halpbot.commands.annotations.Command;
import nz.pumbas.halpbot.commands.exceptions.OutputException;
import nz.pumbas.halpbot.commands.exceptions.TokenCommandException;
import nz.pumbas.halpbot.commands.commandmethods.SimpleCommand;
import nz.pumbas.halpbot.commands.tokens.CommandManager;
import nz.pumbas.halpbot.commands.context.MethodContext;
import nz.pumbas.halpbot.objects.Exceptional;

public class SimpleCommandAdapter extends AbstractCommandAdapter
{
    public SimpleCommandAdapter(@Nullable JDABuilder builder, String commandPrefix) {
        super(builder, commandPrefix);
    }

    /**
     * Creates a {@link CommandMethod} from the specified information.
     *
     * @param instance
     *     The {@link Object} that the {@link Method} belongs to
     * @param method
     *     The {@link Method} for this {@link CommandMethod}
     * @param command
     *     The {@link Command} which annotates the {@link Method}
     *
     * @return The created {@link CommandMethod}
     */
    @Override
    protected CommandMethod createCommandMethod(@NotNull Object instance,
                                                @NotNull Method method,
                                                @NotNull Command command) {
        return CommandManager.generateCommandMethod(instance, method, command);
    }

    /**
     * Handles the invocation, matching and parsing of a {@link CommandMethod} with the given {@link String content}.
     *
     * @param event
     *     The {@link MessageReceivedEvent} which invoked the {@link CommandMethod}
     * @param commandMethod
     *     The {@link CommandMethod} which matches to the command alias that was invoked
     * @param content
     *     The rest of the {@link String} after the {@link String command alias} or null if there was nothing else
     *
     * @return If the {@link String content} matched this {@link CommandMethod} and it was invoked
     * @throws OutputException
     *     Any {@link OutputException} thrown by the {@link CommandMethod} when it was invoked
     */
    @Override
    protected Exceptional<Object> handleCommandMethodCall(@NotNull MessageReceivedEvent event,
                                                          @NotNull CommandMethod commandMethod,
                                                          @NotNull String content) throws OutputException {
        // Sanity check (This shouldn't be an issue)
        if (!(commandMethod instanceof SimpleCommand))
            return Exceptional.of(
                new TokenCommandException("The command method " + commandMethod.getDisplayCommand()
                    + " cannot be used with the command adapter " + this.getClass().getSimpleName()));
//
        SimpleCommand tokenCommand = (SimpleCommand) commandMethod;
        //If the command has been restricted and you're not whitelisted
        if (!tokenCommand.getRestrictedTo().isEmpty() &&
            !tokenCommand.getRestrictedTo().contains(event.getAuthor().getIdLong()) ||
            !event.getMember().hasPermission(commandMethod.getPermissions()))
                return Exceptional.of(new TokenCommandException("You do not have permission to use this command"));

        MethodContext ctx = MethodContext.of(content, this, event, tokenCommand.getReflections());
        return tokenCommand.parse(ctx, false);
    }
}
