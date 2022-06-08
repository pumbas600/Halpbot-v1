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

package net.pumbas.halpbot.actions.invokable;

import net.pumbas.halpbot.buttons.ButtonContext;
import net.pumbas.halpbot.buttons.ButtonInvocationContext;
import net.pumbas.halpbot.commands.actioninvokable.context.CommandInvocationContext;
import net.pumbas.halpbot.converters.tokens.ParsingToken;
import net.pumbas.halpbot.events.HalpbotEvent;
import net.pumbas.halpbot.events.MessageEvent;
import net.pumbas.halpbot.mocks.MockMessageEvent;

import org.dockbox.hartshorn.component.Service;
import org.dockbox.hartshorn.component.factory.Factory;

import java.util.List;

@Service
public interface InvocationContextFactory
{
    @Factory
    CommandInvocationContext command(String content,
                                     HalpbotEvent halpbotEvent);

    default CommandInvocationContext command(String content) {
        return this.command(content, new MessageEvent(MockMessageEvent.INSTANCE));
    }

    @Factory
    ButtonInvocationContext button(HalpbotEvent halpbotEvent,
                                   List<ParsingToken> nonCommandParameterTokens,
                                   Object[] passedParameters);

    default ButtonInvocationContext button(HalpbotEvent halpbotEvent, ButtonContext buttonContext) {
        return this.button(halpbotEvent, buttonContext.nonCommandParameterTokens(), buttonContext.passedParameters());
    }

    @Factory
    SourceInvocationContext source(HalpbotEvent halpbotEvent,
                                   List<ParsingToken> nonCommandParameterTokens);
}
