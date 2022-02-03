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

package nz.pumbas.halpbot.buttons;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.Button;
import net.dv8tion.jda.api.interactions.components.Component;

import org.dockbox.hartshorn.core.annotations.inject.ComponentBinding;
import org.dockbox.hartshorn.core.context.ApplicationContext;
import org.dockbox.hartshorn.core.context.element.MethodContext;
import org.dockbox.hartshorn.core.domain.Exceptional;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
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
import nz.pumbas.halpbot.objects.AsyncDuration;
import nz.pumbas.halpbot.utilities.HalpbotUtils;

@Singleton
@Accessors(chain = false)
@ComponentBinding(ButtonAdapter.class)
public class HalpbotButtonAdapter implements ButtonAdapter
{
    @Getter @Setter private int idSuffix;
    // This will be overwritten when the bot starts such that the prefix is unique to this particular bot
    @Getter @Setter private String dynamicPrefix = "HB-TEMP";

    private final Map<String, ButtonContext> registeredButtons = new ConcurrentHashMap<>();
    private final Map<String, ButtonContext> dynamicButtons = new ConcurrentHashMap<>();
    private final Map<String, AfterRemovalFunction> afterRemovalFunctions = new ConcurrentHashMap<>();
    private final Map<String, ScheduledFuture<?>> scheduledExpirations = new ConcurrentHashMap<>();

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
        final String newId = this.generateDynamicId(id);
        ButtonContext newButtonContext = this.buttonContextFactory
                .create(newId, parameters, buttonContext, buttonContext.afterRemoval());

        this.dynamicButtons.put(newId, newButtonContext);
        if (newButtonContext.isUsingDuration()) {
            AsyncDuration duration = newButtonContext.removeAfter();

            // Store the completable future so that it can be cancelled later if the dynamic button is unregistered
            this.scheduledExpirations.put(newId, this.halpbotCore.threadpool()
                    .schedule(
                            () -> this.removeDynamicButton(newId, true),
                            duration.value(),
                            duration.unit()));
        }

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
        if (this.scheduledExpirations.containsKey(id)) {
            this.scheduledExpirations.get(id).cancel(false);
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
                buttonAction.maxUses(),
                HalpbotUtils.asAsyncDuration(buttonAction.removeAfter()),
                buttonAction.afterRemoval().strategy()
        );
    }

    private ButtonContext retrieveDynamicButtonContext(String id) {
        ButtonContext buttonContext = this.dynamicButtons.get(id);
        // The button context has already been checked to make sure it's not past it's expiration time (If
        // applicable) so we only need to determine if it still has any uses left.

        if (buttonContext.hasUses()) {
            buttonContext.deductUse();
            // Now check if it no longer has any uses
            if (!buttonContext.hasUses())
                this.removeDynamicButton(id, true);
        }

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
                this.handleRemovalFunctions(event);
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

    private boolean removalFunctionsApplies(ButtonClickEvent event) {
        return event.getMessage().getButtons().stream()
                .anyMatch(button -> {
                    final String id = button.getId();
                    return id != null && this.afterRemovalFunctions.containsKey(id);
                });
    }

    private void handleRemovalFunctions(ButtonClickEvent event) {
        if (!this.afterRemovalFunctions.isEmpty() && this.removalFunctionsApplies(event)) {
            final List<ActionRow> rows = new ArrayList<>();

            for (ActionRow row : event.getMessage().getActionRows()) {
                List<Component> components = new ArrayList<>();
                for (Component component : row.getComponents()) {
                    final String componentId = component.getId();
                    final AfterRemovalFunction afterRemoval = this.afterRemovalFunctions.remove(componentId);

                    if (null != afterRemoval) {
                        Component modifiedComponent = afterRemoval.apply(component);
                        components.add(modifiedComponent);
                        continue;
                    }
                    components.add(component);
                }
                rows.add(ActionRow.of(components));
            }
            event.getHook().editMessageComponentsById(event.getMessageIdLong(), rows).queue();
        }
    }
}
