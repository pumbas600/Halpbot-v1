package nz.pumbas.commands.tokens;

import nz.pumbas.commands.annotations.Command;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class MultiChoiceToken extends BuiltInTypeToken {

    private final List<String> options;

    public MultiChoiceToken(boolean isOptional, Class<?> type, @Nullable String defaultValue, List<String> options) {
        this.isOptional = isOptional;
        this.type = type;
        if (!TokenManager.isBuiltInType(type))
            throw new IllegalArgumentException(
                    String.format("The type %s must be a built in type.", type));

        this.options = options; // This needs to be set before calling parseDefaultValue as it calls the matches method
        this.defaultValue = this.parseDefaultValue(defaultValue);
    }

    /**
     * Returns if the passed in {@link String invocation token} matches this {@link CommandToken}.
     *
     * @param invocationToken
     *      An individual element in the invocation of an {@link Command}
     *
     * @return If the {@link String invocation token} matches this {@link CommandToken}
     */
    @Override
    public boolean matches(@NotNull String invocationToken) {
        boolean contained = this.options.contains(invocationToken);
        if (contained)
            return true;
        else {
            for (String option : this.options) {
                if (option.equalsIgnoreCase(invocationToken))
                    return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return String.format("MultiChoiceToken{isOptional=%s, type=%s, defaultValue=%s, options=%s}",
                this.isOptional, this.type.getSimpleName(), this.defaultValue, this.options);
    }
}
