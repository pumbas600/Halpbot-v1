package net.pumbas.halpbot.decorators;

import net.pumbas.halpbot.decorators.log.LogDecorator;

import org.dockbox.hartshorn.component.Service;
import org.dockbox.hartshorn.component.processing.Provider;

@Service
public class DecoratorProviders {

    @Provider
    public Class<? extends DecoratorService> decoratorService() {
        return HalpbotDecoratorService.class;
    }

    @Provider
    @SuppressWarnings("rawtypes")
    public Class<? extends LogDecorator> logDecorator() {
        return LogDecorator.class;
    }
}
