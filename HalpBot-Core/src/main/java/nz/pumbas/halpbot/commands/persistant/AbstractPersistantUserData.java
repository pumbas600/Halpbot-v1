/*
 * MIT License
 *
 * Copyright (c) 2021 pumbas600
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package nz.pumbas.halpbot.commands.persistant;

import nz.pumbas.halpbot.commands.commandadapters.AbstractCommandAdapter;

public class AbstractPersistantUserData implements PersistantUserData
{
    protected long userId;
    protected AbstractCommandAdapter commandAdapter;
    protected boolean alreadyExisted;

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
     * @return If this persistent data already existed when it was retrieved or if it was just created
     */
    @Override
    public boolean alreadyExisted() {
        return this.alreadyExisted;
    }

    /**
     * Sets that this persistent data already existed when it was retrieved and wasn't just created.
     */
    @Override
    public void setAlreadyExisted() {
        this.alreadyExisted = true;
    }

    /**
     * Destroys this command data. This means if a command with this parameter is called again, a new
     * {@link PersistantUserData} will be created instead of this one being retrieved again.
     */
    @Override
    public void destroy() {
        this.commandAdapter.removePersistantUserData(this);
    }
}
