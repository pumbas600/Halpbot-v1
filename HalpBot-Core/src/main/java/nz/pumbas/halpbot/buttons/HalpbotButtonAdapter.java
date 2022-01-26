package nz.pumbas.halpbot.buttons;

import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.interactions.components.Button;

import org.dockbox.hartshorn.core.HartshornUtils;
import org.dockbox.hartshorn.core.annotations.inject.ComponentBinding;
import org.dockbox.hartshorn.core.annotations.stereotype.Service;
import org.dockbox.hartshorn.core.context.ApplicationContext;
import org.dockbox.hartshorn.core.context.element.MethodContext;
import org.dockbox.hartshorn.core.domain.Exceptional;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;

import lombok.Getter;
import nz.pumbas.halpbot.HalpbotCore;
import nz.pumbas.halpbot.actions.invokable.ActionInvokable;
import nz.pumbas.halpbot.actions.invokable.InvocationContextFactory;
import nz.pumbas.halpbot.configurations.DisplayConfiguration;
import nz.pumbas.halpbot.converters.tokens.ParsingToken;
import nz.pumbas.halpbot.converters.tokens.TokenService;
import nz.pumbas.halpbot.decorators.DecoratorService;
import nz.pumbas.halpbot.events.HalpbotEvent;
import nz.pumbas.halpbot.events.InteractionEvent;

@Singleton
@ComponentBinding(ButtonAdapter.class)
public class HalpbotButtonAdapter implements ButtonAdapter
{
    private int idSuffix;
    private final Map<String, ButtonContext> registeredButtons = HartshornUtils.emptyMap();

    @Inject private TokenService tokenService;
    @Inject private DecoratorService decoratorService;
    @Inject private InvocationContextFactory invocationContextFactory;
    @Inject private ButtonContextFactory buttonContextFactory;

    @Inject @Getter private ApplicationContext applicationContext;
    @Inject @Getter private HalpbotCore halpbotCore;

    @Override
    public <T> void registerButton(T instance, MethodContext<?, T> methodContext) {
        ButtonAction button = methodContext.annotation(ButtonAction.class).get();
        String id = this.id(button, methodContext);
        ButtonContext buttonContext = this.createButton(
                id,
                button,
                new HalpbotButtonInvokable(instance, methodContext), //TODO: Use factory instead
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
        String id = button.getId();
        if (id == null) {
            this.applicationContext.log().warn("You cannot register a button that has no id with the button adapter");
            return button;
        }

        ButtonContext buttonContext = this.buttonContext( button.getId());
        if (buttonContext == null)
            throw new IllegalArgumentException(
                    "You cannot register a button with the id %s as there is no matching button action for it"
                            .formatted(button.getId()));

        String newId = this.generateId(id);
        ButtonContext newButtonContext = this.buttonContextFactory
                .create(newId, parameters, buttonContext);

        this.registeredButtons.put(newId, newButtonContext);
        //TODO: Remove after awhile

        return button.withId(newId);
    }

    public String generateId(String currentId) {
        // The id suffix prevents buttons registered within the same millisecond having the same id
        this.idSuffix = (this.idSuffix + 1) % Integer.MAX_VALUE;
        return "%s-%d-%d".formatted(currentId, System.currentTimeMillis(), this.idSuffix);
    }

    @Override
    public void unregister(String id) {
        this.registeredButtons.remove(id);
    }

    private <T> ButtonContext createButton(String id,
                                           ButtonAction buttonAction,
                                           ActionInvokable<ButtonInvocationContext> actionInvokable,
                                           Object[] passedParameters)
    {
        return this.buttonContextFactory.create(
                id,
                buttonAction.isEphemeral(),
                Duration.of(buttonAction.display().value(), buttonAction.display().unit()),
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
    public void onButtonClick(ButtonClickEvent event) {
        if (!this.registeredButtons.containsKey(event.getComponentId())) {
            return;
        }

        ButtonContext buttonContext = this.registeredButtons.get(event.getComponentId());
        HalpbotEvent halpbotEvent = new InteractionEvent(event);
        ButtonInvocationContext invocationContext = this.invocationContextFactory.button(new InteractionEvent(event), buttonContext);

        Exceptional<Object> result = buttonContext.invoke(invocationContext);

        if (result.present()) {
            this.displayResult(halpbotEvent, buttonContext, result.get());
        }
        else if (result.caught()) {
            event.deferEdit(); // Prevent interaction failed event
            this.handleException(halpbotEvent, result.error());
        }
    }
}
