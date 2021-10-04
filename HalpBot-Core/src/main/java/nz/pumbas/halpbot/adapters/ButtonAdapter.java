package nz.pumbas.halpbot.adapters;

import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;

import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import nz.pumbas.halpbot.actions.AbstractActionCallback;
import nz.pumbas.halpbot.actions.annotations.ButtonAction;

public class ButtonAdapter extends HalpbotAdapter
{
    private final Map<String, AbstractActionCallback> parsedButtonCallbacks = new HashMap<>();

    @Override
    public void register(Object... objects) {
        for (Object object : objects) {
            this.parseButtonCallbacks(object);
        }
    }

    @Override
    public void onButtonClick(@NotNull ButtonClickEvent event) {
        if (!this.parsedButtonCallbacks.containsKey(event.getComponentId())) {
            return;
        }
        event.reply("Test").setEphemeral(true);
    }

    private void parseButtonCallbacks(Object object) {
        for (Method method : object.getClass().getDeclaredMethods()) {
            if (method.isAnnotationPresent(ButtonAction.class)) {
                // Generate callback data
            }
        }
    }
}