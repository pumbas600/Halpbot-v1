package nz.pumbas.halpbot.actions;

import nz.pumbas.halpbot.adapters.HalpbotCore;
import nz.pumbas.halpbot.commands.events.HalpbotEvent;
import nz.pumbas.halpbot.utilities.ErrorManager;

public interface ActionHandler
{
    default void handle(ActionCallback actionCallback, HalpbotEvent event) {
        long userId = event.getUser().getIdLong();

        if (!actionCallback.hasPermission(event.getUser())) {
            this.getHalpbotCore().getDisplayConfiguration()
                .displayTemporary(event, "You don't have permission to use this acrion");
            return;
        }

        if (this.getHalpbotCore().hasCooldown(event, userId, this.getActionId(event))) {
            return;
        }

        actionCallback.invokeCallback(event)
            .present(value ->
                {
                    if (actionCallback.getDisplayResultTemporarily())
                        this.getHalpbotCore().getDisplayConfiguration().displayTemporary(event, value);
                    else this.getHalpbotCore().getDisplayConfiguration().display(event, value);
                })
            .caught(ErrorManager::handle);

        if (actionCallback.isSingleUse()) {
            this.removeActionCallbacks(event);
            return;
        }

        if (actionCallback.hasCooldown()) {
            this.getHalpbotCore()
                .addCooldown(userId, this.getActionId(event), actionCallback.createCooldown());
        }
    }

    HalpbotCore getHalpbotCore();

    String getActionId(HalpbotEvent event);

    void removeActionCallbacks(HalpbotEvent halpbotEvent);
}
