package nz.pumbas.commands.tokens.tokentypes;

import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.annotation.Annotation;
import java.util.Optional;
import nz.pumbas.commands.tokens.TokenCommand;
import nz.pumbas.commands.tokens.TokenManager;
import nz.pumbas.commands.tokens.tokensyntax.InvocationContext;
import nz.pumbas.objects.Result;
import nz.pumbas.resources.Resource;

public class ObjectTypeToken implements ParsingToken
{
    private final boolean isOptional;
    private final Class<?> type;
    private final Object defaultValue;
    private final Annotation[] annotations;

    public ObjectTypeToken(boolean isOptional, Class<?> type, @Nullable String defaultValue) {
        this (isOptional, type, defaultValue, new Annotation[0]);
    }

    public ObjectTypeToken(boolean isOptional, Class<?> type, @Nullable String defaultValue, Annotation[] annotations) {
        this.isOptional = isOptional;
        this.type = type;
        this.defaultValue = this.parseDefaultValue(defaultValue);
        this.annotations = annotations;
    }

    /**
     * @return If this {@link CommandToken} is optional or not.
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
        Optional<String> oTypeAlias = invocationToken.getNextSurrounded("#", "[", false);
        if (oTypeAlias.isEmpty() || !oTypeAlias.get().equalsIgnoreCase(TokenManager.getTypeAlias(this.type)))
            return false;

        Optional<String> oParameters = invocationToken.getNextSurrounded("[", "]");
        if (oParameters.isPresent()) {
            InvocationContext subInvocationToken = InvocationContext.of(oParameters.get());
            subInvocationToken.saveState(this);

            for (TokenCommand tokenCommand : TokenManager.getParsedConstructors(this.getType())) {
                if (tokenCommand.matches(subInvocationToken.restoreState(this)))
                    return true;
            }
            return false;
        }
        return false;
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
     * @return The {@link Class type} of this {@link ParsingToken}
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
    @Override
    @Nullable
    public Object parseOld(@NotNull InvocationContext context)
    {
        context.getNext("[", false);
        Optional<String> oParameters = context.getNextSurrounded("[", "]");

        if (oParameters.isPresent()) {
            InvocationContext subInvocationToken = InvocationContext.of(oParameters.get());
            subInvocationToken.saveState(this);
            for (TokenCommand tokenCommand : TokenManager.getParsedConstructors(this.getType())) {
                if (tokenCommand.matches(subInvocationToken.restoreState(this))) {
                    return tokenCommand.invoke(subInvocationToken.restoreState(this), null, null)
                        .orElse(null);
                }
            }
        }
        return null;
    }

    /**
     * @return Retrieves the default value for this {@link ParsingToken} if this is optional, otherwise it returns null.
     */
    @Override
    @Nullable
    public Object getDefaultValue()
    {
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
        @NonNls String expectedTypeAlias = TokenManager.getTypeAlias(this.type);
        Optional<String> oTypeAlias = context.getNext("[", false);

        if (oTypeAlias.isEmpty())
            return Result.of(Resource.get("halpbot.commands.match.object.missingbrackets",expectedTypeAlias));

        if (!oTypeAlias.get().equalsIgnoreCase(expectedTypeAlias))
            return Result.of(Resource.get("halpbot.commands.match.object.alias",
                oTypeAlias.get(), expectedTypeAlias));

        Optional<String> oParameters = context.getNextSurrounded("[", "]");
        if (oParameters.isEmpty())
            return Result.of(Resource.get("halpbot.commands.match.object.missingclosingbracket",expectedTypeAlias));

        InvocationContext parameterContext = InvocationContext.of(oParameters.get());
        parameterContext.saveState(this);

        Result<Object> result = Result.empty();
        for (TokenCommand tokenCommand : TokenManager.getParsedConstructors(this.getType())) {
            result = tokenCommand.parse(parameterContext.restoreState(this))
                .orIfEmpty(Result.of(Resource.get("halpbot.commands.match.object.creationerror",
                    expectedTypeAlias, oParameters.get())));
            if (result.hasValue())
                break;
        }
        return result;
    }

    @Override
    public String toString() {
        return String.format("ObjectTypeToken{isOptional=%s, type=%s, defaultValue=%s}",
                this.isOptional, this.type.getSimpleName(), this.defaultValue);
    }
}
