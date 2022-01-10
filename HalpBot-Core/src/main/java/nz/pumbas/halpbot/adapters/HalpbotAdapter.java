package nz.pumbas.halpbot.adapters;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.hooks.EventListener;

import org.dockbox.hartshorn.core.Enableable;
import org.dockbox.hartshorn.core.context.ContextCarrier;
import org.dockbox.hartshorn.core.exceptions.ApplicationException;

import nz.pumbas.halpbot.actions.DisplayableResult;
import nz.pumbas.halpbot.common.CoreCarrier;
import nz.pumbas.halpbot.common.ExplainedException;
import nz.pumbas.halpbot.common.UndisplayedException;
import nz.pumbas.halpbot.configurations.DisplayConfiguration;
import nz.pumbas.halpbot.events.HalpbotEvent;

public interface HalpbotAdapter extends ContextCarrier, CoreCarrier, EventListener, Enableable
{
    default void onCreation(JDA jda) {}

    default void initialise(JDABuilder jdaBuilder) {}

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

    default void displayResult(HalpbotEvent halpbotEvent, DisplayableResult displayableResult, Object result) {
        DisplayConfiguration displayConfiguration = this.halpbotCore().displayConfiguration();
        if (displayableResult.isEphemeral())
            displayConfiguration.displayTemporary(halpbotEvent, result, 0);
        else displayConfiguration.display(halpbotEvent, result, displayableResult.displayDuration());
    }
}
