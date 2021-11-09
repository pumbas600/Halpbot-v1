package nz.pumbas.halpbot.actions;

import org.dockbox.hartshorn.core.domain.Exceptional;

import java.util.concurrent.TimeUnit;

import nz.pumbas.halpbot.actions.cooldowns.CooldownAction;
import nz.pumbas.halpbot.events.HalpbotEvent;
import nz.pumbas.halpbot.permissions.Permissive;

public interface ActionCallback extends CooldownAction, Permissive
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
