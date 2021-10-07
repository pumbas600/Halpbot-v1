package objects;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import nz.pumbas.halpbot.objects.expiringmap.ConcurrentExpiringMap;
import nz.pumbas.halpbot.objects.expiringmap.ExpiringMap;

public class ConcurrentExpiringMapTests
{
    private static final long EXPIRATION_DURATION = 2000;

    private int removalCounter;
    private ExpiringMap<Integer, String> expiringMap;

    @BeforeEach
    public void initialiseMap() {
        this.expiringMap = new ConcurrentExpiringMap<>(EXPIRATION_DURATION, TimeUnit.MILLISECONDS,
            (k, v) -> {
                this.removalCounter++;
                System.out.printf("Removed [%d: %s]\n", k, v);
            });
        this.removalCounter = 0;
    }

    @Test
    public void invalidConstructorTests() {
        Assertions.assertThrows(IllegalArgumentException.class,
            () -> new ConcurrentExpiringMap<>(EXPIRATION_DURATION, TimeUnit.MICROSECONDS));

        Assertions.assertThrows(IllegalArgumentException.class,
            () -> new ConcurrentExpiringMap<>(EXPIRATION_DURATION, TimeUnit.NANOSECONDS));

        Assertions.assertThrows(IllegalArgumentException.class,
            () -> new ConcurrentExpiringMap<>(-1, TimeUnit.MILLISECONDS));
    }

    @Test
    public void putTest() throws InterruptedException {
        this.expiringMap.put(1, "One");
        this.expiringMap.put(2, "Two");

        Assertions.assertEquals("One", this.expiringMap.get(1));
        Assertions.assertEquals("Two", this.expiringMap.get(2));
        Assertions.assertNull(this.expiringMap.get(3));
        Assertions.assertEquals(0, this.removalCounter);

        // Some time has passed, but not enough for the keys to be automatically removed
        Thread.sleep(EXPIRATION_DURATION / 2);

        Assertions.assertEquals("One", this.expiringMap.get(1));
        Assertions.assertEquals("Two", this.expiringMap.get(2));
        Assertions.assertNull(this.expiringMap.get(3));
        Assertions.assertEquals(0, this.removalCounter);

        // The keys should've been automatically removed
        Thread.sleep(EXPIRATION_DURATION);

        Assertions.assertNull(this.expiringMap.get(1));
        Assertions.assertNull(this.expiringMap.get(2));
        Assertions.assertNull(this.expiringMap.get(3));
        Assertions.assertEquals(2, this.removalCounter);
    }

    @Test
    public void putAllTest() throws InterruptedException {
        Map<Integer, String> otherKeysMap = new HashMap<>();
        otherKeysMap.put(1, "One");
        otherKeysMap.put(2, "Two");

        this.expiringMap.putAll(otherKeysMap);

        Assertions.assertEquals("One", this.expiringMap.get(1));
        Assertions.assertEquals("Two", this.expiringMap.get(2));
        Assertions.assertNull(this.expiringMap.get(3));
        Assertions.assertEquals(0, this.removalCounter);

        // Some time has passed, but not enough for the keys to be automatically removed
        Thread.sleep(EXPIRATION_DURATION / 2);

        Assertions.assertEquals("One", this.expiringMap.get(1));
        Assertions.assertEquals("Two", this.expiringMap.get(2));
        Assertions.assertNull(this.expiringMap.get(3));
        Assertions.assertEquals(0, this.removalCounter);

        // The keys should've been automatically removed
        Thread.sleep(EXPIRATION_DURATION);

        Assertions.assertNull(this.expiringMap.get(1));
        Assertions.assertNull(this.expiringMap.get(2));
        Assertions.assertNull(this.expiringMap.get(3));
        Assertions.assertEquals(2, this.removalCounter);
    }

