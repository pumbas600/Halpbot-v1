package nz.pumbas.halpbot.actions.cooldowns;

public interface CooldownStrategy
{
    CooldownTimer get(long guildId, long userId);

    void put(long guildId, long userId, CooldownTimer cooldownTimer);

    String message();
}
