package nz.pumbas.halpbot.buttons;

import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.hooks.SubscribeEvent;

import org.dockbox.hartshorn.core.context.element.MethodContext;
import org.dockbox.hartshorn.core.context.element.TypeContext;

import java.util.List;

import nz.pumbas.halpbot.adapters.HalpbotAdapter;

public interface ButtonAdapter extends HalpbotAdapter
{
    default <T> void registerButtons(TypeContext<T> type) {
        final T instance = this.applicationContext().get(type);
        List<MethodContext<?, T>> buttons = type.methods(ButtonAction.class);

        for (MethodContext<?, T> button : buttons) {
            if (!button.isPublic()) {
                this.applicationContext().log()
                        .warn("The button action %s must be public if its annotated with @ButtonCommand"
                                .formatted(button.qualifiedName()));
                return;
            }
            this.registerButton(instance, button);
        }

        this.applicationContext().log().info("Registered %d buttons found in %s"
                .formatted(buttons.size(), type.qualifiedName()));
    }

    <T> void registerButton(T instance, MethodContext<?, T> buttonMethodContext);

    @SubscribeEvent
    void onButtonClick(ButtonClickEvent event);
}
