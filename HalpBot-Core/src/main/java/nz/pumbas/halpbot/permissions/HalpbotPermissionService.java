package nz.pumbas.halpbot.permissions;

import org.dockbox.hartshorn.core.annotations.inject.Binds;
import org.dockbox.hartshorn.core.annotations.stereotype.Service;

import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import nz.pumbas.halpbot.permissions.repositories.PermissionRepository;

@Service
@Binds(PermissionService.class)
public class HalpbotPermissionService implements PermissionService
{
    @Inject
    private PermissionRepository permissionRepository;

    @Override
    public boolean isPermission(String permission) {
        return false;
    }

    @Override
    public void givePermission(long userId, String permission) {

    }

    @Override
    public void createPermissions(List<String> permissions) {

    }

    @Override
    public boolean hasPermissions(long userId, List<String> permissions) {
        return true;
    }

    @Override
    public List<String> getPermissions(long userId) {
        return Collections.emptyList();
    }

    @Override
    public List<String> getAllPermissions() {
        return Collections.emptyList();
    }
}
