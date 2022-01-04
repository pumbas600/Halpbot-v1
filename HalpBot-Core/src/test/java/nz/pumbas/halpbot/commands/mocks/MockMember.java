package nz.pumbas.halpbot.commands.mocks;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.internal.entities.GuildImpl;
import net.dv8tion.jda.internal.entities.MemberImpl;

import org.jetbrains.annotations.NotNull;

import java.util.Set;

public class MockMember extends MemberImpl
{
    private final Set<Permission> permissions;

    public MockMember(GuildImpl guild, User user, Permission... permissions) {
        super(guild, user);
        this.permissions = Set.of(permissions);
    }

    @Override
    public boolean hasPermission(@NotNull Permission... permissions) {
        for (Permission permission : permissions) {
            if (!this.permissions.contains(permission))
                return false;
        }
        return true;
    }
}
