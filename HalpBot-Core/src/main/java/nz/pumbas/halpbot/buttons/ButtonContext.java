package nz.pumbas.halpbot.buttons;

import org.jetbrains.annotations.Nullable;

import java.time.Duration;

import nz.pumbas.halpbot.actions.DisplayableResult;
import nz.pumbas.halpbot.actions.invokable.SourceContext;

public interface ButtonContext extends SourceContext<ButtonInvocationContext>, DisplayableResult
{
    String id();

    Object[] passedParameters();

    int remainingUses();

    void deductUse();

    Duration removeAfter();

    default boolean hasUses() {
        return this.remainingUses() > 0;
    }

    default boolean isUsingDuration() {
        return !this.removeAfter().isNegative();
    }

    @Nullable
    AfterRemovalFunction afterRemoval();
}
