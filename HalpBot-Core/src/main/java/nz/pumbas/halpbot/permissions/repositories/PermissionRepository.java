package nz.pumbas.halpbot.permissions.repositories;

import org.dockbox.hartshorn.core.annotations.stereotype.Service;
import org.dockbox.hartshorn.core.proxy.DelegatorAccessor;
import org.dockbox.hartshorn.data.annotations.Query;
import org.dockbox.hartshorn.data.jpa.JpaRepository;

import java.util.List;
import java.util.Set;

@Service
public abstract class PermissionRepository implements JpaRepository<GuildPermission, GuildPermissionId>,
        DelegatorAccessor<PermissionRepository>
{
    @Query("SELECT gp.permission FROM GuildPermission gp WHERE gp.guildId = :guildId AND gp.roleId IN :roleIds")
    public abstract Set<String> permissions(long guildId, Set<Long> roleIds);

    @Query("SELECT COUNT(*) FROM GuildPermissions gp WHERE gp.guildId = :guildId AND gp.roleId = :roleId")
    public abstract Long countPermissionsWithRole(long guildId, long roleId);

    @Query("SELECT COUNT(*) FROM GuildPermissions gp WHERE gp.guildId = :guildId AND gp.permission = :permission")
    public abstract Long countPermissions(long guildId, String permission);

    @Query("INSERT INTO GuildPermissions VALUES (:guildId, :permission, :roleId)")
    public abstract Long insertPermission(long guildId, String permission, long roleId);

    @Query("UPDATE GuildPermissions gp SET roleId = :roleId WHERE gp.guildId = :guildId AND gp.permission = :permission")
    public abstract Long updatePermission(long guildId, String permission, long roleId);
}
