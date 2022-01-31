package nz.pumbas.halpbot.buttons;

import org.jetbrains.annotations.Nullable;

import java.time.Duration;

import nz.pumbas.halpbot.actions.DisplayableResult;
import nz.pumbas.halpbot.actions.invokable.SourceContext;

public interface ButtonContext extends SourceContext<ButtonInvocationContext>, DisplayableResult
{
    String id();

    Object[] passedParameters();

    int uses();

    void deductUse();

    Duration after();

    default boolean hasUses() {
        return this.uses() > 0;
    }

    default boolean isUsingDuration() {
        return !this.after().isNegative();
    }

    @Nullable
    AfterRemovalFunction afterRemoval();
}
