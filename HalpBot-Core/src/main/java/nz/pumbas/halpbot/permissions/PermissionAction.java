package nz.pumbas.halpbot.permissions;

import net.dv8tion.jda.api.entities.User;

import org.jetbrains.annotations.Nullable;

import java.util.List;

import nz.pumbas.halpbot.utilities.HalpbotUtils;

public interface PermissionAction
{
    List<String> getPermissions();

    default boolean hasPermission(@Nullable User user) {
        if (null == user)
            return this.getPermissions().isEmpty();
        return this.hasPermission(user.getIdLong());
    }

    default boolean hasPermission(long userId) {
        return this.getPermissions().isEmpty() ||
            HalpbotUtils.context()
            .get(PermissionManager.class)
            .hasPermissions(userId, this.getPermissions());
    }
}
