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

package nz.pumbas.halpbot.utilities.context;

import org.dockbox.hartshorn.core.domain.Exceptional;

public interface ContextHolder
{
    /**
     * Retrieves the instance of the specified {@link Class implementation}. If there isn't already an implementation
     * for that class, then it tries to create one, assuming it has a constructor that takes no parameters. If the
     * specified {@link Class}, or a bound implementation if present is abstract or an interface, null is returned.
     *
     * @param implementation
     *      The {@link Class implementation} of the instance to retrieve
     * @param <T>
     *      The type of the instance to retrieve
     *
     * @return The instance, or null if there isn't one registered.
     */
    <T> T get(Class<T> implementation);

    /**
     * Retrieves an {@link Exceptional} of the instance of the specified {@link Class contract}.
     *
     * @param contract
     *      The {@link Class contract} of the instance to retrieve
     * @param <T>
     *      The type of the instance to retrieve
     *
     * @return An {@link Exceptional} of the instance, or {@link Exceptional#empty()} if there isn't one registered.
     */
    default <T> Exceptional<T> getSafely(Class<T> contract) {
        return Exceptional.of(this.get(contract));
    }
}
