package nz.pumbas.halpbot.permissions;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;

import org.dockbox.hartshorn.core.annotations.inject.Binds;
import org.dockbox.hartshorn.core.annotations.stereotype.Service;
import org.dockbox.hartshorn.core.context.ApplicationContext;
import org.dockbox.hartshorn.core.context.element.MethodContext;
import org.dockbox.hartshorn.core.domain.Exceptional;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Inject;

import lombok.Getter;
import nz.pumbas.halpbot.actions.methods.Invokable;
import nz.pumbas.halpbot.actions.methods.InvokableFactory;
import nz.pumbas.halpbot.configurations.BotConfiguration;
import nz.pumbas.halpbot.permissions.repositories.GuildPermission;
import nz.pumbas.halpbot.permissions.repositories.GuildPermissionId;
import nz.pumbas.halpbot.permissions.repositories.PermissionRepository;

@Service
@Binds(PermissionService.class)
public class HalpbotPermissionService implements PermissionService
{
    @Getter
    @Inject private ApplicationContext applicationContext;
    @Inject private InvokableFactory invokableFactory;
    @Inject private PermissionRepository permissionRepository;

    private final Set<String> permissions = new HashSet<>();
    private final Map<String, Invokable> permissionSuppliers = new HashMap<>();

    @Getter private boolean useCustomPermissions;

    @Override
    public void initialise() {
        this.useCustomPermissions = this.applicationContext.get(BotConfiguration.class).useRoleBinding();

        if(!this.useCustomPermissions && !this.rolePermissions().isEmpty())
            this.applicationContext.log()
                    .error("You haven't enabled custom permissions in the bot-config but you have some defined.");
        if (this.useCustomPermissions)
            this.deleteOldPermissions();
    }

    private void deleteOldPermissions() {
        List<GuildPermission> oldPermissions =  this.permissionRepository.findAll()
                .stream()
                .filter((gp) -> !this.isPermission(gp.permission()))
                .toList();
        if (!oldPermissions.isEmpty()) {
            this.applicationContext.log().info("Deleting the following deprecated permissions from the database: %s"
                    .formatted(String.join(", ", oldPermissions.stream().map(GuildPermission::permission).toList())));
            oldPermissions.forEach(this.permissionRepository::delete);
        }
    }

    private UnsupportedOperationException disabledError() {
        return new UnsupportedOperationException(
                "The custom permissions database has been disabled. To use it, set the property " +
                        "useCustomPermissions to true in the bot-config");
    }

    @Override
    public boolean isPermission(String permission) {
        return this.permissions.contains(permission);
    }

    @Override
    public boolean isPermission(long guildId, long roleId) {
        if (!this.useCustomPermissions)
            throw this.disabledError();
        return this.permissionRepository.countPermissionsWithRole(guildId, roleId) != 0;
    }

    @Override
    public boolean permissionExists(long guildId, String permission) {
        if (!this.useCustomPermissions)
            throw this.disabledError();
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
        if (!this.useCustomPermissions)
            throw this.disabledError();
        return this.permissionRepository.updateOrSave(guildPermission);
    }

    @Override
    public GuildPermission update(GuildPermission guildPermission) {
        if (!this.useCustomPermissions)
            throw this.disabledError();
        return this.permissionRepository.update(guildPermission);
    }

    @Override
    public GuildPermission save(GuildPermission guildPermission) {
        if (!this.useCustomPermissions)
            throw this.disabledError();
        return this.permissionRepository.save(guildPermission);
    }

    @Override
    public Exceptional<GuildPermission> findById(GuildPermissionId id) {
        if (!this.useCustomPermissions)
            throw this.disabledError();
        return this.permissionRepository.findById(id);
    }

    @Override
    public void close() {
        if (!this.useCustomPermissions)
            throw this.disabledError();
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

        return Boolean.TRUE.equals(
                this.permissionRepository.findById(new GuildPermissionId(guild.getIdLong(), permission))
                        .map((gp) -> member.getRoles().contains(guild.getRoleById(gp.roleId())))
                        .or(false)
        );
    }

    @Override
    public boolean hasPermission(long userId, String permission) {
        return false;
    }

    @Override
    public Set<String> permissions(long guildId, Member member) {
        if (!this.useCustomPermissions)
            throw this.disabledError();
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
    public Map<String, Long> permissionBindings(Guild guild) {
        if (!this.useCustomPermissions)
            throw this.disabledError();

        Map<String, Long> bindings = new HashMap<>();
        this.permissionRepository.guildPermissions(guild.getIdLong())
                .forEach((gp) -> bindings.put(gp.permission(), gp.roleId()));

        this.rolePermissions().forEach((permission) -> bindings.putIfAbsent(permission, null));
        return bindings;
    }
}
