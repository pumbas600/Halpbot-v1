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

package nz.pumbas.halpbot.converters;

import net.dv8tion.jda.api.interactions.commands.OptionType;

import org.dockbox.hartshorn.core.domain.Exceptional;

import java.util.function.Function;

import nz.pumbas.halpbot.commands.context.MethodContext;
import nz.pumbas.halpbot.utilities.enums.Priority;

public interface Converter<T>
{
    /**
     * @return The {@link Function mapper} for this {@link Converter}
     */
    Function<MethodContext, Exceptional<T>> getMapper();

    /**
     * @return The {@link Priority} associated with this {@link Converter}
     */
    Priority getPriority();

    /**
     * @return The {@link ConverterRegister} for this converter, describing how to register it to a {@link ConverterHandler}
     */
    ConverterRegister getRegister();

    OptionType getOptionType();

    /**
     * Registers itself to the {@link ConverterHandler} using the {@link ConverterRegister}.
     *
     * @param handler
     *      The {@link ConverterHandler} to register itself to.
     */
    default void register(ConverterHandler handler) {
        this.getRegister().register(handler, this);
    }
}
