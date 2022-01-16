package nz.pumbas.halpbot.commands.factory;

import org.dockbox.hartshorn.core.annotations.inject.ComponentBinding;
import org.dockbox.hartshorn.core.annotations.inject.Bound;

import javax.inject.Inject;
import javax.inject.Singleton;

import lombok.Getter;
import nz.pumbas.halpbot.permissions.PermissionService;

@Singleton
@ComponentBinding(DemoServiceA.class)
public class DemoServiceA<T>
{
    @Getter
    @Inject private PermissionService permissionService;

    @Bound
    public DemoServiceA() {

    }
}
