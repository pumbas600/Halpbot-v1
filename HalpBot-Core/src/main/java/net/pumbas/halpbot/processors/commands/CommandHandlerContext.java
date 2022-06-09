package net.pumbas.halpbot.processors.commands;

import net.pumbas.halpbot.processors.MultiMapContext;

import org.dockbox.hartshorn.context.AutoCreating;
import org.dockbox.hartshorn.util.reflect.MethodContext;
import org.dockbox.hartshorn.util.reflect.TypeContext;

@AutoCreating
public class CommandHandlerContext extends MultiMapContext<TypeContext<?>, MethodContext<?, ?>> {

}
