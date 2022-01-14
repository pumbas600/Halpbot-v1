package nz.pumbas.halpbot.triggers;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import org.dockbox.hartshorn.core.annotations.inject.Binds;
import org.dockbox.hartshorn.core.annotations.stereotype.Service;
import org.dockbox.hartshorn.core.context.ApplicationContext;
import org.dockbox.hartshorn.core.context.element.MethodContext;

import java.time.Duration;
import java.util.ArrayList;
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
import nz.pumbas.halpbot.events.MessageEvent;

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
        String message = event.getMessage().getContentDisplay().toLowerCase(Locale.ROOT);

        for (TriggerContext triggerContext : this.triggerContexts) {
            if (triggerContext.matches(message))
                triggerContext.invoke(this.invocationContextFactory.source(
                        new MessageEvent(event),
                        triggerContext.nonCommandParameterTokens()));
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
                trigger.strategy(),
                trigger.merger(),
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
}
