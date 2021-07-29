package nz.pumbas.parsers;

import org.jetbrains.annotations.NotNull;

import java.lang.annotation.Annotation;
import java.util.function.Function;
import java.util.function.Predicate;

import nz.pumbas.commands.tokens.context.ContextState;
import nz.pumbas.commands.tokens.context.MethodContext;
import nz.pumbas.utilities.Exceptional;
import nz.pumbas.utilities.enums.Priority;

public class TypeParser<T> implements Parser<T>
{
    private final Function<MethodContext, Exceptional<T>> parser;
    private final Priority priority;

    public TypeParser(Function<MethodContext, Exceptional<T>> parser, Priority priority)
    {
        this.parser = parser;
        this.priority = priority;
    }

    /**
     * Returns a {@link TypeParserBuilder} that parses an element to the specified {@link Class type}.
     *
     * @param type
     *      The {@link Class type} of the parsed element
     * @param <T>
     *      The type of the parsed element
     *
     * @return A {@link TypeParserBuilder}
     */
    public static <T> TypeParserBuilder<T> builder(@NotNull Class<T> type) {
        return new TypeParserBuilder<>(type);
    }

    /**
     * Returns a {@link TypeParserBuilder} that utilises a {@link Predicate<Class> filter}.
     *
     * @param filter
     *      The {@link Predicate<Class>} filter that matches classes to this {@link Parser}
     * @param <T>
     *      The type of the parsed element
     *
     * @return A {@link TypeParserBuilder}
     */
    public static <T> TypeParserBuilder<T> builder(@NotNull Predicate<Class<?>> filter) {
        return new TypeParserBuilder<>(filter);
    }

    /**
     * @return The {@link Function} for this {@link Parser}
     */
    @Override
    public Function<MethodContext, Exceptional<T>> mapper()
    {
        return this.parser;
    }

    /**
     * @return The {@link Priority} associated with this {@link Parser}
     */
    @Override
    public Priority priority()
    {
        return this.priority;
    }



    protected static class TypeParserBuilder<T> {

        private Class<T> type;
        private Predicate<Class<?>> filter;

        private Function<MethodContext, Exceptional<T>> parser;
        private Priority priority = Priority.DEFAULT;
        private Class<?> annotation = Void.class;
        private boolean includeClassAnnotations;

        protected TypeParserBuilder(@NotNull Class<T> type) {
            this.type = type;
        }

        protected TypeParserBuilder(@NotNull Predicate<Class<?>> filter) {
            this.filter = filter;
        }

        public TypeParserBuilder<T> convert(@NotNull Function<MethodContext, Exceptional<T>> parser) {
            this.parser = ctx -> {
                int currentIndex = ctx.getCurrentIndex();
                ContextState state = ctx.contextState().copyContextState();
                Exceptional<T> result = parser.apply(ctx)
                    .caught(t -> ctx.setCurrentIndex(currentIndex));

                // Always restore the state of parser back to what it was when it was called.
                ctx.contextState(state);
                return result;
            };
            return this;
        }

        public TypeParserBuilder<T> priority(@NotNull Priority priority) {
            this.priority = priority;
            return this;
        }

        public TypeParserBuilder<T> annotation(@NotNull Class<? extends Annotation> annotation) {
            this.annotation = annotation;
            return this;
        }

        public TypeParserBuilder<T> includeClassAnnotations() {
            this.includeClassAnnotations = true;
            return this;
        }

        public TypeParser<T> build() {
            return new TypeParser<>(this.parser, this.priority);
        }

        public TypeParser<T> register() {
            TypeParser<T> typeParser = this.build();

            if (null == this.type)
                ParserManager.registerParser(this.filter, this.annotation, typeParser);
            else ParserManager.registerParser(this.type, this.annotation, typeParser);

            return typeParser;
        }
    }
}
