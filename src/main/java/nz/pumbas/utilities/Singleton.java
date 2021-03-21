package nz.pumbas.utilities;

import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import nz.pumbas.commands.ErrorManager;

public final class Singleton
{
    private static final Map<Class<?>, Object> singletons = new HashMap<>();

    private Singleton() {}

    @SuppressWarnings("unchecked")
    public static <T> T getInstance(@NotNull Class<T> clazz) {
        if (!singletons.containsKey(clazz)) {
            try {
                Constructor<?> constructor =  clazz.getConstructors()[0];
                constructor.setAccessible(true);
                if (0 != constructor.getParameterCount())
                    throw new IllegalArgumentException(
                        String.format("The singleton %s can't take any paramaters in its constructor",
                            clazz.getSimpleName()));

                singletons.put(clazz, constructor.newInstance());
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                ErrorManager.handle(e);
            }
        }
        return (T)singletons.get(clazz);
    }
}
