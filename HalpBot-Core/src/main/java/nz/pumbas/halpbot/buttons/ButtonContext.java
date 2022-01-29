package nz.pumbas.halpbot.buttons;

import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.interactions.components.ActionRow;

import java.time.Duration;
import java.util.List;
import java.util.function.Function;

import nz.pumbas.halpbot.actions.DisplayableResult;
import nz.pumbas.halpbot.actions.invokable.SourceContext;

public interface ButtonContext extends SourceContext<ButtonInvocationContext>, DisplayableResult
{
    String id();

    Object[] passedParameters();

    int afterUsages();

    void deductUsage();

    Duration after();

    default boolean isUsingUsages() {
        return this.afterUsages() > 0;
    }

    default boolean isUsingDuration() {
        return this.after().isNegative();
    }

    Function<ButtonClickEvent, List<ActionRow>> afterRemoval();
}
