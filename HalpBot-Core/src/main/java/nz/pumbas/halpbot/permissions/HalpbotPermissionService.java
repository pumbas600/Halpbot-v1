package nz.pumbas.halpbot.permissions;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;

import org.dockbox.hartshorn.core.HartshornUtils;
import org.dockbox.hartshorn.core.annotations.inject.Binds;
import org.dockbox.hartshorn.core.annotations.stereotype.Service;
import org.dockbox.hartshorn.core.context.ApplicationContext;
import org.dockbox.hartshorn.core.context.element.MethodContext;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Inject;

import lombok.Getter;
import nz.pumbas.halpbot.actions.methods.Invokable;
import nz.pumbas.halpbot.actions.methods.InvokableFactory;
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

    private final Set<String> permissions = new HashSet<>();
    private final Map<String, Invokable> permissionSuppliers = new HashMap<>();

    @Inject
    private PermissionRepository permissionRepository;

    @Override
    public boolean isPermission(String permission) {
        return this.permissions.contains(permission);
    }

    @Override
    public boolean isPermission(long guildId, long roleId) {
        return this.permissionRepository.countPermissionsWithRole(guildId, roleId) != 0;
    }

    @Override
    public boolean permissionExists(long guildId, String permission) {
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
    public void updateOrSave(GuildPermission guildPermission) {
        this.permissionRepository.updateOrSave(guildPermission);
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
        Map<String, Long> bindings = new HashMap<>();
        this.permissionRepository.guildPermissions(guild.getIdLong())
                .forEach((gp) -> bindings.put(gp.permission(), gp.roleId()));

        this.rolePermissions().forEach((permission) -> bindings.putIfAbsent(permission, null));
        return bindings;
    }
}
