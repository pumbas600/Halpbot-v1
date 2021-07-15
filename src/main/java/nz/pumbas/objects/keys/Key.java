package nz.pumbas.objects.keys;

import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;


public class Key<K, V>
{
    protected BiConsumer<K, V> setter;
    protected Function<K, Optional<V>> getter;

    protected Key() {}

    protected Key(BiConsumer<K, V> setter, Function<K, Optional<V>> getter)
    {
        this.setter = setter;
        this.getter = getter;
    }

    /**
     * Sets the value of the {@link Key} for the passed key holder.
     *
     * @param keyholder
     *      The {@link Object key holder} to have the value set against
     * @param value
     *      The value to set for this {@link Key}
     */
    public void set(K keyholder, V value)
    {
        this.setter.accept(keyholder, value);
    }

    /**
     * Gets an {@link Optional} containing the value of the {@link Key} for the passed key holder.
     *
     * @param keyholder
     *      The {@link Object key holder} to retrieve the value from
     *
     * @return An {@link Optional} containing the value of the {@link Key}, if present
     */
    public Optional<V> get(K keyholder)
    {
        return this.getter.apply(keyholder);
    }

    /**
     * Gets the value of the {@link Key} for the passed key holder if present, otherwise it return null.
     *
     * @param keyholder
     *      The {@link Object key holder} to retrieve the value from
     *
     * @return The value of the {@link Key} if present, otherwise null
     */
    public V getUnchecked(K keyholder) {
        return this.getter.apply(keyholder).orElse(null);
    }
}
