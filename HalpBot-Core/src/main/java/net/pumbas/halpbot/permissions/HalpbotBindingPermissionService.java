/*
 * MIT License
 *
 * Copyright (c) 2021 pumbas600
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package net.pumbas.halpbot.permissions;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.pumbas.halpbot.permissions.repositories.GuildPermission;
import net.pumbas.halpbot.permissions.repositories.GuildPermissionId;
import net.pumbas.halpbot.permissions.repositories.PermissionRepository;

import org.dockbox.hartshorn.component.Service;
import org.dockbox.hartshorn.util.Result;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import jakarta.inject.Inject;

@Service(lazy = true)
public class HalpbotBindingPermissionService extends HalpbotPermissionService {

    @Inject
    private PermissionRepository permissionRepository;

    @Override
    public GuildPermission updateOrSave(final GuildPermission guildPermission) {
        return this.permissionRepository.updateOrSave(guildPermission);
    }

    @Override
    public GuildPermission update(final GuildPermission guildPermission) {
        return this.permissionRepository.update(guildPermission);
    }

    @Override
    public GuildPermission save(final GuildPermission guildPermission) {
        return this.permissionRepository.save(guildPermission);
    }

    @Override
    public void delete(final GuildPermission guildPermission) {
        this.permissionRepository.delete(guildPermission);
    }

    @Override
    public boolean isRoleBound(final long guildId, final long roleId) {
        return this.permissionRepository.countPermissionsWithRole(guildId, roleId) != 0;
    }

    @Override
    public boolean isPermissionBound(final long guildId, final String permission) {
        return this.permissionRepository.countPermissions(guildId, permission) != 0;
    }

    @Override
    public boolean hasPermission(final Guild guild, final Member member, final String permission) {
        return super.hasPermission(guild, member, permission) || Boolean.TRUE.equals(
            this.permissionRepository.findById(new GuildPermissionId(guild.getIdLong(), permission))
                .map((gp) -> member.getRoles().contains(guild.getRoleById(gp.roleId())))
                .or(false)
        );
    }

    @Override
    public Result<GuildPermission> findById(final GuildPermissionId id) {
        return this.permissionRepository.findById(id);
    }

    @Override
    public Set<String> permissions(final long guildId, final Member member) {
        return this.permissionRepository
            .permissions(guildId, member.getRoles()
                .stream()
                .map(Role::getIdLong)
                .collect(Collectors.toSet())
            );
    }

    @Override
    public Map<String, Long> roleBindings(final Guild guild) {
        final Map<String, Long> bindings = new HashMap<>();
        this.permissionRepository.guildPermissions(guild.getIdLong())
            .forEach((gp) -> bindings.put(gp.permission(), gp.roleId()));

        this.rolePermissions().forEach((permission) -> bindings.putIfAbsent(permission, null));
        return bindings;
    }

    @Override
    public void enable() {
        super.enable();
        this.deleteOldPermissions();
    }

    private void deleteOldPermissions() {
        final List<GuildPermission> oldPermissions = this.permissionRepository.findAll()
            .stream()
            .filter((gp) -> !this.isRegistered(gp.permission()))
            .toList();
        if (!oldPermissions.isEmpty()) {
            this.applicationContext().log().info("Deleting the following deprecated permissions from the database: %s"
                .formatted(String.join(", ", oldPermissions.stream().map(GuildPermission::permission).toList())));
            oldPermissions.forEach(this::delete);
        }
    }
}
