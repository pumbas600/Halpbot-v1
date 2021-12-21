package nz.pumbas.halpbot.decorators;

import nz.pumbas.halpbot.commands.context.CommandContext;
import nz.pumbas.halpbot.permissions.Permission;
import nz.pumbas.halpbot.permissions.PermissionDecorator;

public interface PermissionDecoratorFactory extends CommandDecoratorFactory<PermissionDecorator, Permission>
{
    //@Factory
    @Override
    PermissionDecorator decorate(CommandContext element, Permission annotation);
}
