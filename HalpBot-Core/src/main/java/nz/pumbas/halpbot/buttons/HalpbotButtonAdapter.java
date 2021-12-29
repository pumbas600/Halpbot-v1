package nz.pumbas.halpbot.buttons;

import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.hooks.SubscribeEvent;
import net.dv8tion.jda.api.interactions.components.Button;

import org.dockbox.hartshorn.core.HartshornUtils;
import org.dockbox.hartshorn.core.annotations.inject.Binds;
import org.dockbox.hartshorn.core.annotations.stereotype.Service;
import org.dockbox.hartshorn.core.context.ApplicationContext;
import org.dockbox.hartshorn.core.context.element.MethodContext;
import org.dockbox.hartshorn.core.domain.Exceptional;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.Map;
import java.util.stream.Collectors;

import javax.inject.Inject;

import lombok.Getter;
import nz.pumbas.halpbot.HalpbotCore;
import nz.pumbas.halpbot.actions.invokable.ActionInvokable;
import nz.pumbas.halpbot.actions.invokable.InvocationContextFactory;
import nz.pumbas.halpbot.common.ExplainedException;
import nz.pumbas.halpbot.configurations.DisplayConfiguration;
import nz.pumbas.halpbot.converters.tokens.ParsingToken;
import nz.pumbas.halpbot.converters.tokens.TokenService;
import nz.pumbas.halpbot.decorators.DecoratorService;
import nz.pumbas.halpbot.events.HalpbotEvent;
import nz.pumbas.halpbot.events.InteractionEvent;

@Service
@Binds(ButtonAdapter.class)
public class HalpbotButtonAdapter implements ButtonAdapter
{
    private final Map<String, ButtonContext> registeredButtons = HartshornUtils.emptyMap();

    @Inject private TokenService tokenService;
    @Inject private DecoratorService decoratorService;
    @Inject private InvocationContextFactory invocationContextFactory;
    @Inject private ButtonContextFactory buttonContextFactory;

    @Inject @Getter private ApplicationContext applicationContext;
    @Inject @Getter private HalpbotCore halpbotCore;

    @Override
    public <T> void registerButton(T instance, MethodContext<?, T> buttonMethodContext) {
        ButtonAction button = buttonMethodContext.annotation(ButtonAction.class).get();
        String id = button.id().isBlank() ? buttonMethodContext.name() : button.id();
        ButtonContext buttonContext = this.createButton(
                id,
                button,
                new HalpbotButtonInvokable(instance, buttonMethodContext), //TODO: Use factory instead
                new Object[0]);

        this.registeredButtons.put(id, buttonContext);
    }

    @Override
    @Nullable
    public ButtonContext buttonContext(@Nullable String id) {
        return this.registeredButtons.get(id);
    }

    @Override
    public Button register(Button button, Object... parameters) {
        ButtonContext buttonContext = this.buttonContext( button.getId());
        if (buttonContext != null) {
            String newId = button.getId() + System.currentTimeMillis();
            ButtonContext newButtonContext = this.buttonContextFactory
                    .create(newId, parameters, buttonContext);

            this.registeredButtons.put(newId, newButtonContext);
            //TODO: Remove after awhile?
        }

        return button;
    }

    private <T> ButtonContext createButton(String id,
                                           ButtonAction buttonAction,
                                           ActionInvokable<ButtonInvocationContext> actionInvokable,
                                           Object[] passedParameters)
    {
        return this.buttonContextFactory.create(
                id,
                buttonAction.isEphemeral(),
                Duration.of(buttonAction.displayDuration().value(), buttonAction.displayDuration().unit()),
                this.decoratorService.decorate(actionInvokable),
                passedParameters,
                this.tokenService.tokens(actionInvokable.executable())
                        .stream()
                        .filter(token -> token instanceof ParsingToken parsingToken && !parsingToken.isCommandParameter())
                        .map(token -> (ParsingToken) token)
                        .collect(Collectors.toList())
        );
    }

    @Override
    @SubscribeEvent
    public void onButtonClick(ButtonClickEvent event) {
        if (!this.registeredButtons.containsKey(event.getComponentId()))
            return;

        ButtonContext buttonContext = this.registeredButtons.get(event.getComponentId());
        HalpbotEvent halpbotEvent = new InteractionEvent(event);
        ButtonInvocationContext invocationContext = this.invocationContextFactory.button(new InteractionEvent(event), buttonContext);

        Exceptional<Object> result = buttonContext.invoke(invocationContext);

        if (result.present()) {
            DisplayConfiguration displayConfiguration = this.halpbotCore.displayConfiguration();
            if (buttonContext.isEphemeral())
                displayConfiguration.displayTemporary(halpbotEvent, result.get(), 0);
            else displayConfiguration.display(halpbotEvent, result.get(), buttonContext.displayDuration());
        }
        else if (result.caught()) {
            Throwable exception = result.error();

            if (exception instanceof ExplainedException explainedException) {
                this.halpbotCore.displayConfiguration()
                        .displayTemporary(halpbotEvent, explainedException.explanation(), 30);
            }
            else this.halpbotCore.displayConfiguration()
                    .displayTemporary(
                            halpbotEvent,
                            "There was the following error trying to invoke this command: " + exception.getMessage(),
                            30);

        }
    }
}
