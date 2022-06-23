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

package net.pumbas.halpbot.objects.expiring;

import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class ExpiringHashSet<T> extends HashSet<T> implements ExpiringCollection<T> {

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
                this.expirationDurationMs / 2,
                this.expirationDurationMs / 2,
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
