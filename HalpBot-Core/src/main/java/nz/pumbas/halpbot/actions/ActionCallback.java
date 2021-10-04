package nz.pumbas.halpbot.actions;

import java.util.List;
import java.util.concurrent.TimeUnit;

import nz.pumbas.halpbot.commands.cooldowns.CooldownAction;
import nz.pumbas.halpbot.commands.events.HalpbotEvent;
import nz.pumbas.halpbot.objects.Exceptional;

public interface ActionCallback extends CooldownAction
{
    long getDeleteAfterDuration();

    TimeUnit getDeleteAfterTimeUnit();

    List<String> getPermissions();

    boolean isSingleUse();

    Exceptional<Object> invokeCallback(HalpbotEvent event);

    static ActionCallbackBuilder builder() {
        return new ActionCallbackBuilder();
    }
}
