package nz.pumbas.halpbot.adapters;

import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;

import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import nz.pumbas.halpbot.actions.ActionCallback;
import nz.pumbas.halpbot.actions.ActionHandler;
import nz.pumbas.halpbot.actions.ButtonActionCallback;
import nz.pumbas.halpbot.actions.annotations.ButtonAction;
import nz.pumbas.halpbot.commands.events.HalpbotEvent;
import nz.pumbas.halpbot.commands.events.InteractionEvent;

public class ButtonAdapter extends HalpbotAdapter implements ActionHandler
{
    private final Map<String, ButtonActionCallback> parsedButtonCallbacks = new HashMap<>();

    @Override
    public void register(Object... objects) {
        for (Object object : objects) {
            this.parseButtonCallbacks(object);
        }
    }

    @Override
    public void onButtonClick(@NotNull ButtonClickEvent event) {
        if (event.getUser().isBot() || !this.parsedButtonCallbacks.containsKey(event.getComponentId().toLowerCase(Locale.ROOT))) {
            return;
        }

        ButtonActionCallback actionCallback = this.parsedButtonCallbacks.get(event.getComponentId().toLowerCase(Locale.ROOT));
        this.handle(actionCallback, new InteractionEvent(event));
    }

    private void parseButtonCallbacks(Object object) {
        for (Method method : object.getClass().getDeclaredMethods()) {
            if (method.isAnnotationPresent(ButtonAction.class)) {
                // Generate callback data
                ButtonActionCallback action = ActionCallback.builder()
                    .setButtonAction(object, method)
                    .buildButtonCallback();
                this.parsedButtonCallbacks.put(method.getName().toLowerCase(Locale.ROOT), action);
            }
        }
    }

    @Override
    public String getActionId(HalpbotEvent event) {
        return event.getEvent(ButtonClickEvent.class).getComponentId();
    }

    @Override
    public void removeActionCallbacks(HalpbotEvent halpbotEvent) {
        ButtonClickEvent event = halpbotEvent.getEvent(ButtonClickEvent.class);
        this.parsedButtonCallbacks.remove(event.getComponentId().toLowerCase(Locale.ROOT));
        event.editButton(null);
    }

    @Override
    public boolean displayTemporarily(ActionCallback actionCallback) {
        return actionCallback.displayTemporarily() ||
            actionCallback instanceof ButtonActionCallback &&
            ((ButtonActionCallback) actionCallback).isEphemeral();
    }
}
