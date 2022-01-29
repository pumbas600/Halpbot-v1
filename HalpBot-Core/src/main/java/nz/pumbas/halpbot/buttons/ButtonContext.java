package nz.pumbas.halpbot.buttons;

import java.time.Duration;

import nz.pumbas.halpbot.actions.DisplayableResult;
import nz.pumbas.halpbot.actions.invokable.SourceContext;

public interface ButtonContext extends SourceContext<ButtonInvocationContext>, DisplayableResult
{
    String id();

    Object[] passedParameters();

    int afterUsages();

    Duration after();

    default boolean isUsingUsages() {
        return this.afterUsages() > 0;
    }

    default boolean isUsingDuration() {
        return this.after().isNegative();
    }
}
