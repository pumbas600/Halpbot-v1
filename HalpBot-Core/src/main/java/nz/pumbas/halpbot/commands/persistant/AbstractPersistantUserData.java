package nz.pumbas.halpbot.commands.persistant;

import nz.pumbas.halpbot.commands.commandadapters.AbstractCommandAdapter;

public class AbstractPersistantUserData implements PersistantUserData
{
    protected long userId;
    protected AbstractCommandAdapter commandAdapter;

    public AbstractPersistantUserData(long userId) {
        this.userId = userId;
    }

    /**
     * Sets the command adapter for which this {@link PersistantUserData} is registered with. Note that this is
     * automatically called when its created to set the appropriate command adapter.
     *
     * @param commandAdapter
     *      The {@link AbstractCommandAdapter} to set for this {@link PersistantUserData}
     */
    public void setCommandAdapter(AbstractCommandAdapter commandAdapter) {
        this.commandAdapter = commandAdapter;
    }

    /**
     * @return The id of the user for which this {@link PersistantUserData} belongs to
     */
    @Override
    public long getUserId() {
        return this.userId;
    }

    /**
     * Destroys this command data. This means if a command with this command is called again, a new
     * {@link PersistantUserData} will be created instead of this one being retrieved again.
     */
    @Override
    public void destroy() {
        this.commandAdapter.removePersistantUserData(this);
    }
}
