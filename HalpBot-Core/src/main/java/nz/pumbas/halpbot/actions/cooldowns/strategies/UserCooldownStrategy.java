package nz.pumbas.halpbot.actions.cooldowns.strategies;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import nz.pumbas.halpbot.actions.cooldowns.CooldownStrategy;
import nz.pumbas.halpbot.actions.cooldowns.CooldownTimer;

public class UserCooldownStrategy implements CooldownStrategy
{
    private final Map<Long, CooldownTimer> cooldownTimers = new ConcurrentHashMap<>();

    @Override
    public CooldownTimer get(long guildId, long userId) {
        return this.cooldownTimers.getOrDefault(userId, CooldownTimer.Empty);
    }

    @Override
    public void put(long guildId, long userId, CooldownTimer cooldownTimer) {
        this.cooldownTimers.put(userId, cooldownTimer);
    }

    @Override
    public String message() {
        return "Please wait, you're on cooldown";
    }
}
