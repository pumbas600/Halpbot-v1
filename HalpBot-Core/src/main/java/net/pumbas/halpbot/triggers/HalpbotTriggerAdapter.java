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

package net.pumbas.halpbot.triggers;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.pumbas.halpbot.HalpbotCore;
import net.pumbas.halpbot.actions.invokable.HalpbotSourceInvokable;
import net.pumbas.halpbot.actions.invokable.InvocationContextFactory;
import net.pumbas.halpbot.converters.tokens.ParsingToken;
import net.pumbas.halpbot.converters.tokens.TokenService;
import net.pumbas.halpbot.decorators.DecoratorService;
import net.pumbas.halpbot.events.HalpbotEvent;
import net.pumbas.halpbot.events.MessageEvent;
import net.pumbas.halpbot.utilities.HalpbotUtils;
import net.pumbas.halpbot.utilities.Require;

import org.dockbox.hartshorn.application.context.ApplicationContext;
import org.dockbox.hartshorn.component.Component;
import org.dockbox.hartshorn.util.Result;
import org.dockbox.hartshorn.util.reflect.MethodContext;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

import jakarta.inject.Inject;
import lombok.Getter;

@Component
public class HalpbotTriggerAdapter implements TriggerAdapter {

    private final List<TriggerContext> triggerContexts = new ArrayList<>();
    @Getter
    @Inject
    private ApplicationContext applicationContext;
    @Getter
    @Inject
    private HalpbotCore halpbotCore;
    @Inject
    private InvocationContextFactory invocationContextFactory;
    @Inject
    private TriggerContextFactory triggerContextFactory;
    @Inject
    private DecoratorService decoratorService;
    @Inject
    private TokenService tokenService;

    @Override
    public void onMessageReceived(final MessageReceivedEvent event) {
        if (event.getAuthor().isBot()) return;

        final String message = event.getMessage().getContentDisplay().toLowerCase(Locale.ROOT);
        final HalpbotEvent halpbotEvent = new MessageEvent(event);


        for (final TriggerContext triggerContext : this.triggerContexts) {
            if (triggerContext.matches(message)) {
                final Result<Object> result = triggerContext.invoke(this.invocationContextFactory.source(
                    halpbotEvent,
                    triggerContext.nonCommandParameterTokens()));

                if (result.present())
                    this.displayResult(halpbotEvent, triggerContext, result.get());
                else if (result.caught()) {
                    this.handleException(halpbotEvent, result.error());
                }
            }
        }
    }

    @Override
    public <T> void registerTrigger(final T instance, final MethodContext<?, T> methodContext) {
        final Trigger trigger = methodContext.annotation(Trigger.class).get();

        // TODO: Use factory to create SourceInvokable

        final TriggerContext context = this.triggerContextFactory.create(
            Stream.of(trigger.value())
                .map(String::toLowerCase)
                .toList(),
            trigger.description(),
            trigger.require() == Require.ALL ? TriggerStrategy.ANYWHERE : trigger.strategy(),
            trigger.require(),
            this.tokenService.tokens(methodContext)
                .stream()
                .filter(token -> token instanceof ParsingToken parsingToken && !parsingToken.isCommandParameter())
                .map(token -> (ParsingToken) token)
                .toList(),
            this.decoratorService.decorate(new HalpbotSourceInvokable(instance, methodContext)),
            HalpbotUtils.asDuration(trigger.display()),
            trigger.isEphemeral()
        );

        this.triggerContexts.add(context);
    }

    @Override
    public List<TriggerContext> triggerContexts() {
        return Collections.unmodifiableList(this.triggerContexts);
    }
}
