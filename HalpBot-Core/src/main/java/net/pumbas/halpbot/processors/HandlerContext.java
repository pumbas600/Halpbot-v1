package net.pumbas.halpbot.processors;

import org.dockbox.hartshorn.context.AutoCreating;
import org.dockbox.hartshorn.context.DefaultContext;
import org.dockbox.hartshorn.util.reflect.MethodContext;
import org.dockbox.hartshorn.util.reflect.TypeContext;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@AutoCreating
public class HandlerContext extends DefaultContext {

    private final Map<TypeContext<?>, TypeHandlers<?>> typeHandlers = new ConcurrentHashMap<>();

    @SuppressWarnings("unchecked")
    public <T> void register(final TypeContext<T> type, final MethodContext<?, T> handler) {
        if (!this.typeHandlers.containsKey(type)) {
            this.typeHandlers.put(type, new TypeHandlers<T>());
        }
        final TypeHandlers<T> typeHandler = (TypeHandlers<T>) this.typeHandlers.get(type);
        typeHandler.addHandler(handler);
    }

    /**
     * Gets an unmodifiable list of handlers registered for the specified type. If the type is not registered than an
     * empty list is returned.
     *
     * @param type
     *     The type to get the handlers for
     * @param <T>
     *     The type of the class the handlers are defined within
     *
     * @return An unmodifiable list of the registered handlers
     */
    @SuppressWarnings("unchecked")
    public <T> List<MethodContext<?, T>> handlers(final TypeContext<T> type) {
        if (!this.typeHandlers.containsKey(type)) {
            return Collections.emptyList();
        }

        final TypeHandlers<T> typeHandler = (TypeHandlers<T>) this.typeHandlers.get(type).handlers();
        return typeHandler.handlers();
    }

    /**
     * Clears the registered type handlers
     */
    public void clear() {
        this.typeHandlers.clear();
    }
}
