package nz.pumbas.halpbot.actions;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

//TODO: Remove
public abstract class AbstractActionCallback implements ActionCallback
{
    protected final long deleteAfterDuration;
    protected final TimeUnit deleteAfterTimeUnit;
    protected final long cooldownDuration;
    protected final TimeUnit cooldownTimeUnit;
    protected final List<String> permissions;
    protected final boolean singleUse;
    protected final long displayDuration;

    @Override
    public Set<String> permissions() {
        return Collections.emptySet();
    }

    protected AbstractActionCallback(
        long deleteAfterDuration, TimeUnit deleteAfterTimeUnit,
        long cooldownDuration, TimeUnit cooldownTimeUnit,
        List<String> permissions, boolean singleUse,
        long displayDuration)
    {
        this.deleteAfterDuration = deleteAfterDuration;
        this.deleteAfterTimeUnit = deleteAfterTimeUnit;
        this.cooldownDuration = cooldownDuration;
        this.cooldownTimeUnit = cooldownTimeUnit;
        this.permissions = permissions;
        this.singleUse = singleUse;
        this.displayDuration = displayDuration;
    }

    @Override
    public long getDeleteAfterDuration() {
        return this.deleteAfterDuration;
    }

    @Override
    public TimeUnit getDeleteAfterTimeUnit() {
        return this.deleteAfterTimeUnit;
    }

    @Override
    public boolean isSingleUse() {
        return this.singleUse;
    }

    @Override
    public long getDisplayDuration() {
        return this.displayDuration;
    }
}
