package nz.pumbas.halpbot.actions;

import java.util.concurrent.TimeUnit;

import nz.pumbas.halpbot.commands.cooldowns.CooldownAction;
import nz.pumbas.halpbot.commands.events.HalpbotEvent;
import nz.pumbas.halpbot.objects.Exceptional;
import nz.pumbas.halpbot.permissions.PermissionAction;

public interface ActionCallback extends CooldownAction, PermissionAction
{
    long getDeleteAfterDuration();

    TimeUnit getDeleteAfterTimeUnit();

    boolean isSingleUse();

    long getDisplayDuration();

    Exceptional<Object> invokeCallback(HalpbotEvent event);

    static ActionCallbackBuilder builder() {
        return new ActionCallbackBuilder();
    }

    default boolean displayTemporarily() {
        return 0 < this.getDisplayDuration();
    }
}
