package net.pumbas.halpbot.permissions;

import net.pumbas.halpbot.configurations.BotConfiguration;

import org.dockbox.hartshorn.component.Service;
import org.dockbox.hartshorn.component.condition.RequiresActivator;
import org.dockbox.hartshorn.component.processing.Provider;

@Service
@RequiresActivator(UsePermissions.class)
public class PermissionProviders {

    @Provider
    public Class<? extends PermissionService> permissionService(final BotConfiguration configuration) {
        if (configuration.useRoleBinding())
            return HalpbotBindingPermissionService.class;
        return HalpbotPermissionService.class;
    }

    @Provider
    @SuppressWarnings("rawtypes")
    public Class<? extends PermissionDecorator> permissionDecorator() {
        return PermissionDecorator.class;
    }
}
