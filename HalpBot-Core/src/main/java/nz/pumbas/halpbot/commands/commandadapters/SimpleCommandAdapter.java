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

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import org.dockbox.hartshorn.core.domain.Exceptional;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;

import nz.pumbas.halpbot.commands.context.CommandContext;
import nz.pumbas.halpbot.commands.annotations.Command;
import nz.pumbas.halpbot.events.HalpbotEvent;
import nz.pumbas.halpbot.commands.exceptions.OutputException;

public class SimpleCommandAdapter extends AbstractCommandAdapter
{
    public SimpleCommandAdapter(String commandPrefix) {
        super(commandPrefix);
    }

    /**
     * Creates a {@link CommandContext} from the specified information.
     *
     * @param instance
     *     The {@link Object} that the {@link Method} belongs to
     * @param method
     *     The {@link Method} for this {@link CommandContext}
     * @param command
     *     The {@link Command} which annotates the {@link Method}
     *
     * @return The created {@link CommandContext}
     */
    @Override
    protected CommandContext createCommandMethod(@NotNull Object instance,
                                                 @NotNull Method method,
                                                 @NotNull Command command) {
        return null;
    }

    /**
     * Handles the invocation, matching and parsing of a {@link CommandContext} with the given {@link String content}.
     *
     * @param event
     *     The {@link MessageReceivedEvent} which invoked the {@link CommandContext}
     * @param commandContext
     *     The {@link CommandContext} which matches to the command alias that was invoked
     * @param content
     *     The rest of the {@link String} after the {@link String command alias} or null if there was nothing else
     *
     * @return If the {@link String content} matched this {@link CommandContext} and it was invoked
     * @throws OutputException
     *     Any {@link OutputException} thrown by the {@link CommandContext} when it was invoked
     */
    @Override
    protected Exceptional<Object> handleCommandMethodCall(@NotNull HalpbotEvent event,
                                                          @NotNull CommandContext commandContext,
                                                          @NotNull String content) throws OutputException {
        //if (!commandContext.hasPermission(event.getUser()))
        //        return Exceptional.of(new CommandException("You do not have permission to use this command"));
//
//        MethodContext ctx = MethodContext.of(content, this.halpBotCore, event, commandContext.reflections());
//
//        return commandContext.parse(ctx, false);
        return Exceptional.empty();
    }
}
