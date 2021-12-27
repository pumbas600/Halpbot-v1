package nz.pumbas.halpbot.decorators.log;

import org.dockbox.hartshorn.core.annotations.service.Service;

import nz.pumbas.halpbot.actions.invokable.ActionInvokable;
import nz.pumbas.halpbot.decorators.ActionInvokableDecoratorFactory;

@Service
public interface LogDecoratorFactory extends ActionInvokableDecoratorFactory<LogDecorator<?>, Log>
{
    //@Factory
    @Override
    LogDecorator<?> decorate(ActionInvokable<?> element, Log annotation);
}
