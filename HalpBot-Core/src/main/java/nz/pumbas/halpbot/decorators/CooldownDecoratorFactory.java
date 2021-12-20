package nz.pumbas.halpbot.decorators;

import org.dockbox.hartshorn.core.annotations.Factory;
import org.dockbox.hartshorn.core.annotations.service.Service;

import nz.pumbas.halpbot.actions.annotations.Cooldown;
import nz.pumbas.halpbot.actions.cooldowns.CooldownDecorator;
import nz.pumbas.halpbot.commands.context.CommandContext;

@Service
public interface CooldownDecoratorFactory extends CommandDecoratorFactory<CooldownDecorator, Cooldown>
{
    //@Factory
    @Override
    CooldownDecorator decorate(CommandContext element, Cooldown annotation);
}
