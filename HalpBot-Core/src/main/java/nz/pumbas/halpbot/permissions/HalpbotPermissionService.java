package nz.pumbas.halpbot.permissions;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;

import org.dockbox.hartshorn.core.annotations.inject.Binds;
import org.dockbox.hartshorn.core.annotations.stereotype.Service;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Inject;

import nz.pumbas.halpbot.permissions.repositories.GuildPermission;
import nz.pumbas.halpbot.permissions.repositories.PermissionRepository;

@Service
@Binds(PermissionService.class)
public class HalpbotPermissionService implements PermissionService
{
    private final Set<String> permissions = new HashSet<>();

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
    public void updateOrSave(GuildPermission guildPermission) {
        this.permissionRepository.updateOrSave(guildPermission);
    }

    @Override
    public void addPermissions(Set<String> permissions) {
        this.permissions.addAll(permissions);
    }

    @Override
    public boolean hasPermission(Guild guild, Member member, String permission) {
        long roleId = this.permissionRepository.guildRole(guild.getIdLong(), permission);
        Role role = guild.getRoleById(roleId);
        return member.getRoles().contains(role);
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
}
