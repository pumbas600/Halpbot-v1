package nz.pumbas.halpbot.converters;

@FunctionalInterface
public interface ConverterRegister
{
    /**
     * Registers the specified {@link Converter} with the {@link ConverterHandler}.
     *
     * @param handler
     *      The {@link ConverterHandler} to have this converter registered against.
     * @param converter
     *      The {@link Converter} to register.
     */
    void register(ConverterHandler handler, Converter<?> converter);
}
