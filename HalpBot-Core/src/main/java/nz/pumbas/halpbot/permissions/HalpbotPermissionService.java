package nz.pumbas.halpbot.permissions;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;

import org.dockbox.hartshorn.core.Enableable;
import org.dockbox.hartshorn.core.annotations.inject.Binds;
import org.dockbox.hartshorn.core.annotations.inject.Provider;
import org.dockbox.hartshorn.core.annotations.stereotype.Service;
import org.dockbox.hartshorn.core.context.ApplicationContext;
import org.dockbox.hartshorn.core.context.element.MethodContext;
import org.dockbox.hartshorn.core.domain.Exceptional;
import org.dockbox.hartshorn.core.exceptions.ApplicationException;
import org.dockbox.hartshorn.data.remote.DerbyFileRemote;
import org.dockbox.hartshorn.data.remote.PersistenceConnection;

import java.io.File;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;

import lombok.Getter;
import nz.pumbas.halpbot.actions.methods.Invokable;
import nz.pumbas.halpbot.actions.methods.InvokableFactory;
import nz.pumbas.halpbot.configurations.BotConfiguration;
import nz.pumbas.halpbot.permissions.repositories.GuildPermission;
import nz.pumbas.halpbot.permissions.repositories.GuildPermissionId;
import nz.pumbas.halpbot.permissions.repositories.PermissionRepository;

@Service
@Binds(PermissionService.class)
public class HalpbotPermissionService implements PermissionService, Enableable
{
    @Getter
    @Inject private ApplicationContext applicationContext;
    @Inject private InvokableFactory invokableFactory;
    @Inject private PermissionRepository permissionRepository;

    private final Set<String> permissions = new HashSet<>();
    private final Map<String, Invokable> permissionSuppliers = new HashMap<>();

    @Getter private boolean useRoleBinding;

    @Override
    public void enable() {
        this.useRoleBinding = this.applicationContext.get(BotConfiguration.class).useRoleBinding();

        Path path = new File("Halpbot-Core-DB").toPath();
        PersistenceConnection connection = DerbyFileRemote.INSTANCE.connection(path, "root", "");
        this.applicationContext.log().info("Role binding database connection created");
        this.applicationContext.get(PermissionRepository.class).connection(connection);
    }

    @Override
    public void initialise() {
        if(!this.useRoleBinding && !this.rolePermissions().isEmpty())
            this.applicationContext.log()
                    .error("You haven't enabled custom permissions in the bot-config but you have some defined.");
        if (this.useRoleBinding)
            this.deleteOldPermissions();
    }

    private void deleteOldPermissions() {
        List<GuildPermission> oldPermissions =  this.permissionRepository.findAll()
                .stream()
                .filter((gp) -> !this.isRegistered(gp.permission()))
                .toList();
        if (!oldPermissions.isEmpty()) {
            this.applicationContext.log().info("Deleting the following deprecated permissions from the database: %s"
                    .formatted(String.join(", ", oldPermissions.stream().map(GuildPermission::permission).toList())));
            oldPermissions.forEach(this::delete);
        }
    }

    @Override
    public boolean isRegistered(String permission) {
        return this.permissions.contains(permission);
    }

    @Override
    public boolean isRoleBound(long guildId, long roleId) {
        if (!this.useRoleBinding)
            return false;
        return this.permissionRepository.countPermissionsWithRole(guildId, roleId) != 0;
    }

    @Override
    public boolean isPermissionBound(long guildId, String permission) {
        if (!this.useRoleBinding)
            return false;
        return this.permissionRepository.countPermissions(guildId, permission) != 0;
    }

    @Override
    public <T> void registerPermissionSupplier(T instance, String permission, MethodContext<Boolean, T> predicate) {
        if (this.permissionSuppliers.containsKey(permission))
            this.applicationContext.log().warn("There is already a supplier for the permission %s".formatted(permission));
        else {
            Invokable invokable = this.invokableFactory.create(instance, predicate);
            this.permissionSuppliers.put(permission, invokable);
        }
    }

    @Override
    public GuildPermission updateOrSave(GuildPermission guildPermission) {
        if (!this.useRoleBinding)
            return guildPermission;
        return this.permissionRepository.updateOrSave(guildPermission);
    }

    @Override
    public GuildPermission update(GuildPermission guildPermission) {
        if (!this.useRoleBinding)
            return guildPermission;
        return this.permissionRepository.update(guildPermission);
    }

    @Override
    public GuildPermission save(GuildPermission guildPermission) {
        if (!this.useRoleBinding)
            return guildPermission;
        return this.permissionRepository.save(guildPermission);
    }

    @Override
    public Exceptional<GuildPermission> findById(GuildPermissionId id) {
        if (!this.useRoleBinding)
            return Exceptional.empty();
        return this.permissionRepository.findById(id);
    }

    @Override
    public void delete(GuildPermission guildPermission) {
        if (!this.useRoleBinding)
            return;
        this.permissionRepository.delete(guildPermission);
    }

    @Override
    public void close() {
        if (!this.useRoleBinding)
            return;
        this.permissionRepository.entityManager().close();
    }

    @Override
    public void addPermissions(Set<String> permissions) {
        this.permissions.addAll(permissions);
    }

    private boolean evaluatePermissionSupplier(String permission, Guild guild, Member member) {
        return Boolean.TRUE.equals(this.permissionSuppliers.get(permission)
                .invoke(guild, member)
                .or(false));
    }

    @Override
    public boolean hasPermission(Guild guild, Member member, String permission) {
        if (this.permissionSuppliers.containsKey(permission))
            return this.evaluatePermissionSupplier(permission, guild, member);

        if (!this.useRoleBinding)
            return false;
        return Boolean.TRUE.equals(
                this.permissionRepository.findById(new GuildPermissionId(guild.getIdLong(), permission))
                        .map((gp) -> member.getRoles().contains(guild.getRoleById(gp.roleId())))
                        .or(false)
        );
    }

    @Override
    public Set<String> permissions(long guildId, Member member) {
        if (!this.useRoleBinding)
            Collections.emptySet();
        return this.permissionRepository
                .permissions(guildId, member.getRoles()
                        .stream()
                        .map(Role::getIdLong)
                        .collect(Collectors.toSet())
        );
    }

    @Override
    public Set<String> permissions() {
        return Collections.unmodifiableSet(this.permissions);
    }

    @Override
    public Set<String> rolePermissions() {
        return this.permissions
                .stream()
                .filter((permission) -> !this.permissionSuppliers.containsKey(permission))
                .collect(Collectors.toSet());
    }

    @Override
    public Map<String, Long> roleBindings(Guild guild) {
        if (!this.useRoleBinding)
            return Collections.emptyMap();

        Map<String, Long> bindings = new HashMap<>();
        this.permissionRepository.guildPermissions(guild.getIdLong())
                .forEach((gp) -> bindings.put(gp.permission(), gp.roleId()));

        this.rolePermissions().forEach((permission) -> bindings.putIfAbsent(permission, null));
        return bindings;
    }
}
