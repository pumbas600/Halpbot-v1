package nz.pumbas.commands.tokens;

import org.jetbrains.annotations.NotNull;

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

    public String getPlaceHolder()
    {
        return this.placeHolder;
    }

    @Override
    public boolean isOptional()
    {
        return this.isOptional;
    }

    @Override
    public boolean matches(@NotNull String invocationToken)
    {
        return this.placeHolder.equalsIgnoreCase(invocationToken);
    }

    @Override
    public String toString()
    {
        return String.format("PlaceholderToken{isOptional=%s, placeHolder=%s}",
            this.isOptional, this.placeHolder);
    }
}
