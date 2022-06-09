package net.pumbas.halpbot.processors;

import net.dv8tion.jda.api.hooks.EventListener;

import org.dockbox.hartshorn.context.AutoCreating;
import org.dockbox.hartshorn.context.DefaultContext;
import org.dockbox.hartshorn.inject.Key;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@AutoCreating
public class EventListenerContext extends DefaultContext {

    private final List<Key<? extends EventListener>> eventListeners = new ArrayList<>();

    /**
     * Registers the {@code EventListener} so that it can be automatically added as an {@code EventListener} for JDA.
     *
     * @param eventListener
     *     The key containing the class that implements {@code EventListener}
     */
    public void register(final Key<? extends EventListener> eventListener) {
        this.eventListeners.add(eventListener);
    }

    /**
     * @return An unmodifiable list of the registered {@code EventListeners}
     */
    public List<Key<? extends EventListener>> eventListeners() {
        return Collections.unmodifiableList(this.eventListeners);
    }
}
