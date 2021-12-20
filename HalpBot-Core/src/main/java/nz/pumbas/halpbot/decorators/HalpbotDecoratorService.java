package nz.pumbas.halpbot.decorators;

import org.dockbox.hartshorn.core.HartshornUtils;
import org.dockbox.hartshorn.core.annotations.inject.Binds;
import org.dockbox.hartshorn.core.context.ApplicationContext;
import org.dockbox.hartshorn.core.context.element.TypeContext;

import java.lang.annotation.Annotation;
import java.util.Map;

import javax.inject.Inject;

import lombok.Getter;

@Binds(DecoratorService.class)
public class HalpbotDecoratorService implements DecoratorService
{
    private final Map<Class<?>, DecoratorContext> decorators = HartshornUtils.emptyMap();

    @Getter @Inject private ApplicationContext applicationContext;

    @Override
    public void register(TypeContext<?> decoratedAnnotation) {
        Method method = decoratedAnnotation.annotation(Decorator.class).map(Decorator::value).get();
        TypeContext<?> instanceType = TypeContext.of(method.type());
        Object instance = this.applicationContext.get(instanceType);

//        if (instance != null) {
//            instanceType.method(method.name(), )
//        }


        //this.decorators.put(decoratedAnnotation.type(), )
    }

    @Override
    public DecoratorContext decorator(Annotation annotation) {
        return null;
    }
}
