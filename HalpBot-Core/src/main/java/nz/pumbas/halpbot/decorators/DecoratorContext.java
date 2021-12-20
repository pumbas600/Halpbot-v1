package nz.pumbas.halpbot.decorators;

import java.lang.annotation.Annotation;

import nz.pumbas.halpbot.actions.methods.Invokable;

public interface DecoratorContext extends Invokable
{
    @SuppressWarnings("unchecked")
    default <T, A extends Annotation> T decorate(T element, A annotation) {
        return (T) this.invoke(element, annotation).get();
    }
}
