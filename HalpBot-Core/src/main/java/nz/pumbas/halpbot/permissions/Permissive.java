package nz.pumbas.halpbot.permissions;

import net.dv8tion.jda.api.entities.User;

import org.dockbox.hartshorn.core.context.ContextCarrier;

import java.util.List;

public interface Permissive extends ContextCarrier
{
    List<String> permissions();

    default boolean hasPermission(User user) {
        return this.hasPermission(user.getIdLong());
    }

    default boolean hasPermission(long userId) {
        return this.permissions().isEmpty() ||
            this.applicationContext()
                .get(PermissionManager.class)
                .hasPermissions(userId, this.permissions());
    }
}
