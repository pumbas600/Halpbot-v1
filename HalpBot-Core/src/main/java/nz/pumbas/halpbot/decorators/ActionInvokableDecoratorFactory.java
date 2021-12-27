package nz.pumbas.halpbot.decorators;

import java.lang.annotation.Annotation;

import nz.pumbas.halpbot.actions.invokable.ActionInvokable;

public interface ActionInvokableDecoratorFactory<R extends ActionInvokable<?>, A extends Annotation>
    extends DecoratorFactory<R, ActionInvokable<?>, A>
{
}
