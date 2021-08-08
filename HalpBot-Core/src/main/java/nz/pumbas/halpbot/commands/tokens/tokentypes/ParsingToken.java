package nz.pumbas.halpbot.commands.tokens.tokentypes;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.List;

import nz.pumbas.halpbot.commands.tokens.context.MethodContext;
import nz.pumbas.halpbot.parsers.Parser;
import nz.pumbas.halpbot.parsers.TypeParser;

/**
 * {@link ParsingToken Parsing tokens} are tokens which have a specific type and can parse an inputted {@link String} to this type.
 */
public interface ParsingToken extends Token
{

    /**
     * @return The {@link Annotation} annotations on this {@link ParsingToken}
     */
    @NotNull
    Annotation[] getAnnotations();

    /**
     * @return An {@link List} of the {@link Class types} of the annotations on this {@link ParsingToken}
     */
    @NotNull
    List<Class<? extends Annotation>> getAnnotationTypes();

    /**
     * @return The required {@link Type} of this {@link ParsingToken}
     */
    @NotNull
    Type getType();

    /**
     * @return The {@link TypeParser} for this token
     */
    @NotNull
    Parser<?> getParser();

    /**
     * @return Retrieves the default value for this {@link ParsingToken} if this is optional, otherwise it returns null.
     */
    @Nullable
    Object getDefaultValue();

    /**
     * Retrieves the {@link Annotation} on this {@link ParsingToken} based on the specified {@link Class annotation
     * type}.
     *
     * @param annotationType
     *     The {@link Class annotation type}
     *
     * @return The {@link Annotation} with the matching {@link Class annotation type}
     */
    @Nullable
    @SuppressWarnings("unchecked")
    default <T extends Annotation> T getAnnotation(Class<T> annotationType) {
        int index = this.getAnnotationTypes().indexOf(annotationType);
        if (-1 == index)
            return null;
        return (T) this.getAnnotations()[index];
    }

    /**
     * Parses the {@link MethodContext} of the default value.
     *
     * @param ctx
     *     {@link MethodContext} containing the default value to be parsed into an {@link Object} using the token's
     *     {@link TypeParser}
     *
     * @return The parsed {@link Object default value}
     */
    @Nullable
    default Object parseDefaultValue(@NotNull MethodContext ctx) {
        if ("null".equalsIgnoreCase(ctx.getOriginal()))
            return null;
        if (String.class.isAssignableFrom(ctx.getContextState().getClazz()))
            return ctx.getOriginal();
        return this.getParser()
            .getMapper()
            .apply(ctx)
            .rethrow()
            .get();

    }
}
