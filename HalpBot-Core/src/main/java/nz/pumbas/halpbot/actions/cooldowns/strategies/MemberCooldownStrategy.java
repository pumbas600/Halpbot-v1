package nz.pumbas.halpbot.actions.cooldowns.strategies;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import nz.pumbas.halpbot.actions.cooldowns.CooldownStrategy;
import nz.pumbas.halpbot.actions.cooldowns.CooldownTimer;

public class MemberCooldownStrategy implements CooldownStrategy
{
    private final Map<Long, Map<Long, CooldownTimer>> cooldownTimers = new ConcurrentHashMap<>();

    @Override
    public CooldownTimer get(long guildId, long userId) {
        return this.cooldownTimers.getOrDefault(guildId, Collections.emptyMap())
                .getOrDefault(userId, CooldownTimer.Empty);
    }

    @Override
    public void put(long guildId, long userId, CooldownTimer cooldownTimer) {
        this.cooldownTimers.computeIfAbsent(guildId, (id) -> new ConcurrentHashMap<>())
                .put(userId, cooldownTimer);
    }

    @Override
    public String message() {
        return "Please wait, you're on cooldown";
    }
}