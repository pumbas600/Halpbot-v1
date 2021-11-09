package nz.pumbas.halpbot.actions.methods;

import org.dockbox.hartshorn.core.domain.Exceptional;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;

public interface MethodCallback
{
    Method getMethod();

    @Nullable
    Object getInstance();

    default Exceptional<Object> invoke(Object... parameters) {
        return Exceptional.of(() -> this.getMethod().invoke(this.getInstance(), parameters));
    }
}
