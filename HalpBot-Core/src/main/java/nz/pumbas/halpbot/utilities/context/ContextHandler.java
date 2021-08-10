package nz.pumbas.halpbot.utilities.context;

import nz.pumbas.halpbot.utilities.Exceptional;

public interface ContextHandler
{
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
    <T> T get(Class<T> implementation);

    /**
     * Retrieves an {@link Exceptional} of the instance of the specified {@link Class contract}.
     *
     * @param contract
     *      The {@link Class contract} of the instance to retrieve
     * @param <T>
     *      The type of the instance to retrieve
     *
     * @return An {@link Exceptional} of the instance, or {@link Exceptional#empty()} if there isn't one registered.
     */
    default <T> Exceptional<T> getSafely(Class<T> contract) {
        return Exceptional.of(this.get(contract));
    }

    /**
     * Binds an {@link Class implementation} to the {@link Class contract}.
     *
     * @param contract
     *      The {@link Class contract}
     * @param implementations
     *      The {@link Class implementation} of the {@link Class contract}
     */
    void bind(Class<?> contract, Class<?> implementations);
}
