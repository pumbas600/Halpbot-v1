package nz.pumbas.halpbot.actions.cooldowns;

import java.util.function.Supplier;

import nz.pumbas.halpbot.actions.cooldowns.strategies.GuildCooldownStrategy;
import nz.pumbas.halpbot.actions.cooldowns.strategies.MemberCooldownStrategy;
import nz.pumbas.halpbot.actions.cooldowns.strategies.UserCooldownStrategy;

public enum CooldownType
{
    /**
     * Make the action cooldown everytime a user invokes it, irrespective of which guild it's invoked within.
     */
    USER(UserCooldownStrategy::new),

    /**
     * Make the action cooldown everytime a user invokes it within the same guild.
     */
    MEMBER(MemberCooldownStrategy::new),

    /**
     * Make the action cooldown everytime it's used within a guild, irrespective of who invokes it.
     */
    GUILD(GuildCooldownStrategy::new);

    private final Supplier<CooldownStrategy> cooldownStrategy;

    CooldownType(Supplier<CooldownStrategy> cooldownStrategy) {
        this.cooldownStrategy = cooldownStrategy;
    }

    public CooldownStrategy strategy() {
        return this.cooldownStrategy.get();
    }
}
