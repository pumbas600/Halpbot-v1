package net.pumbas.halpbot.processors.eventlisteners;

import net.dv8tion.jda.api.hooks.EventListener;
import net.pumbas.halpbot.processors.ListContext;

import org.dockbox.hartshorn.context.AutoCreating;
import org.dockbox.hartshorn.util.reflect.TypeContext;

@AutoCreating
public class EventListenerContext extends ListContext<TypeContext<? extends EventListener>> {

}
