package nz.pumbas.halpbot.buttons;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.Button;
import net.dv8tion.jda.api.interactions.components.Component;

import org.dockbox.hartshorn.core.HartshornUtils;
import org.dockbox.hartshorn.core.annotations.inject.ComponentBinding;
import org.dockbox.hartshorn.core.context.ApplicationContext;
import org.dockbox.hartshorn.core.context.element.MethodContext;
import org.dockbox.hartshorn.core.domain.Exceptional;
import org.jetbrains.annotations.Nullable;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
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
    @Getter @Setter private String dynamicPrefix;

    private final Map<String, ButtonContext> registeredButtons = HartshornUtils.emptyMap();
    private final Map<String, ButtonContext> dynamicButtons = HartshornUtils.emptyMap();
    private final Map<String, OffsetDateTime> dynamicButtonExpirations = new ConcurrentHashMap<>();
    private final Map<String, AfterRemovalFunction> afterRemovalFunctions = new ConcurrentHashMap<>();

    @Inject private TokenService tokenService;
    @Inject private DecoratorService decoratorService;
    @Inject private InvocationContextFactory invocationContextFactory;
    @Inject private ButtonContextFactory buttonContextFactory;

    @Inject @Getter private ApplicationContext applicationContext;
    @Inject @Getter private HalpbotCore halpbotCore;

    @Override
    public void onCreation(JDA jda) {
        this.dynamicPrefix("HB-" + jda.getSelfUser().getAsTag());
    }

    @Override
    public <T> void registerButton(T instance, MethodContext<?, T> methodContext) {
        ButtonAction buttonAction = methodContext.annotation(ButtonAction.class).get();
        ButtonContext buttonContext = this.createButton(
                buttonAction.id(),
                buttonAction,
                new HalpbotButtonInvokable(instance, methodContext), //TODO: Use factory instead
                new Object[0]);

        this.registeredButtons.put(buttonAction.id(), buttonContext);
    }

    @Override
    @Nullable
    public ButtonContext buttonContext(@Nullable String id) {
        return this.registeredButtons.get(id);
    }

    @Override
    public Button register(Button button, Object... parameters) {
        if (this.isInvalid(button))
            return button;

        ButtonContext buttonContext = this.registeredButtons.get(button.getId());
        return this.register(button, buttonContext.afterRemoval(), parameters);
    }

    @Override
    public Button register(Button button,
                           @Nullable AfterRemovalFunction afterRemoval,
                           Object... parameters)
    {
        if (this.isInvalid(button))
            return button;

        String id = button.getId();
        ButtonContext buttonContext = this.registeredButtons.get(id);

        assert id != null; // id will never be null as this would invalidate the button
        String newId = this.generateDynamicId(id);
        ButtonContext newButtonContext = this.buttonContextFactory
                .create(newId, parameters, buttonContext, buttonContext.afterRemoval());

        this.dynamicButtons.put(newId, newButtonContext);
        if (newButtonContext.isUsingDuration())
            this.dynamicButtonExpirations.put(newId, OffsetDateTime.now().plus(newButtonContext.displayDuration()));

        return button.withId(newId);
    }

    private boolean isInvalid(Button button) {
        String id = button.getId();
        if (id == null) {
            this.applicationContext.log().warn("You cannot register a button that has no id with the button adapter");
            return true;
        }

        ButtonContext buttonContext = this.buttonContext( button.getId());
        if (buttonContext == null) {
            this.applicationContext.log().error(
                    "You cannot register a button with the id %s as there is no matching button action for it"
                            .formatted(button.getId()));
            return true;
        }
        return false;
    }

    @Override
    public void unregister(String id, boolean applyRemovalFunction) {
        if (this.registeredButtons.containsKey(id))
            this.registeredButtons.remove(id);
        else this.removeDynamicButton(id, applyRemovalFunction);
    }

    private void removeDynamicButton(String id, boolean applyRemovalFunction) {
        if (!this.dynamicButtons.containsKey(id))
            return;

        ButtonContext buttonContext = this.dynamicButtons.remove(id);
        if (buttonContext.isUsingDuration()) {
            this.dynamicButtonExpirations.remove(id);
        }

        if (applyRemovalFunction) {
            AfterRemovalFunction afterRemoval = buttonContext.afterRemoval();
            if (afterRemoval != null) {
                this.afterRemovalFunctions.put(id, afterRemoval);
            }
        }
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
                buttonAction.uses(),
                HalpbotUtils.asDuration(buttonAction.after()),
                buttonAction.afterRemoval().strategy()
        );
    }

    private ButtonContext retrieveDynamicButtonContext(String id) {
        boolean remove = false;
        ButtonContext buttonContext = this.dynamicButtons.get(id);
        if (buttonContext.hasUses()) {
            buttonContext.deductUse();
            if (!buttonContext.hasUses())
                remove = true;
        }
        if (!remove && buttonContext.isUsingDuration() &&
            OffsetDateTime.now().isAfter(this.dynamicButtonExpirations.get(id))) {
                remove = true;
        }

        if (remove) // Remove the button if it's expired
            this.removeDynamicButton(id, true);
        return buttonContext;
    }


    @Override
    public void onButtonClick(ButtonClickEvent event) {
        String id = event.getComponentId();

        HalpbotEvent halpbotEvent = new InteractionEvent(event);
        ButtonContext buttonContext;

        if (this.isDynamic(id)) {
            if (this.dynamicButtons.containsKey(id)) {
                buttonContext = this.retrieveDynamicButtonContext(id);
            }
            else { // The button has expired
                this.halpbotCore().displayConfiguration()
                        .displayTemporary(halpbotEvent, "This button has expired", -30);
                return;
            }
        }
        else if (this.registeredButtons.containsKey(id)) {
            buttonContext = this.registeredButtons.get(id);
        }
        else return; // Not a halpbot button

        ButtonInvocationContext invocationContext = this.invocationContextFactory.button(halpbotEvent, buttonContext);
        Exceptional<Object> result = buttonContext.invoke(invocationContext);

        if (result.present()) {
            this.displayResult(halpbotEvent, buttonContext, result.get());
        }
        else if (result.caught()) {
            event.deferEdit(); // Prevent interaction failed event
            this.handleException(halpbotEvent, result.error());
        }

        this.handleRemovalFunctions(event);
    }

    private void handleRemovalFunctions(ButtonClickEvent event) {
        if (!this.afterRemovalFunctions.isEmpty()) {
            boolean changedComponent = false;
            final List<ActionRow> rows = new ArrayList<>();

            for (ActionRow row : event.getMessage().getActionRows()) {
                List<Component> components = new ArrayList<>();
                this.applicationContext.log().info(row.getComponents().toString());
                for (Component component : row.getComponents()) {
                    final String componentId = component.getId();
                    final AfterRemovalFunction afterRemoval = this.afterRemovalFunctions.remove(componentId);

                    if (null != afterRemoval) {
                        components.add(afterRemoval.apply(component));
                        changedComponent = true;
                        continue;
                    }
                    components.add(component);
                }
                rows.add(ActionRow.of(components));
            }
            if (changedComponent)
                event.getHook().editOriginalComponents(rows).queue();

        }
    }
}
