package nz.pumbas.commands.tokens.tokentypes;

import nz.pumbas.commands.tokens.TokenManager;
import nz.pumbas.commands.tokens.tokensyntax.InvocationContext;
import nz.pumbas.objects.Result;
import nz.pumbas.resources.Resource;

import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.annotation.Annotation;
import java.util.List;

public class MultiChoiceToken extends BuiltInTypeToken {

    private final List<String> options;

    public MultiChoiceToken(boolean isOptional, @NotNull Class<?> type, @Nullable String defaultValue,
                            @NotNull List<String> options) {
        this(isOptional, type, defaultValue, options, new Annotation[0]);
    }

    public MultiChoiceToken(boolean isOptional, @NotNull Class<?> type, @Nullable String defaultValue,
                            @NotNull List<String> options, @NotNull Annotation[] annotations) {
        this.isOptional = isOptional;
        this.type = type;
        if (!TokenManager.isBuiltInType(type))
            throw new IllegalArgumentException(
                    String.format("The type %s must be a built in type.", type));

        this.options = options; // This needs to be set before calling parseDefaultValue as it calls the matches method
        this.defaultValue = this.parseDefaultValue(defaultValue);
        this.annotations = annotations;
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
        String token = invocationToken.getNext();

        if (this.options.contains(token))
            return true;
        else {
            for (String option : this.options) {
                if (option.equalsIgnoreCase(token))
                    return true;
            }
        }
        return false;
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
        context.saveState(this);
        String token = context.getNext();

        if (this.options.contains(token))
            return super.parse(context.restoreState(this));
        else {
            for (@NonNls String option : this.options) {
                if (option.equalsIgnoreCase(token))
                    return super.parse(context.restoreState(this));
            }
        }
        return Result.of(Resource.get("halpbot.commands.match.multichoice", token, this.options));

    }

    @Override
    public String toString()
    {
        return String.format("MultiChoiceToken{isOptional=%s, type=%s, defaultValue=%s, options=%s}",
                this.isOptional, this.type.getSimpleName(), this.defaultValue, this.options);
    }
}
