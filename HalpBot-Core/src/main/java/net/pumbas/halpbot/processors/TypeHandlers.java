package net.pumbas.halpbot.processors;

import org.dockbox.hartshorn.util.reflect.MethodContext;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TypeHandlers<T> {

    private final List<MethodContext<?, T>> handlers = new ArrayList<>();

    /**
     * Adds a handler to the list of handlers that are declared within this class
     *
     * @param handler
     *     The handler to add to the list of handlers
     */
    public void addHandler(final MethodContext<?, T> handler) {
        this.handlers.add(handler);
    }

    /**
     * @return An unmodifiable list of the handlers declared within this class
     */
    public List<MethodContext<?, T>> handlers() {
        return Collections.unmodifiableList(this.handlers);
    }
}
