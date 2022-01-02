package nz.pumbas.halpbot.commands.bugtesting;

import nz.pumbas.halpbot.permissions.PermissionService;

public interface DemoInterface<T>
{
    String name();

    PermissionService permissionService();
}
