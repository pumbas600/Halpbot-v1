package nz.pumbas.halpbot.objects.expiringmap;

import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;

public class ConcurrentExpiringMap<K, V> extends ConcurrentHashMap<K, V> implements ExpiringMap<K, V>
{
    private final Map<K, Long> expirationKeys = new ConcurrentHashMap<>();
    private final long expirationDurationMs;
    private final BiConsumer<K, V> removalCallback;

    /**
     * Creates a concurrent expiring map where the elements expire after the specified expiration duration.
     * <p>
     * Note that if you try and specifiy an expiration duration unit that is smaller than milliseconds an
     * {@link IllegalArgumentException} will be thrown.
     *
     * @param expirationDuration
     *      The time that must pass before an entry is removed
     * @param expirationDurationUnit
     *      The time unit for the expiration duration. This cannot be smaller than milliseconds
     * @throws IllegalArgumentException
     *      If the expiration duration unit is smaller than milliseconds
     */
    public ConcurrentExpiringMap(long expirationDuration, TimeUnit expirationDurationUnit)
    {
        this(expirationDuration, expirationDurationUnit, (k, v) -> { });
    }

    /**
     * Creates a concurrent expiring map where the elements expire after the specified expiration duration. Before
     * the expired entries are removed from the map, the removal callback is invoked on them. Note that this could be
     * on another thread.
     * <p>
     * Note that if you try and specifiy an expiration duration unit that is smaller than milliseconds an
     * {@link IllegalArgumentException} will be thrown.
     *
     * @param expirationDuration
     *      The time that must pass before an entry is removed
     * @param expirationDurationUnit
     *      The time unit for the expiration duration. This cannot be smaller than milliseconds
     * @param removalCallback
     *      The callback that's invoked before an entry is removed
     *
     * @throws IllegalArgumentException
     *      If the expiration duration is less than or equal to 0 or if the expiration duration unit is smaller than
     *      milliseconds.
     */
    public ConcurrentExpiringMap(
        long expirationDuration,
        TimeUnit expirationDurationUnit,
        BiConsumer<K,V> removalCallback)
    {
        if (0 >= expirationDuration)
            throw new IllegalArgumentException("The expiration duration must be greater than 0");

        if (TimeUnit.MICROSECONDS == expirationDurationUnit || TimeUnit.NANOSECONDS == expirationDurationUnit)
            throw new IllegalArgumentException(
                "This expiring map is only capable of measuring expiration durations that are in milliseconds or " +
                    "another longer time unit");

        this.expirationDurationMs = TimeUnit.MILLISECONDS.convert(expirationDuration, expirationDurationUnit);
        this.removalCallback = removalCallback;
        this.initialiseCleaner();
    }

    // Its Scheduled twice every expiration period to reduce the chance of extrememe looping (An element just doesn't
    // expire on the loop, meaning it essentially stays in the map for twice the expiration duration)
    private void initialiseCleaner() {
        Executors.newSingleThreadScheduledExecutor()
            .scheduleAtFixedRate(
                this::clean,
                this.expirationDurationMs/2,
                this.expirationDurationMs/2,
                TimeUnit.MILLISECONDS);
    }

    private void clean() {
        for (Entry<K, Long> entry : this.expirationKeys.entrySet()) {
            if (this.hasExpired(entry.getValue())) {
                this.removalCallback.accept(entry.getKey(), this.get(entry.getKey()));
                this.remove(entry.getKey());
                this.expirationKeys.remove(entry.getKey());
            }
        }
    }

    private boolean hasExpired(long addedTimeMs) {
        return System.currentTimeMillis() - addedTimeMs > this.expirationDurationMs;
    }

    @Override
    public boolean renewKey(K key) {
        if (this.expirationKeys.containsKey(key)) {
            this.expirationKeys.put(key, System.currentTimeMillis());
            return true;
        }
        return false;
    }

    @Override
    public long getExpirationDuration() {
        return this.expirationDurationMs;
    }

    @Override
    public V put(@NotNull K key, @NotNull V value) {
        this.expirationKeys.put(key, System.currentTimeMillis());
        return super.put(key, value);
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> map) {
        for (Entry<? extends K, ? extends V> entry : map.entrySet()) {
            this.put(entry.getKey(), entry.getValue());
        }
    }

    @Override
    public V putIfAbsent(K key, V value) {
        if (!this.contains(key)) {
            return this.put(key, value);
        }
        return this.get(key);
    }
}
