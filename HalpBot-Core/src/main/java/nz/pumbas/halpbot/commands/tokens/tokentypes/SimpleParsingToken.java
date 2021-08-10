package nz.pumbas.halpbot.commands.tokens.tokentypes;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import nz.pumbas.halpbot.commands.tokens.context.MethodContext;
import nz.pumbas.halpbot.parsers.Parser;
import nz.pumbas.halpbot.parsers.ParserHandler;
import nz.pumbas.halpbot.commands.annotations.Unrequired;
import nz.pumbas.halpbot.parsers.TypeParser;
import nz.pumbas.halpbot.utilities.HalpbotUtils;

public class SimpleParsingToken implements ParsingToken
{
    private final Type type;
    private final Annotation[] annotations;
    private final List<Class<? extends Annotation>> annotationTypes;
    private final boolean isOptional;
    private final Parser<?> parser;
    private final Object defaultValue;

    public SimpleParsingToken(Type type, Annotation[] annotations) {
        this.type = type;
        this.annotations = annotations;
        this.annotationTypes = Stream.of(annotations)
            .map(Annotation::annotationType)
            .collect(Collectors.toUnmodifiableList());

        Unrequired unrequired = this.getAnnotation(Unrequired.class);
        this.isOptional = null != unrequired;

        String defaultValue = this.isOptional ? unrequired.value() : "null";
        MethodContext ctx = MethodContext.of(defaultValue, this);

        this.parser = HalpbotUtils.context().get(ParserHandler.class).from(ctx);
        this.defaultValue = this.parseDefaultValue(ctx);
    }

    /**
     * @return If this {@link Token} is optional or not
     */
    @Override
    public boolean isOptional() {
        return this.isOptional;
    }

    /**
     * @return The {@link Annotation} annotations on this {@link ParsingToken}
     */
    @Override
    public @NotNull Annotation[] getAnnotations() {
        return this.annotations;
    }

    /**
     * @return A copy of the {@link List} of the {@link Class types} of the annotations on this {@link ParsingToken}
     */
    @Override
    public @NotNull List<Class<? extends Annotation>> getAnnotationTypes() {
        return new ArrayList<>(this.annotationTypes);
    }

    /**
     * @return The required {@link Type} of this {@link ParsingToken}
     */
    @Override
    public @NotNull Type getType() {
        return this.type;
    }

    /**
     * @return The {@link TypeParser} for this token
     */
    @Override
    public @NotNull Parser<?> getParser() {
        return this.parser;
    }

    /**
     * @return Retrieves the default value for this {@link ParsingToken} if this is optional, otherwise it returns null.
     */
    @Override
    public @Nullable Object getDefaultValue() {
        return this.defaultValue;
    }
}
