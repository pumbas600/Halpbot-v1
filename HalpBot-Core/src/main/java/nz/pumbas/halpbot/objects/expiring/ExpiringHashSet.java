package nz.pumbas.halpbot.objects.expiring;

import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class ExpiringHashSet<T> extends HashSet<T> implements ExpiringCollection<T>
{
    private final long expirationDurationMs;
    private final Map<T, Long> expiringValues = new ConcurrentHashMap<>();

    public ExpiringHashSet(long expirationDuration, TimeUnit expirationDurationUnit) {
        this.expirationDurationMs = TimeUnit.MILLISECONDS.convert(expirationDuration, expirationDurationUnit);
        this.initialiseCleaner();
    }

    // Its Scheduled twice every expiration period to reduce the chance of extrememe looping (An element just doesn't
    // expire on the first loop, meaning it essentially stays in the map for twice the expected expiration duration)
    private void initialiseCleaner() {
        Executors.newSingleThreadScheduledExecutor()
            .scheduleAtFixedRate(
                this::clean,
                this.expirationDurationMs/2,
                this.expirationDurationMs/2,
                TimeUnit.MILLISECONDS);
    }

    private void clean() {
        for (Entry<T, Long> entry : this.expiringValues.entrySet()) {
            if (this.hasExpired(entry.getValue())) {
                this.remove(entry.getKey());
                this.expiringValues.remove(entry.getKey());
            }
        }
    }

    private boolean hasExpired(long addedTimeMs) {
        return System.currentTimeMillis() - addedTimeMs > this.expirationDurationMs;
    }

    @Override
    public void renew(T value) {
        if (this.expiringValues.containsKey(value)) {
            this.expiringValues.put(value, System.currentTimeMillis());
        }
    }

    @Override
    public long getExpirationDuration() {
        return this.expirationDurationMs;
    }

    @Override
    public boolean add(T value) {
        this.expiringValues.put(value, System.currentTimeMillis());
        return super.add(value);
    }

    @Override
    public boolean remove(Object value) {
        this.expiringValues.remove(value);
        return super.remove(value);
    }
}
