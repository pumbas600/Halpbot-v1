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
