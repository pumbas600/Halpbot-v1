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

import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.interactions.components.Button;
import net.pumbas.halpbot.adapters.HalpbotAdapter;

import org.dockbox.hartshorn.util.reflect.MethodContext;
import org.dockbox.hartshorn.util.reflect.TypeContext;
import org.dockbox.hartshorn.util.Result;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public interface ButtonAdapter extends HalpbotAdapter
{
    String DYNAMIC_ID_FORMAT = "%s$$%s$$%d-%d";
    Pattern DYNAMIC_ID_EXTRACTION_PATTERN = Pattern.compile(".*\\$\\$(.*)\\$\\$.*");

    String dynamicPrefix();

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

    @Nullable
    ButtonContext buttonContext(@Nullable String id);

    default Result<ButtonContext> buttonContextSafely(String id) {
        return Result.of(this.buttonContext(id));
    }

    Button register(Button button, Object... parameters);

    Button register(Button button, AfterRemovalFunction afterRemoval, Object... parameters);

    void unregister(String id, boolean applyRemovalFunction);

    default void unregister(String id) {
        this.unregister(id, true);
    }

    default void unregister(Button button, boolean applyRemovalFunction) {
        String id = button.getId();
        if (id != null)
            this.unregister(button.getId(), applyRemovalFunction);
    }

    default void unregister(Button button) {
        this.unregister(button, true);
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
        return DYNAMIC_ID_FORMAT.formatted(this.dynamicPrefix(), currentId, System.currentTimeMillis(), this.idSuffix());
    }

    default Result<String> extractOriginalIdSafely(String dynamicId) {
        String extractedId = this.extractOriginalId(dynamicId);

        if (extractedId == null)
            return Result.of(
                new IllegalArgumentException("The specified id did not match the dynamic id format of '%s'"
                    .formatted(DYNAMIC_ID_FORMAT)));

        return Result.of(extractedId);
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
        return id.startsWith(this.dynamicPrefix());
    }

    default boolean isDynamic(Button button) {
        String id = button.getId();
        return id != null && this.isDynamic(id);
    }
}
