package nz.pumbas.halpbot.actions;

import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;

import nz.pumbas.halpbot.objects.Exceptional;

public interface MethodCallback
{
    Method getMethod();

    @Nullable
    Object getInstance();

    default Exceptional<Object> invoke(Object... parameters) {
        return Exceptional.of(() -> this.getMethod().invoke(this.getInstance(), parameters));
    }
}
