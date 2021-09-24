package nz.pumbas.halpbot.commands.cooldowns;

import java.util.concurrent.TimeUnit;

public class Cooldown
{
    private final long startTimeMs;
    private final long durationMs;

    public Cooldown(long duration, TimeUnit timeUnit) {
        this.startTimeMs = System.currentTimeMillis();
        this.durationMs = TimeUnit.MILLISECONDS.convert(duration, timeUnit);
    }

    public boolean hasFinished() {
        return System.currentTimeMillis() - this.startTimeMs > this.durationMs;
    }

    public long getRemainingTime() {
        return this.durationMs - (System.currentTimeMillis() - this.startTimeMs);
    }

    public String getRemainingTimeMessage() {
        return this.getRemainingTime() + "ms Remaining";
    }
}