    @Test
    public void removeTest() throws InterruptedException {
        this.expiringMap.put(1, "One");
        this.expiringMap.put(2, "Two");

        Assertions.assertTrue(this.expiringMap.containsKey(1));
        Assertions.assertTrue(this.expiringMap.containsKey(2));
        Assertions.assertFalse(this.expiringMap.containsKey(3));
        Assertions.assertEquals(0, this.removalCounter);

        this.expiringMap.remove(1);

        Assertions.assertFalse(this.expiringMap.containsKey(1));
        Assertions.assertTrue(this.expiringMap.containsKey(2));
        Assertions.assertFalse(this.expiringMap.containsKey(3));
        Assertions.assertEquals(0, this.removalCounter);

        // All keys should have been removed
        Thread.sleep(2 * EXPIRATION_DURATION);

        Assertions.assertNull(this.expiringMap.get(1));
        Assertions.assertNull(this.expiringMap.get(2));
        Assertions.assertNull(this.expiringMap.get(3));
        Assertions.assertEquals(1, this.removalCounter);
    }

    @Test
    public void removeKeyValueTest() throws InterruptedException {
        this.expiringMap.put(1, "One");
        this.expiringMap.put(2, "Two");

        Assertions.assertTrue(this.expiringMap.containsKey(1));
        Assertions.assertTrue(this.expiringMap.containsKey(2));
        Assertions.assertFalse(this.expiringMap.containsKey(3));
        Assertions.assertEquals(0, this.removalCounter);

        this.expiringMap.remove(1, "One");
        this.expiringMap.remove(2, "2");

        Assertions.assertFalse(this.expiringMap.containsKey(1));
        Assertions.assertTrue(this.expiringMap.containsKey(2));
        Assertions.assertFalse(this.expiringMap.containsKey(3));
        Assertions.assertEquals(0, this.removalCounter);

        // All keys should have been removed
        Thread.sleep(2 * EXPIRATION_DURATION);

        Assertions.assertNull(this.expiringMap.get(1));
        Assertions.assertNull(this.expiringMap.get(2));
        Assertions.assertNull(this.expiringMap.get(3));
        Assertions.assertEquals(1, this.removalCounter);
    }

    @Test
    public void renewKeyTest() throws InterruptedException {
        this.expiringMap.put(1, "One");
        this.expiringMap.put(2, "Two");

        Assertions.assertEquals("One", this.expiringMap.get(1));
        Assertions.assertEquals("Two", this.expiringMap.get(2));
        Assertions.assertNull(this.expiringMap.get(3));

        // Some time has passed, but not enough for the keys to be automatically removed
        Thread.sleep(EXPIRATION_DURATION / 2);

        this.expiringMap.renewKey(1);

        Assertions.assertEquals("One", this.expiringMap.get(1));
        Assertions.assertEquals("Two", this.expiringMap.get(2));
        Assertions.assertNull(this.expiringMap.get(3));
        Assertions.assertEquals(0, this.removalCounter);

        // Key 2 should've been automatically removed
        Thread.sleep((long)(EXPIRATION_DURATION * 0.8));

        Assertions.assertEquals("One", this.expiringMap.get(1));
        Assertions.assertNull(this.expiringMap.get(2));
        Assertions.assertNull(this.expiringMap.get(3));
        Assertions.assertEquals(1, this.removalCounter);

        // Key 1 should've been automatically removed
        Thread.sleep(EXPIRATION_DURATION);

        Assertions.assertNull(this.expiringMap.get(1));
        Assertions.assertNull(this.expiringMap.get(2));
        Assertions.assertNull(this.expiringMap.get(3));
        Assertions.assertEquals(2, this.removalCounter);
    }

    @Test
    public void removalCallbackTest() throws InterruptedException {
        this.expiringMap.put(1, "One");
        this.expiringMap.put(2, "Two");

        Assertions.assertEquals("One", this.expiringMap.get(1));
        Assertions.assertEquals("Two", this.expiringMap.get(2));
        Assertions.assertNull(this.expiringMap.get(3));
        Assertions.assertEquals(0, this.removalCounter);

        Thread.sleep((long)(1.5 * EXPIRATION_DURATION));

        Assertions.assertNull(this.expiringMap.get(1));
        Assertions.assertNull(this.expiringMap.get(2));
        Assertions.assertNull(this.expiringMap.get(3));
        Assertions.assertEquals(2, this.removalCounter);
    }
}
