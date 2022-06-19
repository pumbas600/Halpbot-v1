package net.pumbas.halpbot.permissions;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.pumbas.halpbot.HalpbotCore;
import net.pumbas.halpbot.actions.invokable.Invokable;
import net.pumbas.halpbot.actions.invokable.InvokableFactory;
import net.pumbas.halpbot.configurations.BotConfiguration;
import net.pumbas.halpbot.permissions.repositories.GuildPermission;
import net.pumbas.halpbot.permissions.repositories.GuildPermissionId;

import org.dockbox.hartshorn.application.context.ApplicationContext;
import org.dockbox.hartshorn.component.Enableable;
import org.dockbox.hartshorn.component.Service;
import org.dockbox.hartshorn.util.Result;
import org.dockbox.hartshorn.util.reflect.MethodContext;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import jakarta.inject.Inject;
import lombok.Getter;

@Service
public class HalpbotPermissionService implements PermissionService, Enableable {

    private final Set<String> permissions = new HashSet<>();
    private final Map<String, Invokable> permissionSuppliers = new HashMap<>();
    @Getter
    @Inject
    private ApplicationContext applicationContext;

    @Getter
    @Inject
    private HalpbotCore halpbotCore;

    @Inject
    private BotConfiguration configuration;

    @Inject
    private InvokableFactory invokableFactory;

    @Override
    public boolean useRoleBinding() {
        return this.configuration.useRoleBinding();
    }

    @Override
    public <T> void registerPermissionSupplier(final T instance, final String permission, final MethodContext<Boolean, T> predicate) {
        if (this.permissionSuppliers.containsKey(permission))
            this.applicationContext.log().warn("There is already a supplier for the permission %s".formatted(permission));
        else {
            final Invokable invokable = this.invokableFactory.create(instance, predicate);
            this.permissionSuppliers.put(permission, invokable);
        }
    }

    @Override
    public GuildPermission updateOrSave(final GuildPermission guildPermission) {
        return guildPermission;
    }

    @Override
    public GuildPermission update(final GuildPermission guildPermission) {
        return guildPermission;
    }

    @Override
    public GuildPermission save(final GuildPermission guildPermission) {
        return guildPermission;
    }

    @Override
    public void delete(final GuildPermission guildPermission) {

    }

    @Override
    public boolean isRegistered(final String permission) {
        return this.permissions.contains(permission);
    }

    @Override
    public boolean isRoleBound(final long guildId, final long roleId) {
        return false;
    }

    @Override
    public boolean isPermissionBound(final long guildId, final String permission) {
        return false;
    }

    @Override
    public void addPermissions(final Set<String> permissions) {
        this.permissions.addAll(permissions);
    }

    @Override
    public boolean hasPermission(final Guild guild, final Member member, final String permission) {
        // The owner has permission to use any command
        if (this.isOwner(member))
            return true;
        if (this.permissionSuppliers.containsKey(permission))
            return this.evaluatePermissionSupplier(permission, guild, member);
        return false;
    }

    @Override
    public Result<GuildPermission> findById(final GuildPermissionId id) {
        return Result.empty();
    }

    @Override
    public Set<String> permissions(final long guildId, final Member member) {
        return Collections.emptySet();
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
    public Map<String, Long> roleBindings(final Guild guild) {
        return Collections.emptyMap();
    }


    protected boolean evaluatePermissionSupplier(final String permission, final Guild guild, final Member member) {
        return Boolean.TRUE.equals(this.permissionSuppliers.get(permission)
            .invoke(guild, member)
            .or(false));
    }

    @Override
    public void enable() {
        if (!this.rolePermissions().isEmpty())
            this.applicationContext.log()
                .error("You haven't enabled role binding in the bot-config but you have some defined.");
    }
}
