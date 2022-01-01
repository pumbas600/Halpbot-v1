package nz.pumbas.halpbot.permissions;

import net.dv8tion.jda.api.entities.User;

import java.util.Set;

public interface Permissive
{
    Set<String> permissions();

    default boolean hasPermission(PermissionService permissionService, User user) {
        return this.hasPermission(permissionService, user.getIdLong());
    }

    default boolean hasPermission(PermissionService permissionService, long userId) {
        return this.permissions().isEmpty() ||
            permissionService.hasPermission(userId, this.permissions());
    }
}
