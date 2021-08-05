package nz.pumbas.parsers;

public class ParserContext {

    private final Class<?> annotationType;
    private final Parser<?> parser;
    private final boolean includeClassAnnotations;

    public ParserContext(Class<?> annotationType, Parser<?> parser, boolean includeClassAnnotations) {
        this.annotationType = annotationType;
        this.parser = parser;
        this.includeClassAnnotations = includeClassAnnotations;
    }

    public Class<?> annotationType() {
        return this.annotationType;
    }

    public Parser<?> parser() {
        return this.parser;
    }

    public boolean includeClassAnnotations() {
        return this.includeClassAnnotations;
    }
}
