package nz.pumbas.halpbot.actions.cooldowns;

import java.util.HashMap;
import java.util.Map;

//TODO: Remove
public class UserCooldowns
{
    private CooldownTimer sendCooldownMessageCooldown;
    private final Map<String, CooldownTimer> cooldowns = new HashMap<>();

    public void addCooldown(String actionId, CooldownTimer cooldown) {
        this.cooldowns.put(actionId, cooldown);
    }

    public CooldownTimer getCooldownFor(String actionId) {
        return this.cooldowns.get(actionId);
    }

    public boolean hasCooldownFor(String actionId) {
        if (this.cooldowns.containsKey(actionId)) {
            CooldownTimer cooldown = this.cooldowns.get(actionId);
            if (cooldown.hasFinished()) {
                this.cooldowns.remove(actionId);
            }
            else return true;
        }
        return false;
    }

    public boolean canSendCooldownMessage() {
        if (null == this.sendCooldownMessageCooldown || this.sendCooldownMessageCooldown.hasFinished()) {
            //this.sendCooldownMessageCooldown = new CooldownTimer(10_000);
            return true;
        }
        return false;
    }

    public boolean isEmpty() {
        return this.cooldowns.isEmpty();
    }
}
