package nz.pumbas.halpbot.decorators.log;

import nz.pumbas.halpbot.actions.invokable.ActionInvokable;
import nz.pumbas.halpbot.decorators.ActionInvokableDecoratorFactory;

public interface LogDecoratorFactory extends ActionInvokableDecoratorFactory<LogDecorator<?>, Log>
{
    //@Factory
    @Override
    LogDecorator<?> decorate(ActionInvokable<?> element, Log annotation);
}
