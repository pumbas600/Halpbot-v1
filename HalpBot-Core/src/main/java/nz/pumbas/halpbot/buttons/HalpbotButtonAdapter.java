package nz.pumbas.halpbot.buttons;

import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.hooks.SubscribeEvent;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.dockbox.hartshorn.core.HartshornUtils;
import org.dockbox.hartshorn.core.annotations.inject.Binds;
import org.dockbox.hartshorn.core.annotations.stereotype.Service;
import org.dockbox.hartshorn.core.context.ApplicationContext;
import org.dockbox.hartshorn.core.context.element.MethodContext;
import org.dockbox.hartshorn.core.domain.Exceptional;

import java.time.Duration;
import java.util.Map;

import javax.inject.Inject;

import lombok.Getter;
import nz.pumbas.halpbot.HalpbotCore;
import nz.pumbas.halpbot.actions.invokable.ActionInvokable;
import nz.pumbas.halpbot.common.ExplainedException;
import nz.pumbas.halpbot.configurations.DisplayConfiguration;
import nz.pumbas.halpbot.decorators.DecoratorService;
import nz.pumbas.halpbot.events.HalpbotEvent;
import nz.pumbas.halpbot.events.InteractionEvent;

@Service
@Binds(ButtonAdapter.class)
public class HalpbotButtonAdapter implements ButtonAdapter
{
    private final Map<String, ButtonContext> registeredButtons = HartshornUtils.emptyMap();

    @Inject private ButtonContextFactory buttonContextFactory;
    @Inject private DecoratorService decoratorService;

    @Inject @Getter private ApplicationContext applicationContext;
    @Inject @Getter private HalpbotCore halpbotCore;

    @Override
    public <T> void registerButton(T instance, MethodContext<?, T> buttonMethodContext) {
        ButtonAction button = buttonMethodContext.annotation(ButtonAction.class).get();
        String id = button.id().isBlank() ? buttonMethodContext.name() : button.id();
        ButtonContext buttonContext = this.createButton(
                id,
                button,
                new HalpbotButtonInvokable(instance, buttonMethodContext)); //TODO: Use factory instead

        this.registeredButtons.put(id, buttonContext);
    }

    private <T> ButtonContext createButton(String id,
                                           ButtonAction buttonAction,
                                           ActionInvokable<ButtonInvocationContext> actionInvokable)
    {
        return this.buttonContextFactory.create(
                id,
                buttonAction.isEphemeral(),
                Duration.of(buttonAction.displayDuration().value(), buttonAction.displayDuration().unit()),
                this.decoratorService.decorate(actionInvokable)
        );
    }

    @Override
    @SubscribeEvent
    public void onButtonClick(ButtonClickEvent event) {
        if (this.registeredButtons.containsKey(event.getComponentId())) {
            ButtonContext buttonContext = this.registeredButtons.get(event.getComponentId());
            HalpbotEvent halpbotEvent = new InteractionEvent(event);
            Exceptional<Object> result = buttonContext.invoke(event);

            if (result.present()) {
                DisplayConfiguration displayConfiguration = this.halpbotCore.displayConfiguration();
                if (buttonContext.isEphemeral())
                    displayConfiguration.displayTemporary(halpbotEvent, result.get(), 0);
                else displayConfiguration.display(halpbotEvent, result.get(), buttonContext.displayDuration());
            }
            else if (result.caught()) {
                Throwable exception = result.error();
                //ErrorManager.handle(event, exception);

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
}
