package net.pumbas.halpbot;

import net.pumbas.halpbot.decorators.time.TimeDecorator;

import org.dockbox.hartshorn.component.Service;
import org.dockbox.hartshorn.component.processing.Provider;

@Service
public class ImplementationProviders {

    @Provider
    @SuppressWarnings("rawtypes")
    public Class<? extends TimeDecorator> timeDecorator() {
        return TimeDecorator.class;
    }
}
