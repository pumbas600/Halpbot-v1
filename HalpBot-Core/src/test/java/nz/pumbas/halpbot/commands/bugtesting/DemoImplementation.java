package nz.pumbas.halpbot.commands.bugtesting;

import org.dockbox.hartshorn.core.annotations.inject.Binds;
import org.dockbox.hartshorn.core.annotations.inject.Bound;

import javax.inject.Inject;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import nz.pumbas.halpbot.permissions.PermissionService;

@Getter
@Binds(DemoInterface.class)
@RequiredArgsConstructor(onConstructor_ = @Bound)
public class DemoImplementation implements DemoInterface
{
    private final String name;
    @Inject private PermissionService permissionService;
}
