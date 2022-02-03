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

import org.dockbox.hartshorn.core.Key;
import org.dockbox.hartshorn.core.annotations.activate.AutomaticActivation;
import org.dockbox.hartshorn.core.context.ApplicationContext;
import org.dockbox.hartshorn.core.services.ServicePreProcessor;

import nz.pumbas.halpbot.commands.annotations.Command;
import nz.pumbas.halpbot.commands.annotations.UseCommands;

@AutomaticActivation
public class CommandServicePreProcessor implements ServicePreProcessor<UseCommands>
{
    @Override
    public Integer order() {
        return 1;
    }



    @Override
    public Class<UseCommands> activator() {
        return UseCommands.class;
    }

    @Override
    public boolean preconditions(ApplicationContext context, Key<?> key) {
        return !key.type().methods(Command.class).isEmpty();
    }

    @Override
    public <T> void process(ApplicationContext context, Key<T> key) {
        context.log().debug("Processing %s".formatted(key.type().qualifiedName()));
        CommandAdapter commandAdapter = context.get(CommandAdapter.class);
        commandAdapter.registerCommands(key.type());
    }
}
