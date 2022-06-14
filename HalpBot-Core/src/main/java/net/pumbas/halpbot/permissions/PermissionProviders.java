package net.pumbas.halpbot.permissions;

import org.dockbox.hartshorn.component.Service;
import org.dockbox.hartshorn.component.condition.RequiresActivator;
import org.dockbox.hartshorn.component.processing.Provider;
import org.dockbox.hartshorn.data.annotations.UsePersistence;

@Service
@RequiresActivator(UsePersistence.class)
public class PermissionProviders {

    @Provider
    public Class<? extends PermissionService> permissionService() {
        return HalpbotPermissionService.class;
    }

    @Provider
    @SuppressWarnings("rawtypes")
    public Class<? extends PermissionDecorator> permissionDecorator() {
        return PermissionDecorator.class;
    }
}
