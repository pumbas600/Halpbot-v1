package nz.pumbas.halpbot.utilities.context;

public interface ContextBinder
{
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
