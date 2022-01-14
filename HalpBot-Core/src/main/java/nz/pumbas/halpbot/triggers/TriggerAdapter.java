package nz.pumbas.halpbot.triggers;

import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import org.dockbox.hartshorn.core.context.element.MethodContext;
import org.dockbox.hartshorn.core.context.element.TypeContext;

import nz.pumbas.halpbot.adapters.HalpbotAdapter;

public interface TriggerAdapter extends HalpbotAdapter
{
    @Override
    default void onEvent(GenericEvent event) {
        if (event instanceof MessageReceivedEvent messageReceivedEvent)
            this.onMessageReceived(messageReceivedEvent);
    }

    void onMessageReceived(MessageReceivedEvent event);

    default <T> void registerTriggers(TypeContext<T> type) {
        T instance = this.applicationContext().get(type);
        int triggers = 0;

        for (MethodContext<?, T> trigger : type.methods(Trigger.class)) {
            if (!trigger.isPublic()) {
                this.applicationContext().log().warn("Methods annotated with @Trigger must be public");
                continue;
            }
            this.registerTrigger(instance, trigger);
            triggers++;
        }
        this.applicationContext().log().info("Registered %d triggers found in %s".formatted(triggers, type.qualifiedName()));
    }

    <T> void registerTrigger(T instance, MethodContext<?, T> methodContext);


}
