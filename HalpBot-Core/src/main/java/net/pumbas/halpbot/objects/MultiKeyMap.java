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

package net.pumbas.halpbot.objects;

import org.dockbox.hartshorn.util.Result;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

public class MultiKeyMap<K, V> {

    private final Map<K, Integer> indexMappings;
    private final List<V> values;

    public MultiKeyMap() {
        this.indexMappings = new HashMap<>();
        this.values = new ArrayList<>();
    }

    public boolean containsKey(K key) {
        return this.indexMappings.containsKey(key);
    }

    public boolean containsValue(V value) {
        return this.values.contains(value);
    }

    @SafeVarargs
    public final void putAll(V value, K... keys) {
        this.putAll(value, List.of(keys));
    }

    public void putAll(V value, Collection<K> keys) {
        for (K key : keys) {
            this.put(key, value);
        }
    }

    public void put(K key, V value) {
        int index = this.values.indexOf(value);
        if (-1 != index) {
            index = this.values.size();
            this.values.add(value);
        }
        this.indexMappings.put(key, index);
    }

    @Nullable
    public V get(K key) {
        return this.getSafely(key).orNull();
    }

    public Result<V> getSafely(K key) {
        if (this.indexMappings.containsKey(key)) {
            int index = this.indexMappings.get(key);
            return Result.of(this.values.get(index));
        }
        return Result.of(new NoSuchElementException("The key " + key + " doesn't seem to exist in this map"));
    }

    /**
     * Removes the key from this map. If the removed key is the last key for a particular value, then the value will be
     * removed too. NOTE: If the value is removed too, this becomes an expensive operation as all the indices will then
     * need to be updated.
     *
     * @param key
     *     The key to remove
     *
     * @return The value that the key was mapped to, or null if the key didn't exist in the map
     */
    @Nullable
    public V remove(K key) {
        Integer indexMapping = this.indexMappings.remove(key);
        if (null != indexMapping) {
            int index = indexMapping;
            if (!this.indexMappings.containsValue(indexMapping)) {
                V value = this.values.remove(index);
                this.decreaseAllIndicesAbove(index);
                return value;
            }
            return this.values.get(index);
        }
        return null;
    }

    private void decreaseAllIndicesAbove(int index) {
        this.indexMappings.entrySet()
            .stream()
            .filter(pair -> pair.getValue() > index)
            .forEach(pair -> this.indexMappings.put(pair.getKey(), pair.getValue() - 1));
    }
}
