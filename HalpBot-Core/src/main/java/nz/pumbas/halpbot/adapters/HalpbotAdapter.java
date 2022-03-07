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

package nz.pumbas.halpbot.adapters;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.hooks.EventListener;

import org.dockbox.hartshorn.core.Enableable;
import org.dockbox.hartshorn.core.context.ContextCarrier;
import org.dockbox.hartshorn.core.exceptions.ApplicationException;

import nz.pumbas.halpbot.actions.DisplayableResult;
import nz.pumbas.halpbot.common.CoreCarrier;
import nz.pumbas.halpbot.common.ExplainedException;
import nz.pumbas.halpbot.common.UndisplayedException;
import nz.pumbas.halpbot.configurations.DisplayConfiguration;
import nz.pumbas.halpbot.events.HalpbotEvent;

public interface HalpbotAdapter extends ContextCarrier, CoreCarrier, EventListener, Enableable
{
    default void initialise(JDA jda) {}

    @Override
    default void enable() throws ApplicationException {
        this.halpbotCore().registerAdapter(this);
    }

    default void handleException(HalpbotEvent halpbotEvent, Throwable exception) {
        if (exception instanceof ExplainedException explainedException) {
            this.halpbotCore().displayConfiguration()
                .displayTemporary(halpbotEvent, explainedException.explanation(), 30);
        } else if (!(exception instanceof UndisplayedException) && exception.getMessage() != null)
            this.halpbotCore().displayConfiguration()
                .displayTemporary(halpbotEvent,
                    "There was the following error trying to invoke this action: " + exception.getMessage(),
                    30);
    }

    default void displayResult(HalpbotEvent halpbotEvent, DisplayableResult displayableResult, Object result) {
        DisplayConfiguration displayConfiguration = this.halpbotCore().displayConfiguration();
        if (displayableResult.isEphemeral())
            displayConfiguration.displayTemporary(halpbotEvent, result, 0);
        else displayConfiguration.display(halpbotEvent, result, displayableResult.displayDuration());
    }
}
