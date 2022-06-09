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

package net.pumbas.halpbot.buttons;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.Button;
import net.dv8tion.jda.api.interactions.components.Component;
import net.pumbas.halpbot.HalpbotCore;
import net.pumbas.halpbot.actions.invokable.ActionInvokable;
import net.pumbas.halpbot.actions.invokable.InvocationContextFactory;
import net.pumbas.halpbot.converters.tokens.ParsingToken;
import net.pumbas.halpbot.converters.tokens.TokenService;
import net.pumbas.halpbot.decorators.DecoratorService;
import net.pumbas.halpbot.events.HalpbotEvent;
import net.pumbas.halpbot.events.InteractionEvent;
import net.pumbas.halpbot.objects.AsyncDuration;
import net.pumbas.halpbot.utilities.HalpbotUtils;

import org.dockbox.hartshorn.application.context.ApplicationContext;
import org.dockbox.hartshorn.inject.binding.ComponentBinding;
import org.dockbox.hartshorn.util.Result;
import org.dockbox.hartshorn.util.reflect.MethodContext;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.stream.Collectors;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Singleton
@Accessors(chain = false)
@ComponentBinding(ButtonAdapter.class)
public class HalpbotButtonAdapter implements ButtonAdapter {

    private final Map<String, ButtonContext> registeredButtons = new ConcurrentHashMap<>();
    private final Map<String, ButtonContext> dynamicButtons = new ConcurrentHashMap<>();
    private final Map<String, AfterRemovalFunction> afterRemovalFunctions = new ConcurrentHashMap<>();
    private final Map<String, ScheduledFuture<?>> scheduledExpirations = new ConcurrentHashMap<>();
    @Getter
    @Setter
    private int idSuffix;
    // This will be overwritten when the bot starts such that the prefix is unique to this particular bot
    @Getter
    @Setter
    private String dynamicPrefix = "HB-TEMP";
    @Inject
    private TokenService tokenService;
    @Inject
    private DecoratorService decoratorService;
    @Inject
    private InvocationContextFactory invocationContextFactory;
    @Inject
    private ButtonContextFactory buttonContextFactory;

    @Inject
    @Getter
    private ApplicationContext applicationContext;
    @Inject
    @Getter
    private HalpbotCore halpbotCore;

    @Override
    public void initialise(final JDA jda) {
        this.dynamicPrefix("HB-" + jda.getSelfUser().getAsTag());
    }

    @Override
    public void onButtonClick(final ButtonClickEvent event) {
        final String id = event.getComponentId();

        final HalpbotEvent halpbotEvent = new InteractionEvent(event);
        final ButtonContext buttonContext;

        if (this.isDynamic(id)) {
            if (this.dynamicButtons.containsKey(id)) {
                buttonContext = this.retrieveDynamicButtonContext(id);
            } else { // The button has expired
                this.halpbotCore().displayConfiguration()
                    .displayTemporary(halpbotEvent, "This button has expired", -30);
                this.handleRemovalFunctions(event);
                return;
            }
        } else if (this.registeredButtons.containsKey(id)) {
            buttonContext = this.registeredButtons.get(id);
        } else return; // Not a halpbot button

        final ButtonInvocationContext invocationContext = this.invocationContextFactory.button(halpbotEvent, buttonContext);
        final Result<Object> result = buttonContext.invoke(invocationContext);

        if (result.present()) {
            this.displayResult(halpbotEvent, buttonContext, result.get());
        } else if (result.caught()) {
            event.deferEdit(); // Prevent interaction failed event
            this.handleException(halpbotEvent, result.error());
        }

        this.handleRemovalFunctions(event);
    }

    @Override
    public <T> void registerButton(final T instance, final MethodContext<?, T> methodContext) {
        final ButtonHandler buttonHandler = methodContext.annotation(ButtonHandler.class).get();
        final ButtonContext buttonContext = this.createButton(
            buttonHandler.id(),
            buttonHandler,
            new HalpbotButtonInvokable(instance, methodContext), //TODO: Use factory instead
            new Object[0]);

        this.registeredButtons.put(buttonHandler.id(), buttonContext);
    }

    @Override
    @Nullable
    public ButtonContext buttonContext(@Nullable final String id) {
        return this.registeredButtons.get(id);
    }

    @Override
    public Button register(final Button button, final Object... parameters) {
        if (this.isInvalid(button))
            return button;

        final ButtonContext buttonContext = this.registeredButtons.get(button.getId());
        return this.register(button, buttonContext.afterRemoval(), parameters);
    }

