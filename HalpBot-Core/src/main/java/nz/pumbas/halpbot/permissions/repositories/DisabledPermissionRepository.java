package nz.pumbas.halpbot.permissions.repositories;

import org.dockbox.hartshorn.core.annotations.inject.Binds;
import org.dockbox.hartshorn.core.annotations.stereotype.Service;
import org.dockbox.hartshorn.core.context.ApplicationContext;
import org.dockbox.hartshorn.core.domain.Exceptional;
import org.dockbox.hartshorn.data.jpa.JpaRepository;
import org.dockbox.hartshorn.data.remote.PersistenceConnection;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.persistence.EntityManager;

public class DisabledPermissionRepository extends PermissionRepository
{
    private UnsupportedOperationException disabledError() {
        return new UnsupportedOperationException(
                "The custom permissions database has been disabled. To use it, set the property " +
                        "useCustomPermissions to true in the bot-config");
    }

    @Override
    public List<GuildPermission> guildPermissions(long guildId) {
        return Collections.emptyList();
    }

    @Override
    public Set<String> permissions(long guildId, Set<Long> roleIds) {
        return Collections.emptySet();
    }

    @Override
    public Long countPermissionsWithRole(long guildId, long roleId) {
        return 0L;
    }

    @Override
    public Long countPermissions(long guildId, String permission) {
        return 0L;
    }

    @Override
    public GuildPermission save(GuildPermission object) {
        throw this.disabledError();
    }

    @Override
    public GuildPermission update(GuildPermission object) {
        throw this.disabledError();
    }

    @Override
    public GuildPermission updateOrSave(GuildPermission object) {
        throw this.disabledError();
    }

    @Override
    public void delete(GuildPermission object) {
    }

    @Override
    public Set<GuildPermission> findAll() {
        return Collections.emptySet();
    }

    @Override
    public Exceptional<GuildPermission> findById(GuildPermissionId guildPermissionId) {
        return Exceptional.empty();
    }

    @Override
    public EntityManager entityManager() {
        throw this.disabledError();
    }

    @Override
    public Class<GuildPermission> reify() {
        return GuildPermission.class;
    }

    @Override
    public void flush() {
    }

    @Override
    public JpaRepository<GuildPermission, GuildPermissionId> connection(PersistenceConnection connection) {
        return this;
    }

    @Override
    public ApplicationContext applicationContext() {
        throw this.disabledError();
    }
}
