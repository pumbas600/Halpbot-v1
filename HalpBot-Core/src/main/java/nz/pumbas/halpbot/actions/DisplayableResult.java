package nz.pumbas.halpbot.actions;

import java.time.Duration;

public interface DisplayableResult
{
    Duration displayDuration();

    boolean isEphemeral();
}
