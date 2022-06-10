package net.pumbas.halpbot.processors.triggers;

import net.pumbas.halpbot.processors.MultiMapContext;

import org.dockbox.hartshorn.context.AutoCreating;
import org.dockbox.hartshorn.util.reflect.MethodContext;
import org.dockbox.hartshorn.util.reflect.TypeContext;

@AutoCreating
public class TriggerHandlerContext extends MultiMapContext<TypeContext<?>, MethodContext<?, ?>> {

}
