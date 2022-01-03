package nz.pumbas.halpbot.permissions;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;

import org.dockbox.hartshorn.core.Enableable;
import org.dockbox.hartshorn.core.annotations.inject.Binds;
import org.dockbox.hartshorn.core.annotations.inject.Bound;
import org.dockbox.hartshorn.core.domain.Exceptional;

import java.util.HashSet;
import java.util.Set;
import java.util.function.BiPredicate;

import javax.inject.Inject;

import lombok.Getter;
import nz.pumbas.halpbot.actions.invokable.ActionInvokable;
import nz.pumbas.halpbot.actions.invokable.ActionInvokableDecorator;
import nz.pumbas.halpbot.actions.invokable.InvocationContext;
import nz.pumbas.halpbot.common.ExplainedException;
import nz.pumbas.halpbot.events.HalpbotEvent;

@Binds(PermissionDecorator.class)
public class PermissionDecorator<C extends InvocationContext> extends ActionInvokableDecorator<C> implements Enableable
{
    @Inject
    @Getter private PermissionService permissionService;
    @Getter private final Set<String> customPermissions = new HashSet<>();
    @Getter private final Set<Permission> jdaPermissions = new HashSet<>();
    @Getter private final Merger merger;
    @Getter private final BiPredicate<Guild, Member> hasPermissions;

    @Bound
    public PermissionDecorator(ActionInvokable<C> actionInvokable, Permissions permissions) {
        super(actionInvokable);
        this.customPermissions.addAll(Set.of(permissions.permissions()));
        this.jdaPermissions.addAll(Set.of(permissions.value()));
        this.merger = permissions.merger();
        this.hasPermissions = switch (this.merger) {
            case AND -> this::and;
            case OR -> this::or;
        };
    }

    @Override
    public <R> Exceptional<R> invoke(C invocationContext) {
        HalpbotEvent event = invocationContext.halpbotEvent();
        Guild guild = event.guild();
        Member member = event.member();

        if (guild == null || member == null || this.hasPermissions.test(guild, member)) {
            return super.invoke(invocationContext);
        }
        return Exceptional.of(new ExplainedException("You do not have permission to use this command"));
    }

    protected boolean or(Guild guild, Member member) {
        for (Permission permission : this.jdaPermissions()) {
            if (member.hasPermission(permission))
                return true;
        }
        for (String permission : this.customPermissions()) {
            if (this.permissionService().hasPermission(guild, member, permission))
                return true;
        }
        return false;
    }

    protected boolean and(Guild guild, Member member) {
        return member.hasPermission(this.jdaPermissions()) &&
                this.permissionService().hasPermissions(guild, member, this.customPermissions());
    }

    @Override
    public void enable() {
        this.permissionService.addPermissions(this.customPermissions);
    }
}
