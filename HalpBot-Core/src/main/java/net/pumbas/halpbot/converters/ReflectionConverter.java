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

package net.pumbas.halpbot.converters;

import net.pumbas.halpbot.commands.CommandAdapter;
import net.pumbas.halpbot.commands.actioninvokable.context.CommandInvocationContext;
import net.pumbas.halpbot.commands.actioninvokable.context.command.CommandContext;
import net.pumbas.halpbot.utilities.HalpbotUtils;

import org.dockbox.hartshorn.util.reflect.TypeContext;
import org.dockbox.hartshorn.util.Result;

import java.util.Collection;

public interface ReflectionConverter
{
    default Result<Object> parseReflection(CommandInvocationContext invocationContext) {
        if (!invocationContext.reflections().isEmpty()) {
            TypeContext<?> targetType = invocationContext.currentType();

            Result<String> methodName = invocationContext.next("(");
            if (methodName.present()) {
                CommandAdapter commandAdapter = invocationContext.applicationContext().get(CommandAdapter.class);
                Collection<CommandContext> commandContexts = commandAdapter.reflectiveCommandContext(
                    targetType, methodName.get(), invocationContext.reflections());
                if (!commandContexts.isEmpty()) {
                    int currentIndex = invocationContext.currentIndex();
                    invocationContext.canHaveContextLeft(true);
                    for (CommandContext commandContext : commandContexts) {
                        Result<Object> result = commandContext.invoke(invocationContext);
                        if (!result.caught() && invocationContext.isNext(')'))
                            return result;
                        else invocationContext.currentIndex(currentIndex);
                    }
                }
            }
        }
        return Result.of(HalpbotUtils.IGNORE_RESULT);
    }
}
