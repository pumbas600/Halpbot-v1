package nz.pumbas.halpbot.buttons;

import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.interactions.components.Button;

import org.dockbox.hartshorn.core.context.element.MethodContext;
import org.dockbox.hartshorn.core.context.element.TypeContext;
import org.dockbox.hartshorn.core.domain.Exceptional;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import nz.pumbas.halpbot.adapters.HalpbotAdapter;

public interface ButtonAdapter extends HalpbotAdapter
{
    @Override
    default void onEvent(GenericEvent event) {
        if (event instanceof ButtonClickEvent buttonClickEvent)
            this.onButtonClick(buttonClickEvent);
    }

    void onButtonClick(ButtonClickEvent event);

    default <T> void registerButtons(TypeContext<T> type) {
        final T instance = this.applicationContext().get(type);
        List<MethodContext<?, T>> buttons = type.methods(ButtonAction.class);

        for (MethodContext<?, T> button : buttons) {
            if (!button.isPublic()) {
                this.applicationContext().log()
                        .warn("The button action %s must be public if its annotated with @ButtonCommand"
                                .formatted(button.qualifiedName()));
                continue;
            }
            this.registerButton(instance, button);
        }

        this.applicationContext().log().info("Registered %d buttons found in %s"
                .formatted(buttons.size(), type.qualifiedName()));
    }

    <T> void registerButton(T instance, MethodContext<?, T> buttonMethodContext);

    default String id(ButtonAction button, MethodContext<?, ?> methodContext) {
        return button.id().isBlank() ? methodContext.name() : button.id();
    }

    @Nullable
    ButtonContext buttonContext(@Nullable String id);

    default Exceptional<ButtonContext> buttonContextSafely(String id) {
        return Exceptional.of(this.buttonContext(id));
    }

    Button register(Button button, Object... parameters);
}
