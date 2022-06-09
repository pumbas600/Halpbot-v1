package net.pumbas.halpbot.processors;

import org.dockbox.hartshorn.context.DefaultContext;
import org.dockbox.hartshorn.util.ArrayListMultiMap;
import org.dockbox.hartshorn.util.MultiMap;

import lombok.Getter;

public class MultiMapContext<K, V> extends DefaultContext {

    @Getter
    private final MultiMap<K, V> registeredContext = new ArrayListMultiMap<>();

    /**
     * Registers a value under the specified key.
     *
     * @param key
     *     The key that maps to the list of values
     * @param value
     *     The value to add to the list of values
     */
    public void register(final K key, final V value) {
        this.registeredContext.put(key, value);
    }
}
