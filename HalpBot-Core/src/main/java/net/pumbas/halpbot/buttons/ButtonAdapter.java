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
import net.pumbas.halpbot.processors.HandlerContext;
import net.pumbas.halpbot.processors.buttons.ButtonHandlerContext;

import org.dockbox.hartshorn.component.Enableable;
import org.dockbox.hartshorn.util.Result;
import org.dockbox.hartshorn.util.reflect.MethodContext;
import org.dockbox.hartshorn.util.reflect.TypeContext;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public interface ButtonAdapter extends HalpbotAdapter, Enableable {

    String DYNAMIC_ID_FORMAT = "%s$$%s$$%d-%d";
    Pattern DYNAMIC_ID_EXTRACTION_PATTERN = Pattern.compile(".*\\$\\$(.*)\\$\\$.*");

    @Override
    default void onEvent(final GenericEvent event) {
        if (event instanceof ButtonClickEvent buttonClickEvent)
            this.onButtonClick(buttonClickEvent);
    }

    void onButtonClick(ButtonClickEvent event);

    @Override
    default void enable() {
        final ButtonHandlerContext buttonHandlerContext =
            this.applicationContext().first(ButtonHandlerContext.class).get();

        for (final TypeContext<?> type : buttonHandlerContext.types()) {
            this.registerButtonHandler(type, buttonHandlerContext);
        }
    }

    private <T> void registerButtonHandler(final TypeContext<T> type, final HandlerContext handlerContext) {
        final T instance = this.applicationContext().get(type);
        final List<MethodContext<?, T>> handlers = handlerContext.handlers(type);
        for (final MethodContext<?, T> handler : handlers) {
            this.registerButton(instance, handler);
        }

        this.applicationContext().log().info("Registered {} button handlers for {}", handlers.size(), type.qualifiedName());
    }

    <T> void registerButton(T instance, MethodContext<?, T> buttonMethodContext);

    default Result<ButtonContext> buttonContextSafely(final String id) {
        return Result.of(this.buttonContext(id));
    }

    @Nullable
    ButtonContext buttonContext(@Nullable String id);

    Button register(Button button, AfterRemovalFunction afterRemoval, Object... parameters);

    default void unregister(final String id) {
        this.unregister(id, true);
    }

    void unregister(String id, boolean applyRemovalFunction);

    default void unregister(final Button button) {
        this.unregister(button, true);
    }

    default void unregister(final Button button, final boolean applyRemovalFunction) {
        final String id = button.getId();
        if (id != null)
            this.unregister(button.getId(), applyRemovalFunction);
    }

    default List<Button> register(final List<Button> buttons, final Object... parameters) {
        return buttons.stream()
            .map((button) -> this.register(button, parameters))
            .toList();
    }

    Button register(Button button, Object... parameters);

    default String generateDynamicId(final String currentId) {
        // The id suffix prevents buttons registered within the same millisecond having the same id. 1000 is used so
        // that it won't take up any more than 3 characters
        this.idSuffix((this.idSuffix() + 1) % 1000);
        return DYNAMIC_ID_FORMAT.formatted(this.dynamicPrefix(), currentId, System.currentTimeMillis(), this.idSuffix());
    }

    void idSuffix(int idSuffix);

    int idSuffix();

    String dynamicPrefix();

    default Result<String> extractOriginalIdSafely(final String dynamicId) {
        final String extractedId = this.extractOriginalId(dynamicId);

        if (extractedId == null)
            return Result.of(
                new IllegalArgumentException("The specified id did not match the dynamic id format of '%s'"
                    .formatted(DYNAMIC_ID_FORMAT)));

        return Result.of(extractedId);
    }

    @Nullable
    default String extractOriginalId(final String dynamicId) {
        final Matcher matcher = DYNAMIC_ID_EXTRACTION_PATTERN.matcher(dynamicId);
        if (matcher.matches()) {
            return matcher.group(1);
        }
        return null;
    }

    default boolean isDynamic(final Button button) {
        final String id = button.getId();
        return id != null && this.isDynamic(id);
    }

    default boolean isDynamic(final String id) {
        return id.startsWith(this.dynamicPrefix());
    }
}
