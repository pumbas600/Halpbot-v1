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

package net.pumbas.halpbot.objects.expiring;

import java.util.Map;

/**
 * An expiring map is a thread-safe map that automatically removes entries after a specified period of time has passed.
 * <p>
 * In addition to the standard map methods, the expiring map also has a {@link ExpiringMap#renewKey(Object)} method that
 * can be used to reset the time that must pass before they're removed.
 * <p>
 * This is ideal for situations where you only want to store the data temporarily, such as cached user data from a
 * database. In this example, data that hasn't been used in a while can be removed so that you don't end up with your
 * entire database table cached in memory.
 *
 * @param <K>
 *     The type of the key elements
 * @param <V>
 *     The type of the value elements
 */
public interface ExpiringMap<K, V> extends Map<K, V>
{
    /**
     * Renews the specified key if it's contained within the map. This resets the expiration duration for this key.
     *
     * @param key
     *     The key to renew
     *
     * @return If the key was successfully renewed (This will be false if the key isn't contained within the map)
     */
    boolean renewKey(K key);

    /**
     * @return The expiration duration for this expiring map in milliseconds
     */
    long getExpirationDuration();
}
