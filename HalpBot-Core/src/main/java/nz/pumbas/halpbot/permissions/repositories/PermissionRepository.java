package nz.pumbas.halpbot.permissions.repositories;

import org.dockbox.hartshorn.core.annotations.stereotype.Service;
import org.dockbox.hartshorn.core.proxy.DelegatorAccessor;
import org.dockbox.hartshorn.data.annotations.Query;
import org.dockbox.hartshorn.data.jpa.JpaRepository;

import java.util.Set;

@Service
public abstract class PermissionRepository implements JpaRepository<GuildPermission, String>, DelegatorAccessor<PermissionRepository>
{
    @Query("SELECT gp.roleId FROM GuildPermission gp WHERE gp.guildId = :guildId AND gp.permission = :permission")
    public abstract long guildRole(long guildId, String permission);

    @Query("SELECT gp.permission FROM GuildPermission gp WHERE gp.guildId = :guildId AND gp.roleId IN :roleIds")
    public abstract Set<String> permissions(long guildId, Set<Long> roleIds);

    @Query("SELECT COUNT(*) FROM GuildPermissions gp WHERE gp.guildId = :guildId AND gp.roleId = :roleId")
    public abstract long countPermissionsWithRole(long guildId, long roleId);

    @Query("SELECT COUNT(*) FROM GuildPermissions gp WHERE gp.guildId = :guildId AND gp.permission = :permission")
    public abstract long countPermissions(long guildId, String permission);

    @Query("INSERT INTO GuildPermissions VALUES (:guildId, :permission, :roleId)")
    public abstract long insertPermission(long guildId, String permission, long roleId);

    @Query("UPDATE GuildPermissions gp SET roleId = :roleId WHERE gp.guildId = :guildId AND gp.permission = :permission")
    public abstract long updatePermission(long guildId, String permission, long roleId);
}
