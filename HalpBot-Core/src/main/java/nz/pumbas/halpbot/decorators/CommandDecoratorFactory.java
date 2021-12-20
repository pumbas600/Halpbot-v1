package nz.pumbas.halpbot.decorators;

import java.lang.annotation.Annotation;

import nz.pumbas.halpbot.commands.context.CommandContext;

public interface CommandDecoratorFactory<R extends CommandContext, A extends Annotation>
        extends DecoratorFactory<R, CommandContext, A>
{
}
