package nz.pumbas.halpbot.permissions;

import org.dockbox.hartshorn.core.annotations.inject.Binds;
import org.dockbox.hartshorn.core.annotations.inject.Bound;
import org.dockbox.hartshorn.core.domain.Exceptional;

import java.util.Set;

import javax.inject.Inject;

import lombok.Getter;
import nz.pumbas.halpbot.actions.invokable.ActionInvokable;
import nz.pumbas.halpbot.actions.invokable.ActionInvokableDecorator;
import nz.pumbas.halpbot.actions.invokable.InvocationContext;
import nz.pumbas.halpbot.common.ExplainedException;
import nz.pumbas.halpbot.events.HalpbotEvent;

@Binds(PermissionDecorator.class)
public class PermissionDecorator<C extends InvocationContext> extends ActionInvokableDecorator<C>
{
    @Inject private PermissionService permissionService;
    @Getter private final Set<String> permissions;

    @Bound
    public PermissionDecorator(ActionInvokable<C> actionInvokable, Permission permission) {
        super(actionInvokable);
        this.permissions = Set.of(permission.value());
    }

    @Override
    public <R> Exceptional<R> invoke(C invocationContext) {
        HalpbotEvent event = invocationContext.halpbotEvent();

        if (this.permissionService.hasPermission(event.user().getIdLong(), this.permissions)) {
            return super.invoke(invocationContext);
        }
        return Exceptional.of(new ExplainedException("You do not have permission to use this command"));
    }
}
