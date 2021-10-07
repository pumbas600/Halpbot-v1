package nz.pumbas.halpbot.actions;

import nz.pumbas.halpbot.adapters.HalpbotCore;
import nz.pumbas.halpbot.events.HalpbotEvent;
import nz.pumbas.halpbot.configurations.DisplayConfiguration;
import nz.pumbas.halpbot.utilities.ErrorManager;

public interface ActionHandler
{
    default void handle(ActionCallback actionCallback, HalpbotEvent event) {
        long userId = event.getUser().getIdLong();

        if (!actionCallback.hasPermission(event.getUser())) {
            this.getHalpbotCore().getDisplayConfiguration()
                .displayTemporary(event, "You don't have permission to use this action", 30);
            return;
        }

        if (!this.getHalpbotCore().hasCooldown(event, userId, this.getActionId(event))) {

            actionCallback.invokeCallback(event)
                .present(value ->
                    {
                        DisplayConfiguration displayConfiguration = this.getHalpbotCore().getDisplayConfiguration();

                        if (this.displayTemporarily(actionCallback))
                                displayConfiguration.displayTemporary(event, value, actionCallback.getDisplayDuration());
                        else displayConfiguration.display(event, value);
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
    }

    default boolean displayTemporarily(ActionCallback actionCallback) {
        return actionCallback.displayTemporarily();
    }

    HalpbotCore getHalpbotCore();

    String getActionId(HalpbotEvent event);

    void removeActionCallbacks(HalpbotEvent halpbotEvent);
}
