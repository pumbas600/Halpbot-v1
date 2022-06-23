package net.pumbas.halpbot.processors;

import org.dockbox.hartshorn.context.DefaultContext;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ListContext<T> extends DefaultContext {

    private final List<T> registeredContexts = new ArrayList<>();

    /**
     * Registers the context so that it can be processed at a later point.
     *
     * @param context
     *     The context to register
     */
    public void register(final T context) {
        this.registeredContexts.add(context);
    }

    /**
     * @return An unmodifiable list of the registered contexts
     */
    public List<T> registeredContexts() {
        return Collections.unmodifiableList(this.registeredContexts);
    }

    /**
     * Clears all the registered contexts
     */
    public void clear() {
        this.registeredContexts.clear();
    }
}
