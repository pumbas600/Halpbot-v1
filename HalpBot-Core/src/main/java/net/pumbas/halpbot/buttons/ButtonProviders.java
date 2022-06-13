package net.pumbas.halpbot.buttons;

import org.dockbox.hartshorn.component.Service;
import org.dockbox.hartshorn.component.condition.RequiresActivator;
import org.dockbox.hartshorn.component.processing.Provider;

import jakarta.inject.Singleton;

@Service
@RequiresActivator(UseButtons.class)
public class ButtonProviders {

    @Provider
    @Singleton
    public Class<? extends ButtonAdapter> buttonAdapter() {
        return HalpbotButtonAdapter.class;
    }

    @Provider
    public Class<? extends ButtonContext> buttonContext() {
        return HalpbotButtonContext.class;
    }

    @Provider
    public Class<? extends ButtonInvocationContext> buttonInvocationContext() {
        return HalpbotButtonInvocationContext.class;
    }

    @Provider
    public Class<? extends ButtonInvokable> buttonInvokable() {
        return HalpbotButtonInvokable.class;
    }
}
