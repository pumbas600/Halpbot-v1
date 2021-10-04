package nz.pumbas.halpbot.actions;

import java.util.List;
import java.util.concurrent.TimeUnit;

public abstract class AbstractActionCallback implements ActionCallback
{

    protected final long deleteAfterDuration;
    protected final TimeUnit deleteAfterTimeUnit;
    protected final long cooldownDuration;
    protected final TimeUnit cooldownTimeUnit;
    protected final List<String> permissions;
    protected final boolean singleUse;

    protected AbstractActionCallback(
        long deleteAfterDuration, TimeUnit deleteAfterTimeUnit,
        long cooldownDuration, TimeUnit cooldownTimeUnit,
        List<String> permissions,
        boolean singleUse)
    {
        this.deleteAfterDuration = deleteAfterDuration;
        this.deleteAfterTimeUnit = deleteAfterTimeUnit;
        this.cooldownDuration = cooldownDuration;
        this.cooldownTimeUnit = cooldownTimeUnit;
        this.permissions = permissions;
        this.singleUse = singleUse;
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
    public long getCooldownDuration() {
        return this.cooldownDuration;
    }

    @Override
    public TimeUnit getCooldownTimeUnit() {
        return this.cooldownTimeUnit;
    }

    @Override
    public List<String> getPermissions() {
        return this.permissions;
    }

    @Override
    public boolean isSingleUse() {
        return this.singleUse;
    }
}
