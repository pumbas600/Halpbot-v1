package nz.pumbas.halpbot.decorators.time;

import org.dockbox.hartshorn.core.annotations.Factory;
import org.dockbox.hartshorn.core.annotations.stereotype.Service;

import nz.pumbas.halpbot.actions.invokable.ActionInvokable;
import nz.pumbas.halpbot.decorators.ActionInvokableDecoratorFactory;

@Service
public interface TimeDecoratorFactory extends ActionInvokableDecoratorFactory<TimeDecorator<?>, Time>
{
    @Factory
    @Override
    TimeDecorator<?> decorate(ActionInvokable<?> element, Time annotation);
}
