package nz.pumbas.halpbot.actions.cooldowns;

import org.dockbox.hartshorn.core.annotations.service.Service;

import nz.pumbas.halpbot.actions.annotations.Cooldown;
import nz.pumbas.halpbot.actions.cooldowns.CooldownDecorator;
import nz.pumbas.halpbot.actions.invokable.ActionInvokable;
import nz.pumbas.halpbot.decorators.ActionInvokableDecoratorFactory;

@Service
public interface CooldownDecoratorFactory extends ActionInvokableDecoratorFactory<CooldownDecorator<?>, Cooldown>
{
    //@Factory
    @Override
    CooldownDecorator<?> decorate(ActionInvokable<?> element, Cooldown annotation);
}
