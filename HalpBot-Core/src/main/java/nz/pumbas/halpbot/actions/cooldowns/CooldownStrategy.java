package nz.pumbas.halpbot.actions.cooldowns;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public interface CooldownStrategy
{
    CooldownTimer get(long guildId, long userId);

    void put(long guildId, long userId, CooldownTimer cooldownTimer);

    class GuildCooldownStrategy implements CooldownStrategy {

        private final Map<Long, CooldownTimer> cooldownTimers = new ConcurrentHashMap<>();

        @Override
        public CooldownTimer get(long guildId, long userId) {
            return this.cooldownTimers.getOrDefault(guildId, CooldownTimer.Empty);
        }

        @Override
        public void put(long guildId, long userId, CooldownTimer cooldownTimer) {
            this.cooldownTimers.put(guildId, cooldownTimer);
        }
    }

    class MemberCooldownStrategy implements CooldownStrategy {

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
    }

    class UserCooldownStrategy implements CooldownStrategy {

        private final Map<Long, CooldownTimer> cooldownTimers = new ConcurrentHashMap<>();

        @Override
        public CooldownTimer get(long guildId, long userId) {
            return this.cooldownTimers.getOrDefault(userId, CooldownTimer.Empty);
        }

        @Override
        public void put(long guildId, long userId, CooldownTimer cooldownTimer) {
            this.cooldownTimers.put(userId, cooldownTimer);
        }
    }
}
