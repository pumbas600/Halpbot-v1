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

package net.pumbas.halpbot.utilities;

import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class ConcurrentManager
{
    private final ScheduledExecutorService scheduler;

    public ConcurrentManager() {
        this.scheduler = Executors.newScheduledThreadPool(5);
    }

    public <T> Future<T> schedule(long delay, TimeUnit timeUnit, Callable<T> callable) {
        return this.scheduler.schedule(callable, delay, timeUnit);
    }

    public Future<?> schedule(long delay, TimeUnit timeUnit, Runnable runnable) {
        return this.scheduler.schedule(runnable, delay, timeUnit);
    }

    public ScheduledFuture<?> scheduleRegularly(long initialDelay, long interval, TimeUnit timeUnit,
                                                Runnable runnable) {
        return this.scheduler.scheduleAtFixedRate(runnable, initialDelay, interval, timeUnit);
    }

    public void shutdown() {
        this.scheduler.shutdown();
    }
}
