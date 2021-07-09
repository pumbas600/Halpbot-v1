package nz.pumbas.commands.tokens.tokentypes;

import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import nz.pumbas.commands.tokens.context.InvocationContext;
import nz.pumbas.objects.Result;
import nz.pumbas.resources.Resource;

/**
 * A placeholder token. These are usually when you add flavouring text in commands. For example, in the command:
 * {@code <my name is> #String}, the text 'my name is' are {@link PlaceholderToken placeholder tokens}.
 */
public class PlaceholderToken implements CommandToken
{
    private final boolean isOptional;
    private final String placeHolder;

    public PlaceholderToken(boolean isOptional, String placeHolder) {
        this.isOptional = isOptional;
        this.placeHolder = placeHolder;
    }

    /**
     * @return The placeholder text
     */
    public String getPlaceHolder()
    {
        return this.placeHolder;
    }

    /**
     * @return If this {@link CommandToken} is optional or not
     */
    @Override
    public boolean isOptional()
    {
        return this.isOptional;
    }

    /**
     * Returns if the passed in {@link InvocationContext context} matches this {@link CommandToken}.
     *
     * @param context
     *     The {@link InvocationContext context}
     *
     * @return If the {@link InvocationContext context} matches this {@link CommandToken}
     */
    public Result<Boolean> matches(@NotNull InvocationContext context)
    {
        @NonNls String token = context.getNext();
        boolean matches = this.placeHolder.equalsIgnoreCase(token);
        return matches ? Result.of(matches)
            : Result.of(matches, Resource.get("halpbot.commands.match.placeholder", token, this.placeHolder));
    }

    /**
     * @return A {@link String} representation of this token in the format {@code PlaceholderToken{isOptional=%s,
     *     placeHolder=%s}}
     */
    @Override
    public String toString()
    {
        return String.format("PlaceholderToken{isOptional=%s, placeHolder=%s}",
            this.isOptional, this.placeHolder);
    }
}
