package nz.pumbas.halpbot.adapters;

import net.dv8tion.jda.api.JDA;

import org.dockbox.hartshorn.core.Enableable;
import org.dockbox.hartshorn.core.context.ContextCarrier;
import org.dockbox.hartshorn.core.context.element.TypeContext;
import org.dockbox.hartshorn.core.exceptions.ApplicationException;

import java.lang.annotation.Annotation;
import java.util.Comparator;
import java.util.List;

import nz.pumbas.halpbot.actions.invokable.ActionInvokable;
import nz.pumbas.halpbot.actions.invokable.InvocationContext;
import nz.pumbas.halpbot.decorators.ActionInvokableDecoratorFactory;
import nz.pumbas.halpbot.decorators.Decorator;
import nz.pumbas.halpbot.decorators.DecoratorFactory;

public interface HalpbotAdapter extends ContextCarrier, Enableable
{
    HalpbotCore halpbotCore();

    default void onCreation(JDA jda) {}

    @Override
    default void enable() throws ApplicationException {
        this.halpbotCore().registerAdapter(this);
    }
}
