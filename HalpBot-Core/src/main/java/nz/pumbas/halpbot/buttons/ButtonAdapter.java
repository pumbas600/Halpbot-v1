package nz.pumbas.halpbot.buttons;

import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.interactions.components.Button;

import org.dockbox.hartshorn.core.context.element.MethodContext;
import org.dockbox.hartshorn.core.context.element.TypeContext;
import org.dockbox.hartshorn.core.domain.Exceptional;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import nz.pumbas.halpbot.adapters.HalpbotAdapter;

public interface ButtonAdapter extends HalpbotAdapter
{
    String DYNAMIC_PREFIX = "DYN-BTN";
    String DYNAMIC_ID_FORMAT = DYNAMIC_PREFIX + "$$%s$$%d-%d";
    Pattern DYNAMIC_ID_EXTRACTION_PATTERN = Pattern.compile(".*\\$\\$(.*)\\$\\$.*");

    int idSuffix();

    void idSuffix(int idSuffix);

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

    void unregister(String id);

    default void unregister(Button button) {
        String id = button.getId();
        if (id != null)
            this.unregister(button.getId());
    }

    default List<Button> register(List<Button> buttons, Object... parameters) {
        return buttons.stream()
                .map((button) -> this.register(button, parameters))
                .toList();
    }

    default String generateDynamicId(String currentId) {
        // The id suffix prevents buttons registered within the same millisecond having the same id. 1000 is used so
        // that it won't take up any more than 3 characters
        this.idSuffix((this.idSuffix() + 1) % 1000);
        return DYNAMIC_ID_FORMAT.formatted(currentId, System.currentTimeMillis(), this.idSuffix());
    }

    default Exceptional<String> extractOriginalIdSafely(String dynamicId) {
        String extractedId = this.extractOriginalId(dynamicId);

        if (extractedId == null)
            return Exceptional.of(
                    new IllegalArgumentException("The specified id did not match the dynamic id format of '%s'"
                            .formatted(DYNAMIC_ID_FORMAT)));

        return Exceptional.of(extractedId);
    }

    @Nullable
    default String extractOriginalId(String dynamicId) {
        Matcher matcher = DYNAMIC_ID_EXTRACTION_PATTERN.matcher(dynamicId);
        if (matcher.matches()) {
            return matcher.group(1);
        }
        return null;
    }

    default boolean isDynamic(String id) {
        return id.startsWith(DYNAMIC_PREFIX);
    }

    default boolean isDynamic(Button button) {
        String id = button.getId();
        return id != null && this.isDynamic(id);
    }
}
