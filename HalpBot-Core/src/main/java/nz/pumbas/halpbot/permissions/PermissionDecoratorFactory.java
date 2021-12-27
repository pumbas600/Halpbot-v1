package nz.pumbas.halpbot.permissions;

import org.dockbox.hartshorn.core.annotations.Factory;
import org.dockbox.hartshorn.core.annotations.stereotype.Service;

import nz.pumbas.halpbot.actions.invokable.ActionInvokable;
import nz.pumbas.halpbot.decorators.ActionInvokableDecoratorFactory;

@Service
public interface PermissionDecoratorFactory extends ActionInvokableDecoratorFactory<PermissionDecorator<?>, Permission>
{
    @Factory
    @Override
    PermissionDecorator<?> decorate(ActionInvokable<?> element, Permission annotation);
}
