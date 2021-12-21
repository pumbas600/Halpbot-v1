package nz.pumbas.halpbot.permissions;

import org.dockbox.hartshorn.core.annotations.inject.Binds;
import org.dockbox.hartshorn.core.annotations.inject.Bound;
import org.dockbox.hartshorn.core.domain.Exceptional;

import java.util.List;

import javax.inject.Inject;

import nz.pumbas.halpbot.actions.cooldowns.CommandContextDecorator;
import nz.pumbas.halpbot.commands.context.CommandContext;
import nz.pumbas.halpbot.commands.context.InvocationContext;
import nz.pumbas.halpbot.common.ExplainedException;
import nz.pumbas.halpbot.events.HalpbotEvent;

@Binds(PermissionDecorator.class)
public class PermissionDecorator extends CommandContextDecorator
{
    @Inject private PermissionService permissionService;

    private final List<String> permissions;

    @Bound
    public PermissionDecorator(CommandContext commandContext, Permission permission) {
        super(commandContext);
        this.permissions = List.of(permission.permissions());
    }

    @Override
    public <R> Exceptional<R> invoke(InvocationContext invocationContext, boolean canHaveContextLeft) {
        HalpbotEvent event = invocationContext.halpbotEvent();

        if (event == null || this.permissionService.hasPermissions(event.getUser().getIdLong(), this.permissions)) {
            return super.invoke(invocationContext, canHaveContextLeft);
        }
        return Exceptional.of(new ExplainedException("You do not have permission to use this command"));
    }
}
