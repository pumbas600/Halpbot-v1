package net.pumbas.halpbot.processors.buttons;

import net.pumbas.halpbot.buttons.ButtonHandler;

import org.dockbox.hartshorn.context.AutoCreating;
import org.dockbox.hartshorn.context.DefaultContext;
import org.dockbox.hartshorn.util.ArrayListMultiMap;
import org.dockbox.hartshorn.util.MultiMap;
import org.dockbox.hartshorn.util.reflect.MethodContext;
import org.dockbox.hartshorn.util.reflect.TypeContext;

import lombok.Getter;

@AutoCreating
public class ButtonHandlerContext extends DefaultContext {

    @Getter
    private final MultiMap<TypeContext<?>, MethodContext<?, ?>> buttonHandlers = new ArrayListMultiMap<>();

    /**
     * Registers a {@code ButtonHandler} method so that it can be processed by the {@code ButtonAdapter}. Note that the
     * button handler should be validated prior to being registered.
     *
     * @param type
     *     The class that the button handler is defined within
     * @param buttonHandler
     *     The button handler method to be registered
     * @param <T>
     *     The type of the class the button handler is defined within
     *
     * @see ButtonHandler
     * @see net.pumbas.halpbot.buttons.ButtonAdapter
     */
    public <T> void register(final TypeContext<T> type, final MethodContext<?, T> buttonHandler) {
        this.buttonHandlers.put(type, buttonHandler);
    }
}
