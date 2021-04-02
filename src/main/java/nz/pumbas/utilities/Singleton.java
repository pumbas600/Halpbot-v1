package nz.pumbas.utilities;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public final class Singleton
{
    private static final Map<Class<?>, Object> singletons = new HashMap<>();

    private Singleton() {}

    @SuppressWarnings("unchecked")
    public static <T> T getInstance(@NotNull Class<T> clazz)
    {
        if (!singletons.containsKey(clazz)) {
            singletons.put(clazz, Utilities.createInstance(clazz));
        }
        return (T) singletons.get(clazz);
    }
}
