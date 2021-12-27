package nz.pumbas.halpbot.permissions;

import org.dockbox.hartshorn.core.annotations.service.Service;

import nz.pumbas.halpbot.actions.invokable.ActionInvokable;
import nz.pumbas.halpbot.decorators.ActionInvokableDecoratorFactory;
import nz.pumbas.halpbot.permissions.Permission;
import nz.pumbas.halpbot.permissions.PermissionDecorator;

@Service
public interface PermissionDecoratorFactory extends ActionInvokableDecoratorFactory<PermissionDecorator<?>, Permission>
{
    //@Factory
    @Override
    PermissionDecorator<?> decorate(ActionInvokable<?> element, Permission annotation);
}
