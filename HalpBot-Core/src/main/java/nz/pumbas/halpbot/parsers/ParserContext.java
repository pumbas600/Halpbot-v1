package nz.pumbas.halpbot.parsers;

public class ParserContext
{

    private final Class<?> annotationType;
    private final Parser<?> parser;

    public ParserContext(Class<?> annotationType, Parser<?> parser) {
        this.annotationType = annotationType;
        this.parser = parser;
    }

    /**
     * @return The {@link Class annotation type} for this {@link ParserContext}
     */
    public Class<?> getAnnotationType() {
        return this.annotationType;
    }

    /**
     * @return The {@link Parser} for this {@link ParserContext}
     */
    public Parser<?> getParser() {
        return this.parser;
    }
}
