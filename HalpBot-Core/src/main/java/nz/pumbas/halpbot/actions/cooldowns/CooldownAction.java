package nz.pumbas.halpbot.actions.cooldowns;

import java.util.concurrent.TimeUnit;

public interface CooldownAction
{
    long getCooldownDuration();

    TimeUnit getCooldownTimeUnit();

    default boolean hasCooldown() {
        return 0 < this.getCooldownDuration();
    }

    default Cooldown createCooldown() {
        return new Cooldown(this.getCooldownDuration(), this.getCooldownTimeUnit());
    }
}
