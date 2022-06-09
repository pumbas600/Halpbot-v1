package net.pumbas.halpbot.processors;

import org.dockbox.hartshorn.context.DefaultContext;
import org.dockbox.hartshorn.util.ArrayListMultiMap;
import org.dockbox.hartshorn.util.MultiMap;

import lombok.Getter;

public class MultiMapContext<K, V> extends DefaultContext {

    @Getter
    private final MultiMap<K, V> registeredContext = new ArrayListMultiMap<>();

    /**
     * Registers a context under the specified key so that it can be processed at a later point.
     *
     * @param key
     *     The key that maps to the list of contexts
     * @param context
     *     The context to add to the list of contexts
     */
    public void register(final K key, final V context) {
        this.registeredContext.put(key, context);
    }
}
