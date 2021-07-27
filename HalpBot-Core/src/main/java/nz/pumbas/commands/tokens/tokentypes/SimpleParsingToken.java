package nz.pumbas.commands.tokens.tokentypes;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import nz.pumbas.commands.annotations.Unrequired;
import nz.pumbas.commands.tokens.context.ParsingContext;
import nz.pumbas.parsers.Parser;
import nz.pumbas.parsers.ParserManager;
import nz.pumbas.parsers.TypeParser;

public class SimpleParsingToken implements ParsingToken
{
    private final Type type;
    private final Annotation[] annotations;
    private final List<Class<? extends Annotation>> annotationTypes;
    private final boolean isOptional;
    private final Parser<?> parser;
    private final Object defaultValue;

    public SimpleParsingToken(Type type, Annotation[] annotations)
    {
        this.type = type;
        this.annotations = annotations;
        this.annotationTypes = Stream.of(annotations)
            .map(Annotation::annotationType)
            .collect(Collectors.toUnmodifiableList());

        Unrequired unrequired = this.annotation(Unrequired.class);
        this.isOptional = null != unrequired;

        String defaultValue = this.isOptional ? unrequired.value() : "null";
        ParsingContext ctx = ParsingContext.of(defaultValue, this);

        this.parser = ParserManager.from(ctx);
        this.defaultValue = this.parseDefaultValue(ctx);
    }

    /**
     * @return If this {@link Token} is optional or not
     */
    @Override
    public boolean isOptional()
    {
        return this.isOptional;
    }

    /**
     * @return The {@link Annotation} annotations on this {@link ParsingToken}
     */
    @Override
    public @NotNull Annotation[] annotations()
    {
        return this.annotations;
    }

    /**
     * @return A copy of the {@link List} of the {@link Class types} of the annotations on this {@link ParsingToken}
     */
    @Override
    public @NotNull List<Class<? extends Annotation>> annotationTypes()
    {
        return new ArrayList<>(this.annotationTypes);
    }

    /**
     * @return The required {@link Type} of this {@link ParsingToken}
     */
    @Override
    public @NotNull Type type()
    {
        return this.type;
    }

    /**
     * @return The {@link TypeParser} for this token
     */
    @Override
    public @NotNull Parser<?> parser()
    {
        return this.parser;
    }

    /**
     * @return Retrieves the default value for this {@link ParsingToken} if this is optional, otherwise it returns null.
     */
    @Override
    public @Nullable Object defaultValue()
    {
        return this.defaultValue;
    }
}
