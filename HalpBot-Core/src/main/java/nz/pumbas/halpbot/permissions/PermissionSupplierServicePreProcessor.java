package nz.pumbas.halpbot.permissions;

import org.dockbox.hartshorn.core.Key;
import org.dockbox.hartshorn.core.annotations.activate.AutomaticActivation;
import org.dockbox.hartshorn.core.context.ApplicationContext;
import org.dockbox.hartshorn.core.services.ServicePreProcessor;

import nz.pumbas.halpbot.commands.annotations.UseCommands;

@AutomaticActivation
public class PermissionSupplierServicePreProcessor implements ServicePreProcessor<UseCommands>
{
    @Override
    public boolean preconditions(ApplicationContext context, Key<?> key) {
        return !key.type().methods(PermissionSupplier.class).isEmpty();
    }

    @Override
    public <T> void process(ApplicationContext context, Key<T> key) {
        final PermissionService permissionService = context.get(PermissionService.class);
        permissionService.registerPermissionSuppliers(key.type());
    }

    @Override
    public Class<UseCommands> activator() {
        return UseCommands.class;
    }
}
