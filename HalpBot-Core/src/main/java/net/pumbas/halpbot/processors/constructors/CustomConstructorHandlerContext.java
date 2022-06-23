package net.pumbas.halpbot.processors.constructors;

import net.pumbas.halpbot.processors.MultiMapContext;

import org.dockbox.hartshorn.context.AutoCreating;
import org.dockbox.hartshorn.util.reflect.ConstructorContext;
import org.dockbox.hartshorn.util.reflect.TypeContext;

@AutoCreating
public class CustomConstructorHandlerContext extends MultiMapContext<TypeContext<?>, ConstructorContext<?>> {

}
