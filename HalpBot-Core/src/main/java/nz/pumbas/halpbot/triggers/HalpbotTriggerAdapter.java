package nz.pumbas.halpbot.triggers;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import org.dockbox.hartshorn.core.annotations.inject.Binds;
import org.dockbox.hartshorn.core.annotations.stereotype.Service;
import org.dockbox.hartshorn.core.context.ApplicationContext;
import org.dockbox.hartshorn.core.context.element.MethodContext;
import org.dockbox.hartshorn.core.domain.Exceptional;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

import javax.inject.Inject;

import lombok.Getter;
import nz.pumbas.halpbot.HalpbotCore;
import nz.pumbas.halpbot.actions.invokable.HalpbotSourceInvokable;
import nz.pumbas.halpbot.actions.invokable.InvocationContextFactory;
import nz.pumbas.halpbot.converters.tokens.ParsingToken;
import nz.pumbas.halpbot.converters.tokens.TokenService;
import nz.pumbas.halpbot.decorators.DecoratorService;
import nz.pumbas.halpbot.events.HalpbotEvent;
import nz.pumbas.halpbot.events.MessageEvent;
import nz.pumbas.halpbot.utilities.Require;

@Service
@Binds(TriggerAdapter.class)
public class HalpbotTriggerAdapter implements TriggerAdapter
{
    @Getter @Inject private ApplicationContext applicationContext;
    @Getter @Inject private HalpbotCore halpbotCore;

    @Inject private InvocationContextFactory invocationContextFactory;
    @Inject private TriggerContextFactory triggerContextFactory;
    @Inject private DecoratorService decoratorService;
    @Inject private TokenService tokenService;

    private final List<TriggerContext> triggerContexts = new ArrayList<>();

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.getAuthor().isBot()) return;

        String message = event.getMessage().getContentDisplay().toLowerCase(Locale.ROOT);
        HalpbotEvent halpbotEvent = new MessageEvent(event);


        for (TriggerContext triggerContext : this.triggerContexts) {
            if (triggerContext.matches(message)) {
                Exceptional<Object> result = triggerContext.invoke(this.invocationContextFactory.source(
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
    public <T> void registerTrigger(T instance, MethodContext<?, T> methodContext) {
        Trigger trigger = methodContext.annotation(Trigger.class).get();

        // TODO: Use factory to create SourceInvokable

        TriggerContext context = this.triggerContextFactory.create(
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
                Duration.of(trigger.display().value(), trigger.display().unit()),
                trigger.isEphemeral()
        );

        this.triggerContexts.add(context);
    }

    @Override
    public List<TriggerContext> triggerContexts() {
        return Collections.unmodifiableList(this.triggerContexts);
    }
}
