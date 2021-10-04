package nz.pumbas.halpbot.adapters;

import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;

import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import nz.pumbas.halpbot.buttons.ButtonAction;
import nz.pumbas.halpbot.buttons.ButtonCallback;

public class ButtonAdapter extends HalpbotAdapter
{
    private final Map<String, ButtonCallback> parsedButtonCallbacks = new HashMap<>();

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

    }

    private void parseButtonCallbacks(Object object) {
        for (Method method : object.getClass().getDeclaredMethods()) {
            if (method.isAnnotationPresent(ButtonAction.class)) {
                // Generate callback data
            }
        }
    }
}
