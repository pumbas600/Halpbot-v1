package nz.pumbas.halpbot.commands.factory;

import org.dockbox.hartshorn.core.annotations.inject.Binds;
import org.dockbox.hartshorn.core.annotations.inject.Bound;
import org.dockbox.hartshorn.core.annotations.stereotype.Service;

import javax.inject.Inject;

import lombok.Getter;
import nz.pumbas.halpbot.permissions.PermissionService;

@Service
@Binds(DemoServiceA.class)
public class DemoServiceA<T>
{
    @Getter
    @Inject private PermissionService permissionService;

    @Bound
    public DemoServiceA() {

    }
}