    @Override
    public Button register(final Button button,
                           @Nullable final AfterRemovalFunction afterRemoval,
                           final Object... parameters)
    {
        if (this.isInvalid(button))
            return button;

        final String id = button.getId();
        final ButtonContext buttonContext = this.registeredButtons.get(id);

        assert id != null; // id will never be null as this would invalidate the button
        final String newId = this.generateDynamicId(id);
        final ButtonContext newButtonContext = this.buttonContextFactory
            .create(newId, parameters, buttonContext, buttonContext.afterRemoval());

        this.dynamicButtons.put(newId, newButtonContext);
        if (newButtonContext.isUsingDuration()) {
            final AsyncDuration duration = newButtonContext.removeAfter();

            // Store the completable future so that it can be cancelled later if the dynamic button is unregistered
            this.scheduledExpirations.put(newId, this.halpbotCore.threadpool()
                .schedule(
                    () -> this.removeDynamicButton(newId, true),
                    duration.value(),
                    duration.unit()));
        }

        return button.withId(newId);
    }

    @Override
    public void unregister(final String id, final boolean applyRemovalFunction) {
        if (this.registeredButtons.containsKey(id))
            this.registeredButtons.remove(id);
        else this.removeDynamicButton(id, applyRemovalFunction);
    }

    private boolean isInvalid(final Button button) {
        final String id = button.getId();
        if (id == null) {
            this.applicationContext.log().warn("You cannot register a button that has no id with the button adapter");
            return true;
        }

        final ButtonContext buttonContext = this.buttonContext(button.getId());
        if (buttonContext == null) {
            this.applicationContext.log().error(
                "You cannot register a button with the id %s as there is no matching button action for it"
                    .formatted(button.getId()));
            return true;
        }
        return false;
    }

    private void removeDynamicButton(final String id, final boolean applyRemovalFunction) {
        if (!this.dynamicButtons.containsKey(id))
            return;

        final ButtonContext buttonContext = this.dynamicButtons.remove(id);
        if (this.scheduledExpirations.containsKey(id)) {
            this.scheduledExpirations.get(id).cancel(false);
        }

        if (applyRemovalFunction) {
            final AfterRemovalFunction afterRemoval = buttonContext.afterRemoval();
            if (afterRemoval != null) {
                this.afterRemovalFunctions.put(id, afterRemoval);
            }
        }
    }

    private ButtonContext createButton(final String id,
                                       final ButtonHandler buttonHandler,
                                       final ActionInvokable<ButtonInvocationContext> actionInvokable,
                                       final Object[] passedParameters)
    {
        return this.buttonContextFactory.create(
            id,
            buttonHandler.isEphemeral(),
            HalpbotUtils.asDuration(buttonHandler.display()),
            this.decoratorService.decorate(actionInvokable),
            passedParameters,
            this.tokenService.tokens(actionInvokable.executable())
                .stream()
                .filter(token -> token instanceof ParsingToken parsingToken && !parsingToken.isCommandParameter())
                .map(token -> (ParsingToken) token)
                .collect(Collectors.toList()),
            buttonHandler.maxUses(),
            HalpbotUtils.asAsyncDuration(buttonHandler.removeAfter()),
            buttonHandler.afterRemoval().strategy()
        );
    }

    private ButtonContext retrieveDynamicButtonContext(final String id) {
        final ButtonContext buttonContext = this.dynamicButtons.get(id);
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

    private void handleRemovalFunctions(final ButtonClickEvent event) {
        if (!this.afterRemovalFunctions.isEmpty() && this.removalFunctionsApplies(event)) {
            final List<ActionRow> rows = new ArrayList<>();

            for (final ActionRow row : event.getMessage().getActionRows()) {
                final List<Component> components = new ArrayList<>();
                for (final Component component : row.getComponents()) {
                    final String componentId = component.getId();
                    final AfterRemovalFunction afterRemoval = this.afterRemovalFunctions.remove(componentId);

                    if (null != afterRemoval) {
                        final Component modifiedComponent = afterRemoval.apply(component);
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

    private boolean removalFunctionsApplies(final ButtonClickEvent event) {
        return event.getMessage().getButtons().stream()
            .anyMatch(button -> {
                final String id = button.getId();
                return id != null && this.afterRemovalFunctions.containsKey(id);
            });
    }
}
