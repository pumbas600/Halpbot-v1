package nz.pumbas.halpbot.decorators;

import java.lang.annotation.Annotation;

public interface DecoratorFactory<R extends T, T, A extends Annotation>
{
    R decorate(T element, A annotation);
}
