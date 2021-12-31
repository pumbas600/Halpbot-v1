package nz.pumbas.halpbot.permissions.repositories;

import org.dockbox.hartshorn.core.annotations.stereotype.Service;
import org.dockbox.hartshorn.core.proxy.DelegatorAccessor;
import org.dockbox.hartshorn.data.annotations.Query;
import org.dockbox.hartshorn.data.jpa.JpaRepository;

@Service
public abstract class PermissionRepository implements JpaRepository<GuildPermission, String>, DelegatorAccessor<PermissionRepository>
{
    @Query("SELECT gp.roleId FROM GuildPermission gp WHERE gp.guildId = :guildId AND gp.permission = :permission")
    public abstract String guildRole(String guildId, String permission);
}
