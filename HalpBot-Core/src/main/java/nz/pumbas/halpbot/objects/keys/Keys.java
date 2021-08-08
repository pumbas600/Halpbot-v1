package nz.pumbas.halpbot.objects.keys;

import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;

public final class Keys
{
    private Keys() {}

    /**
     * Creates a key which only supports the {@link Key#set} operation. Attempts to call the {@link Key#get}
     * operation will throw an {@link UnsupportedOperationException}.
     *
     * @param setter
     *     The {@link BiConsumer setter} for the key
     * @param <K>
     *     The type of the key holder
     * @param <V>
     *     The type of the passed value
     *
     * @return The created {@link Key}
     */
    public static <K, V> Key<K, V> setterKey(BiConsumer<K, V> setter) {
        return new Key<>(setter, k -> {
            throw new UnsupportedOperationException("This key doesn't support the get operation");
        });
    }

    /**
     * Creates a key which only supports the {@link Key#get} operation. Attempting to call the {@link Key#set}
     * operation will throw an {@link UnsupportedOperationException}.
     *
     * @param getter
     *     The {@link Function getter} for the key
     * @param <K>
     *     The type of the key holder
     * @param <V>
     *     The type of the returned value
     *
     * @return The created {@link Key}
     */
    public static <K, V> Key<K, V> getterKey(Function<K, V> getter) {
        return new Key<>((k, v) -> {
            throw new UnsupportedOperationException("This key doesn't support the set operation");
        }, k -> Optional.of(getter.apply(k)));
    }

    /**
     * Creates a key with standard {@link Key#set} and {@link Key#get} operations.
     *
     * @param setter
     *     The {@link BiConsumer setter} for the key
     * @param getter
     *     The {@link Function getter} for the key
     * @param <K>
     *     The type of the key holder
     * @param <V>
     *     The type of the value of the key
     *
     * @return The created {@link Key}
     */
    public static <K, V> Key<K, V> standardKey(BiConsumer<K, V> setter, Function<K, Optional<V>> getter) {
        return new Key<>(setter, getter);
    }

    public static <K, V> StorageKey<K, V> storageKey() {
        return new StorageKey<>();
    }
}
