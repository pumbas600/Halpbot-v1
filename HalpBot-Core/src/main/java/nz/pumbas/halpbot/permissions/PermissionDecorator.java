package nz.pumbas.halpbot.permissions;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;

import org.dockbox.hartshorn.core.Enableable;
import org.dockbox.hartshorn.core.annotations.inject.Binds;
import org.dockbox.hartshorn.core.annotations.inject.Bound;
import org.dockbox.hartshorn.core.annotations.stereotype.Service;
import org.dockbox.hartshorn.core.domain.Exceptional;
import org.dockbox.hartshorn.core.exceptions.ApplicationException;

import java.util.Set;

import javax.inject.Inject;

import lombok.Getter;
import lombok.Setter;
import nz.pumbas.halpbot.actions.invokable.ActionInvokable;
import nz.pumbas.halpbot.actions.invokable.ActionInvokableDecorator;
import nz.pumbas.halpbot.actions.invokable.InvocationContext;
import nz.pumbas.halpbot.common.ExplainedException;
import nz.pumbas.halpbot.events.HalpbotEvent;

@Binds(PermissionDecorator.class)
public class PermissionDecorator<C extends InvocationContext> extends ActionInvokableDecorator<C> implements Enableable
{
    @Inject protected PermissionService permissionService;
    @Getter protected final Set<String> customPermissions;
    @Getter protected final Set<Permission> jdaPermissions;

    @Bound
    public PermissionDecorator(ActionInvokable<C> actionInvokable, Permissions permissions) {
        super(actionInvokable);
        this.customPermissions = Set.of(permissions.permissions());
        this.jdaPermissions = Set.of(permissions.value());
    }

    @Override
    public <R> Exceptional<R> invoke(C invocationContext) {
        HalpbotEvent event = invocationContext.halpbotEvent();
        Guild guild = event.guild();
        Member member = event.member();

        if (guild == null || member == null || this.hasPermission(guild, member))
        {
            return super.invoke(invocationContext);
        }
        return Exceptional.of(new ExplainedException("You do not have permission to use this command"));
    }

    private boolean hasPermission(Guild guild, Member member) {
        return member.hasPermission(this.jdaPermissions) &&
                this.permissionService.hasPermissions(guild, member, this.customPermissions);
    }

    @Override
    public void enable() {
        this.permissionService.addPermissions(this.customPermissions);
    }
}
