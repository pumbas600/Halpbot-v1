package nz.pumbas.halpbot.objects.keys;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class StorageKey<K, V> extends Key<K, V>
{
    private final Map<K, V> localStorage = new HashMap<>();

    protected StorageKey() {
        this.setter = this.localStorage::put;

        this.getter = k -> {
            if (this.localStorage.containsKey(k))
                return Optional.of(this.localStorage.get(k));
            else return Optional.empty();
        };
    }

    public Optional<V> remove(K keyholder) {
        return Optional.ofNullable(this.localStorage.remove(keyholder));
    }
}
