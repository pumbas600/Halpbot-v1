package nz.pumbas.halpbot.decorators;

import org.dockbox.hartshorn.core.annotations.service.Service;

import java.lang.annotation.Annotation;

import nz.pumbas.halpbot.commands.context.CommandContext;
import nz.pumbas.halpbot.commands.context.decorators.CommandContextDecorator;

@Service
public interface CommandContextDecoratorFactory
{
    <A extends Annotation, T extends CommandContextDecorator<A>> T create(A annotation,
                                                                            CommandContext commandContext);

    default <A extends Annotation, T extends CommandContextDecorator<A>> T create(Class<T> type,
                                                                               A annotation,
                                                                               CommandContext commandContext)
    {
        return this.create(annotation, commandContext);
    }
}
