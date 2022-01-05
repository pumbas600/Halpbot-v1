package nz.pumbas.halpbot.actions.cooldowns;

import java.util.function.Supplier;

import nz.pumbas.halpbot.actions.cooldowns.strategies.GuildCooldownStrategy;
import nz.pumbas.halpbot.actions.cooldowns.strategies.MemberCooldownStrategy;
import nz.pumbas.halpbot.actions.cooldowns.strategies.UserCooldownStrategy;

public enum CooldownType
{
    USER(UserCooldownStrategy::new),
    MEMBER(MemberCooldownStrategy::new),
    GUILD(GuildCooldownStrategy::new);

    private final Supplier<CooldownStrategy> cooldownStrategy;

    CooldownType(Supplier<CooldownStrategy> cooldownStrategy) {
        this.cooldownStrategy = cooldownStrategy;
    }

    public CooldownStrategy strategy() {
        return this.cooldownStrategy.get();
    }
}
