package nz.pumbas.halpbot.utilities;

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
        return this.scheduler.scheduleAtFixedRate(runnable, initialDelay, initialDelay, timeUnit);
    }

    public void shutdown() {
        this.scheduler.shutdown();
    }
}
