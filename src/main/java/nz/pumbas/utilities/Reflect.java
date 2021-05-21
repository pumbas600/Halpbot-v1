package nz.pumbas.utilities;

import java.lang.reflect.Method;

public final class Reflect {

    private Reflect() {}

    /**
     * Returns the first {@link Method} in a class with the specified name. If the method cannot be
     * found, an {@link IllegalArgumentException} is thrown.
     *
     * @param clazz
     *     The {@link Class} to check, for the {@link Method} with the specified name
     * @param name
     *     The name of the {@link Method} to find
     *
     * @return The {@link Method} with the specified name
     */
    public static Method getMethod(Class<?> clazz, String name)
    {
        for (Method method : clazz.getDeclaredMethods()) {
            if (method.getName().equals(name)) {
                method.setAccessible(true);
                return method;
            }
        }
        throw new IllegalArgumentException(
                String.format("There is no method with the name %s in the class %s", name, clazz.getSimpleName()));
    }

    /**
     * Returns the first {@link Method} in a class with the specified name. If the method cannot be
     * found, an {@link IllegalArgumentException} is thrown.
     *
     * @param instance
     *     The {@link Object} to check for the {@link Method} with the specified name
     * @param name
     *     The name of the {@link Method} to find
     *
     * @return The {@link Method} with the specified name
     */
    public static Method getMethod(Object instance, String name)
    {
        return getMethod(instance.getClass(), name);
    }
}
