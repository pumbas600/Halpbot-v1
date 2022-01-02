package nz.pumbas.halpbot.actions.methods;

import org.dockbox.hartshorn.core.context.ApplicationContext;
import org.dockbox.hartshorn.core.context.element.ConstructorContext;
import org.dockbox.hartshorn.core.context.element.ExecutableElementContext;
import org.dockbox.hartshorn.core.context.element.MethodContext;
import org.dockbox.hartshorn.core.domain.Exceptional;
import org.jetbrains.annotations.Nullable;

public interface Invokable
{
    @Nullable
    Object instance();

    ExecutableElementContext<?, ?> executable();

    @SuppressWarnings("unchecked")
    default <R> Exceptional<R> invoke(Object... parameters) {
        final ExecutableElementContext<?, ?> executable = this.executable();
        if (executable instanceof MethodContext methodContext) {
            return methodContext.invoke(this.instance(), parameters);
        }
        ConstructorContext<R> constructorContext = (ConstructorContext<R>) executable;
        return constructorContext.createInstance(parameters);
    }

    @SuppressWarnings("unchecked")
    default <R> Exceptional<R> invoke(ApplicationContext applicationContext) {
        final ExecutableElementContext<?, ?> executable = this.executable();
        if (executable instanceof MethodContext methodContext) {
            return methodContext.invoke(applicationContext, this.instance());
        }
        ConstructorContext<R> constructorContext = (ConstructorContext<R>) executable;
        return constructorContext.createInstance(applicationContext);
    }
}
