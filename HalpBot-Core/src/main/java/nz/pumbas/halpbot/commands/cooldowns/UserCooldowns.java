package nz.pumbas.halpbot.commands.cooldowns;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class UserCooldowns
{
    private Cooldown sendCooldownMessageCooldown;
    private final Map<String, Cooldown> cooldowns = new HashMap<>();

    public void addCooldown(String actionId, Cooldown cooldown) {
        this.cooldowns.put(actionId, cooldown);
    }

    public Cooldown getCooldownFor(String actionId) {
        return this.cooldowns.get(actionId);
    }

    public boolean hasCooldownFor(String actionId) {
        if (this.cooldowns.containsKey(actionId)) {
            Cooldown cooldown = this.cooldowns.get(actionId);
            if (cooldown.hasFinished()) {
                this.cooldowns.remove(actionId);
            }
            else return true;
        }
        return false;
    }

    public boolean canSendCooldownMessage() {
        if (null == this.sendCooldownMessageCooldown || this.sendCooldownMessageCooldown.hasFinished()) {
            this.sendCooldownMessageCooldown = new Cooldown(10, TimeUnit.SECONDS);
            return true;
        }
        return false;
    }

    public boolean isEmpty() {
        return this.cooldowns.isEmpty();
    }
}
