package nz.pumbas.halpbot.actions.cooldowns;

import org.dockbox.hartshorn.core.annotations.inject.Binds;
import org.dockbox.hartshorn.core.annotations.inject.Bound;

import nz.pumbas.halpbot.actions.annotations.Cooldown;
import nz.pumbas.halpbot.commands.context.CommandContext;

@Binds(CooldownDecorator.class)
public class CooldownDecorator extends CommandContextDecorator
{
    @Bound
    public CooldownDecorator(CommandContext commandContext, Cooldown cooldown) {
        super(commandContext);
    }

    @Override
    public String toString() {
        return "Cooldown decorator";
    }
}
