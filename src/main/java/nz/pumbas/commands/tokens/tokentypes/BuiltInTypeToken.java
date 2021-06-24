package nz.pumbas.commands.tokens.tokentypes;

import org.jetbrains.annotations.NotNull;

import nz.pumbas.commands.tokens.TokenManager;
import nz.pumbas.commands.tokens.tokensyntax.InvocationContext;
import nz.pumbas.objects.Result;
import nz.pumbas.resources.Resource;
import nz.pumbas.utilities.Reflect;

import org.jetbrains.annotations.Nullable;

import java.lang.annotation.Annotation;

public class BuiltInTypeToken implements ParsingToken
{
    protected boolean isOptional;
    protected Class<?> type;
    protected Object defaultValue;
    protected Annotation[] annotations;

    public BuiltInTypeToken(boolean isOptional, Class<?> type, @Nullable String defaultValue) {
        this(isOptional, type, defaultValue, new Annotation[0]);
    }

    public BuiltInTypeToken(boolean isOptional, Class<?> type, @Nullable String defaultValue, Annotation[] annotations) {
        this.isOptional = isOptional;
        if (!TokenManager.isBuiltInType(type))
            throw new IllegalArgumentException(
                    String.format("The type %s must be a built in type.", type));

        this.type = type;
        this.defaultValue = this.parseDefaultValue(defaultValue);
        this.annotations = annotations;
    }

    protected BuiltInTypeToken() { }

    /**
     * @return If this {@link CommandToken} is optional or not
     */
    @Override
    public boolean isOptional()
    {
        return this.isOptional;
    }

    /**
     * Returns if the passed in @link InvocationTokenInfo invocation token} matches this {@link CommandToken}.
     *
     * @param invocationToken
     *     The {@link InvocationContext invocation token} containing the invoking information
     *
     * @return If the {@link InvocationContext invocation token} matches this {@link CommandToken}
     */
    @Override
    public boolean matchesOld(@NotNull InvocationContext invocationToken)
    {
        if (this.type.isEnum()) {
            return Reflect.isEnumValue(this.type, invocationToken.getNext().toUpperCase());
        }

        return Reflect.matches(invocationToken.getNext(), this.type);
    }

    /**
     * @return The {@link Annotation} annotations on this {@link ParsingToken}
     */
    @Override
    public Annotation[] getAnnotations()
    {
        return this.annotations;
    }

    /**
     * @return The required {@link Class type} of this {@link ParsingToken}
     */
    @Override
    public Class<?> getType()
    {
        return this.type;
    }

    /**
     * Parses an {@link InvocationContext invocation token} to the type of the {@link ParsingToken}.
     *
     * @param context
     *     The {@link InvocationContext invocation token} to be parsed into the type of the {@link ParsingToken}
     *
     * @return An {@link Object} parsing the {@link InvocationContext invocation token} to the correct type
     */
    @SuppressWarnings("unchecked")
    @Override
    public Object parseOld(@NotNull InvocationContext context)
    {
        String token = context.getNext();
        if (this.type.isEnum()) {
            return Enum.valueOf((Class<? extends Enum>)this.type, token.toUpperCase());
        }
        return Reflect.parse(token, this.type);
    }

    /**
     * @return Retrieves the default value for this {@link ParsingToken} if this is optional, otherwise it returns null.
     */
    @Override
    public @Nullable Object getDefaultValue() {
        return this.defaultValue;
    }

    /**
     * Parses the context into the type of this {@link ParsingToken}. If the context doesn't match, the
     * {@link Result} will contain a {@link Resource} explaing why.
     *
     * @param context
     *     The {@link InvocationContext}
     *
     * @return An {@link Result} containing the parsed context
     */
    @Override
    public Result<Object> parse(@NotNull InvocationContext context)
    {
        String token = context.getNext();
        if (this.getType().isEnum()) {
            return Result.of(Reflect.parseEnumValue(this.type, token.toUpperCase()),
                Resource.get("halpbot.commands.match.enum", token, this.getType().getSimpleName()))
                .map(Object.class::cast);
        }

        return Reflect.matches(token, this.getType())
            ? Result.of(Reflect.parse(token, this.getType()))
            : Result.of(Resource.get("halpbot.commands.match.builtintype",
            token, this.getType().getSimpleName()));
    }

    @Override
    public String toString()
    {
        return String.format("BuiltInTypeToken{isOptional=%s, type=%s, defaultValue=%s}",
            this.isOptional, this.getType().getSimpleName(), this.defaultValue);
    }
}
