package net.pumbas.halpbot.actions;

import net.pumbas.halpbot.actions.invokable.HalpbotInvokable;
import net.pumbas.halpbot.actions.invokable.HalpbotSourceInvocationContext;
import net.pumbas.halpbot.actions.invokable.HalpbotSourceInvokable;
import net.pumbas.halpbot.actions.invokable.Invokable;
import net.pumbas.halpbot.actions.invokable.SourceInvocationContext;
import net.pumbas.halpbot.actions.invokable.SourceInvokable;

import org.dockbox.hartshorn.component.Service;
import org.dockbox.hartshorn.component.processing.Provider;

@Service
public class ActionProviders {

    @Provider
    public Class<? extends Invokable> invokable() {
        return HalpbotInvokable.class;
    }

    @Provider
    public Class<? extends SourceInvocationContext> sourceInvocationContext() {
        return HalpbotSourceInvocationContext.class;
    }

    @Provider
    @SuppressWarnings("rawtypes")
    public Class<? extends SourceInvokable> sourceInvokable() {
        return HalpbotSourceInvokable.class;
    }
}
