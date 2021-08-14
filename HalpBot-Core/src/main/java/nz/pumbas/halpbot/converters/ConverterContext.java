package nz.pumbas.halpbot.converters;

public class ConverterContext
{

    private final Class<?> annotationType;
    private final Converter<?> converter;

    public ConverterContext(Class<?> annotationType, Converter<?> converter) {
        this.annotationType = annotationType;
        this.converter = converter;
    }

    /**
     * @return The {@link Class annotation type} for this {@link ConverterContext}
     */
    public Class<?> getAnnotationType() {
        return this.annotationType;
    }

    /**
     * @return The {@link Converter} for this {@link ConverterContext}
     */
    public Converter<?> getConverter() {
        return this.converter;
    }
}
