package nz.pumbas.halpbot.adapters;

import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.Button;

import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;

import nz.pumbas.halpbot.actions.ActionCallback;
import nz.pumbas.halpbot.actions.ActionHandler;
import nz.pumbas.halpbot.actions.ButtonActionCallback;
import nz.pumbas.halpbot.actions.annotations.ButtonAction;
import nz.pumbas.halpbot.events.HalpbotEvent;
import nz.pumbas.halpbot.events.InteractionEvent;
import nz.pumbas.halpbot.utilities.ConcurrentManager;

public class ButtonAdapter extends HalpbotAdapter implements ActionHandler
{
    private final ConcurrentManager concurrentManager = new ConcurrentManager();
    private final Queue<String> expiredButtonCallbacks = new ConcurrentLinkedQueue<>();
    private final Map<String, ButtonActionCallback> parsedButtonCallbacks = new HashMap<>();

    @Override
    public void registerObjects(Object... objects) {
        for (Object object : objects) {
            this.parseButtonCallbacks(object);
        }
    }

    @Override
    public void onButtonClick(@NotNull ButtonClickEvent event) {
        if (event.getUser().isBot()) {
            return;
        }

        HalpbotEvent halpbotEvent = new InteractionEvent(event);
        if (this.expiredButtonCallbacks.contains(event.getComponentId())) {
            this.halpBotCore.getDisplayConfiguration()
                .displayTemporary(halpbotEvent, "This button is no longer being used to save resources sorry :)", -1);
            event.getMessage()
                .editMessageComponents(ActionRow.of(
                    event.getMessage()
                        .getButtons()
                        .stream()
                        .map(Button::asDisabled)
                        .collect(Collectors.toList())))
                .queue(success -> {
                    this.expiredButtonCallbacks.remove(event.getComponentId());
                    this.parsedButtonCallbacks.remove(event.getComponentId());
                });
            return;
        }

        ButtonActionCallback actionCallback = this.parsedButtonCallbacks.get(event.getComponentId());
        this.handle(actionCallback, halpbotEvent);
        if (!event.isAcknowledged())
            event.deferEdit().queue();
    }

    private void parseButtonCallbacks(Object object) {
        for (Method method : object.getClass().getDeclaredMethods()) {
            if (method.isAnnotationPresent(ButtonAction.class)) {
                method.setAccessible(true);
                // Generate callback data
                ButtonActionCallback actionCallback = ActionCallback.builder()
                    .setButtonAction(object, method)
                    .buildButtonCallback();
                this.parsedButtonCallbacks.put(method.getName(), actionCallback);
            }
        }
    }

    private String getModifiedId(String actionCallback) {
        long suffix = 0;
        String modifiedId;
        do {
            modifiedId = actionCallback + suffix;
            suffix++;
        } while (this.parsedButtonCallbacks.containsKey(modifiedId));
        return modifiedId;
    }

    public Button register(Button button, Object... parameters) {
        final String actionCallback = button.getId();
        if (!this.parsedButtonCallbacks.containsKey(actionCallback))
            throw new IllegalArgumentException("The callback: " + actionCallback + " doesn't seem to be registered");

        ButtonActionCallback copiedActionCallback = this.parsedButtonCallbacks.get(actionCallback)
            .copy()
            .setParameters(parameters)
            .buildButtonCallback();

        final String modifiedId = this.getModifiedId(actionCallback);
        Button modifiedButton = button.withId(modifiedId);

        this.parsedButtonCallbacks.put(modifiedId, copiedActionCallback);
        if (0 < copiedActionCallback.getDeleteAfterDuration()) {
            this.concurrentManager.schedule(
                copiedActionCallback.getDeleteAfterDuration(),
                copiedActionCallback.getDeleteAfterTimeUnit(),
                () -> this.expiredButtonCallbacks.add(modifiedId));
        }
        return modifiedButton;
    }

    @Override
    public String getActionId(HalpbotEvent event) {
        return event.getEvent(ButtonClickEvent.class).getComponentId();
    }

    @Override
    public void removeActionCallbacks(HalpbotEvent halpbotEvent) {
        ButtonClickEvent event = halpbotEvent.getEvent(ButtonClickEvent.class);
        this.parsedButtonCallbacks.remove(event.getComponentId());
        event.editButton(event.getButton().asDisabled()).queue();
    }

    @Override
    public boolean displayTemporarily(ActionCallback actionCallback) {
        return actionCallback.displayTemporarily() ||
            actionCallback instanceof ButtonActionCallback &&
            ((ButtonActionCallback) actionCallback).isEphemeral();
    }
}
