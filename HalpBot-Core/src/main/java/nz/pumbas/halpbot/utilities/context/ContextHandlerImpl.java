package nz.pumbas.halpbot.utilities.context;

import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import nz.pumbas.halpbot.utilities.Reflect;

public class ContextHandlerImpl implements ContextHandler
{
    Map<Class<?>, Object> instances = new HashMap<>();
    Map<Class<?>, Class<?>> implementations = new HashMap<>();

    /**
     * Retrieves the instance of the specified {@link Class implementation}. If there isn't already an implementation
     * for that class, then it tries to create one, assuming it has a constructor that takes no parameters. If the
     * specified {@link Class}, or a bound implementation if present is abstract or an interface, null is returned.
     *
     * @param implementation
     *      The {@link Class implementation} of the instance to retrieve
     * @param <T>
     *      The type of the instance to retrieve
     *
     * @return The instance, or null if there isn't one registered.
     */
    @Override
    @SuppressWarnings("unchecked")
    public <T> @Nullable T get(Class<T> implementation) {
        Class<?> contract = implementation;
        if (this.implementations.containsKey(implementation))
            contract = this.implementations.get(implementation);

        // For performance reasons, this condition has been added here as it will only be false when an implementation
        // has not yet been created and so prevents unnecessarily checking for an implementation if the class is a
        // contract.
        if (this.instances.containsKey(contract))
            return (T) this.instances.get(contract);

        else {
            // If the specified class is a contract, try and find an implementation for it.
            final Class<T> finalImplementation = implementation;
            implementation = this.implementations.entrySet()
                .stream()
                .filter(e -> e.getValue().isAssignableFrom(finalImplementation))
                .findFirst()
                .map(Entry::getKey)
                .map(c -> (Class<T>) c)
                .orElse(implementation);
        }

        return (T) this.createInstance(contract, implementation, true);
    }

    /**
     * Creates an instance of the {@link Class implementation} with the specified parameters. If the {@link Class
     * implementation} is an interface or abstract, then null is returned. If an object is created, you can specify
     * if you want to cache the instance, which automatically stores it in {@link ContextHandlerImpl#implementations}.
     *
     * @param contract
     *      The {@link Class} that the implementation is bound to
     * @param implementation
     *      The {@link Class} to create the instance of
     * @param cacheInstance
     *      If the instance should be cached after creation
     * @param parameters
     *      The constructor parameters
     *
     * @return The created {@link Object instance}
     */
    private @Nullable Object createInstance(Class<?> contract, Class<?> implementation,
                                            boolean cacheInstance, Object... parameters) {
        if (implementation.isInterface() || Modifier.isAbstract(implementation.getModifiers()))
            return null;

        Object instance = Reflect.createInstance(implementation, parameters);
        if (cacheInstance)
            this.instances.put(contract, instance);
        if (instance instanceof LateInit)
            ((LateInit) instance).lateInitialisation();

        return instance;
    }

    /**
     * Binds an {@link Class implementation} to the {@link Class contract}.
     *
     * @param contract
     *     The {@link Class contract}
     * @param implementations
     *     The {@link Class implementation} of the {@link Class contract}
     */
    @Override
    public void bind(Class<?> contract, Class<?> implementations) {
        this.implementations.put(implementations, contract);
    }
}
