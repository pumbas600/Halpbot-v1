package nz.pumbas.halpbot.buttons;

import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.interactions.components.Button;

import org.dockbox.hartshorn.core.HartshornUtils;
import org.dockbox.hartshorn.core.annotations.inject.ComponentBinding;
import org.dockbox.hartshorn.core.context.ApplicationContext;
import org.dockbox.hartshorn.core.context.element.MethodContext;
import org.dockbox.hartshorn.core.domain.Exceptional;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.Map;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import nz.pumbas.halpbot.HalpbotCore;
import nz.pumbas.halpbot.actions.invokable.ActionInvokable;
import nz.pumbas.halpbot.actions.invokable.InvocationContextFactory;
import nz.pumbas.halpbot.converters.tokens.ParsingToken;
import nz.pumbas.halpbot.converters.tokens.TokenService;
import nz.pumbas.halpbot.decorators.DecoratorService;
import nz.pumbas.halpbot.events.HalpbotEvent;
import nz.pumbas.halpbot.events.InteractionEvent;
import nz.pumbas.halpbot.utilities.HalpbotUtils;

@Singleton
@Accessors(chain = false)
@ComponentBinding(ButtonAdapter.class)
public class HalpbotButtonAdapter implements ButtonAdapter
{
    @Getter @Setter private int idSuffix;
    private final Map<String, ButtonContext> registeredButtons = HartshornUtils.emptyMap();
    private final Map<String, ButtonContext> dynamicButtons = HartshornUtils.emptyMap();

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

        String newId = this.generateDynamicId(id);
        ButtonContext newButtonContext = this.buttonContextFactory
                .create(newId, parameters, buttonContext);

        this.dynamicButtons.put(newId, newButtonContext);
        //TODO: Remove after awhile

        return button.withId(newId);
    }

    @Override
    public void unregister(String id) {
        this.registeredButtons.remove(id);
    }

    private ButtonContext createButton(String id,
                                       ButtonAction buttonAction,
                                       ActionInvokable<ButtonInvocationContext> actionInvokable,
                                       Object[] passedParameters)
    {
        return this.buttonContextFactory.create(
                id,
                buttonAction.isEphemeral(),
                HalpbotUtils.asDuration(buttonAction.display()),
                this.decoratorService.decorate(actionInvokable),
                passedParameters,
                this.tokenService.tokens(actionInvokable.executable())
                        .stream()
                        .filter(token -> token instanceof ParsingToken parsingToken && !parsingToken.isCommandParameter())
                        .map(token -> (ParsingToken) token)
                        .collect(Collectors.toList()),
                buttonAction.afterUsages(),
                HalpbotUtils.asDuration(buttonAction.after())
        );
    }

    @Override
    public void onButtonClick(ButtonClickEvent event) {
        String id = event.getComponentId();

        HalpbotEvent halpbotEvent = new InteractionEvent(event);
        ButtonContext buttonContext;
        if (this.isDynamic(id)) {
            if (this.dynamicButtons.containsKey(id))
                buttonContext = this.dynamicButtons.get(id);
            else { // The button has expired
                this.halpbotCore().displayConfiguration()
                        .displayTemporary(halpbotEvent, "This button has expired", 30);
                return;
            }
        }
        else if (this.registeredButtons.containsKey(id)) {
            buttonContext = this.registeredButtons.get(id);
        }
        else return; // Not a halpbot button

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
