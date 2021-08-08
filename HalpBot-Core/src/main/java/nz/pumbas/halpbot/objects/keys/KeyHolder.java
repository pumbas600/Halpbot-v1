package nz.pumbas.halpbot.objects.keys;

import java.util.Optional;

public interface KeyHolder<T extends KeyHolder<T>>
{
    /**
     * Sets the value of the {@link Key}.
     *
     * @param key
     *     The {@link Key} to set the value against
     * @param value
     *     The value to set
     * @param <V>
     *     The type of the value
     */
    @SuppressWarnings("unchecked")
    default <V> void set(Key<T, V> key, V value) {
        key.set((T) this, value);
    }

    /**
     * Gets an {@link Optional} containing the value of the key, if present.
     *
     * @param key
     *     The {@link Key} to retrieve the value for
     * @param <V>
     *     The type of the value to be returned
     *
     * @return An {@link Optional} containing the value of the key, if present
     */
    @SuppressWarnings("unchecked")
    default <V> Optional<V> get(Key<T, V> key) {
        return key.get((T) this);
    }

    /**
     * Gets the value from the {@link Key} without checking if its present. If the value is not present, null is
     * returned instead.
     *
     * @param key
     *     The {@link Key} to get the value from
     * @param <V>
     *     The type of the value to be returned
     *
     * @return The value of the key
     */
    @SuppressWarnings("unchecked")
    default <V> V getUnchecked(Key<T, V> key) {
        return key.getUnchecked((T) this);
    }

    /**
     * Removes a value from an {@link StorageKey}.
     *
     * @param key
     *     The {@link Key} to remove the value for
     * @param <V>
     *     The type of the value being removed
     *
     * @return An {@link Optional} containing the removed value, if present
     */
    @SuppressWarnings("unchecked")
    default <V> Optional<V> remove(StorageKey<T, V> key) {
        return key.remove((T) this);
    }
}
