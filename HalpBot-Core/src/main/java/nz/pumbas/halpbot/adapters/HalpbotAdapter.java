package nz.pumbas.halpbot.adapters;

import net.dv8tion.jda.api.JDA;

import org.dockbox.hartshorn.core.Enableable;
import org.dockbox.hartshorn.core.context.ContextCarrier;
import org.dockbox.hartshorn.core.exceptions.ApplicationException;

import nz.pumbas.halpbot.HalpbotCore;
import nz.pumbas.halpbot.common.CoreCarrier;
import nz.pumbas.halpbot.common.ExplainedException;
import nz.pumbas.halpbot.common.UndisplayedException;
import nz.pumbas.halpbot.events.HalpbotEvent;

public interface HalpbotAdapter extends ContextCarrier, CoreCarrier, Enableable
{
    default void onCreation(JDA jda) {}

    @Override
    default void enable() throws ApplicationException {
        this.halpbotCore().registerAdapter(this);
    }

    default void handleException(HalpbotEvent halpbotEvent, Throwable exception) {
        if (exception instanceof ExplainedException explainedException) {
            this.halpbotCore().displayConfiguration()
                    .displayTemporary(halpbotEvent, explainedException.explanation(), 30);
        }
        else if (!(exception instanceof UndisplayedException))
            this.halpbotCore().displayConfiguration()
                    .displayTemporary(halpbotEvent,
                            "There was the following error trying to invoke this action: " + exception.getMessage(),
                            30);
    }
}
