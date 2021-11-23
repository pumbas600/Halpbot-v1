package nz.pumbas.halpbot.permissions;

import net.dv8tion.jda.api.entities.User;

import java.util.List;

public interface Permissive
{
    List<String> permissions();

    default boolean hasPermission(PermissionManager permissionManager, User user) {
        return this.hasPermission(permissionManager, user.getIdLong());
    }

    default boolean hasPermission(PermissionManager permissionManager, long userId) {
        return this.permissions().isEmpty() ||
            permissionManager.hasPermissions(userId, this.permissions());
    }
}
