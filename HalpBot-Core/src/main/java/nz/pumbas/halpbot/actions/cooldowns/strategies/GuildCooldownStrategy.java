package nz.pumbas.halpbot.actions.cooldowns.strategies;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import nz.pumbas.halpbot.actions.cooldowns.CooldownStrategy;
import nz.pumbas.halpbot.actions.cooldowns.CooldownTimer;

public class GuildCooldownStrategy implements CooldownStrategy
{
    private final Map<Long, CooldownTimer> cooldownTimers = new ConcurrentHashMap<>();

    @Override
    public CooldownTimer get(long guildId, long userId) {
        return this.cooldownTimers.getOrDefault(guildId, CooldownTimer.Empty);
    }

    @Override
    public void put(long guildId, long userId, CooldownTimer cooldownTimer) {
        this.cooldownTimers.put(guildId, cooldownTimer);
    }

    @Override
    public String message() {
        return "Please wait, your guild is on cooldown";
    }
}
