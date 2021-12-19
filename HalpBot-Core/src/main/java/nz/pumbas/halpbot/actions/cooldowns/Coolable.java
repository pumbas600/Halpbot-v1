package nz.pumbas.halpbot.actions.cooldowns;

import org.jetbrains.annotations.Nullable;

public interface Coolable
{
    @Nullable
    CooldownTimer cooldownTimer();

    Coolable cooldownTimer(CooldownTimer cooldownTimer);

    long cooldownDurationMs();

    default boolean hasCooldown() {
        return this.cooldownDurationMs() > 0;
    }

    default void resetTimer() {
        if (this.hasCooldown())
            this.cooldownTimer(new CooldownTimer(this.cooldownDurationMs()));
    }

    default boolean hasFinishedCoolingDown() {
        CooldownTimer cooldownTimer = this.cooldownTimer();
        return cooldownTimer != null && cooldownTimer.hasFinished();
    }
}
