package nz.pumbas.commands.tokens.tokentypes;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.annotation.Annotation;

import nz.pumbas.commands.tokens.context.InvocationContext;
import nz.pumbas.objects.Result;
import nz.pumbas.resources.Language;

/**
 * {@link ParsingToken Parsing tokens} are tokens which have a specific type and can parse an inputted {@link String} to this type.
 */
public interface ParsingToken extends CommandToken {

    /**
     * @return The {@link Annotation} annotations on this {@link ParsingToken}
     */
    Annotation[] getAnnotations();

    /**
     * @return The required {@link Class type} of this {@link ParsingToken}
     */
    Class<?> getType();

    /**
     * @return Retrieves the default value for this {@link ParsingToken} if this is optional, otherwise it returns null.
     */
    @Nullable
    Object getDefaultValue();

    /**
     * Parses the {@link String default value} for a token. If this {@link String default value} is null or a string
     * of 'null', then it returns null.
     * 
     * @param defaultValue
     *      {@link String default value} to be parsed into an {@link Object} using
     *      {@link ParsingToken#parse(InvocationContext)}
     *      
     * @return The parsed {@link Object default value}
     */
    @Nullable
    default Object parseDefaultValue(@Nullable String defaultValue) {
        if (null == defaultValue || "null".equalsIgnoreCase(defaultValue))
            return null;
        else {
            Result<Object> result = this.parse(InvocationContext.of(defaultValue));
            if (result.isValueAbsent())
                throw new IllegalArgumentException(
                    result.getReason().getTranslation(Language.EN_UK));

            return result.getValue();
        }
    }

    /**
     * Parses the context into the type of this {@link ParsingToken}. If the context doesn't match, the
     * {@link Result} will contain a {@link nz.pumbas.resources.Resource} explaing why.
     *
     * @param context
     *      The {@link InvocationContext}
     *
     * @return An {@link Result} containing the parsed context
     */
    Result<Object> parse(@NotNull InvocationContext context);
}
