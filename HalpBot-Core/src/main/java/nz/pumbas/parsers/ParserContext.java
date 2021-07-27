package nz.pumbas.parsers;

public class ParserContext {

    private final Class<?> annotation;
    private final TypeParser<?> typeParser;
    private final boolean includeClassAnnotations;

    public ParserContext(Class<?> annotation, TypeParser<?> typeParser, boolean includeClassAnnotations) {
        this.annotation = annotation;
        this.typeParser = typeParser;
        this.includeClassAnnotations = includeClassAnnotations;
    }

    public Class<?> annotation() {
        return this.annotation;
    }

    public TypeParser<?> typeParser() {
        return this.typeParser;
    }

    public boolean includeClassAnnotations() {
        return this.includeClassAnnotations;
    }
}
