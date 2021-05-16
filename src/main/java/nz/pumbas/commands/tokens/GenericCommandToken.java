package nz.pumbas.commands.tokens;

import org.jetbrains.annotations.NotNull;

public class GenericCommandToken implements CommandToken {

    protected final String token;
    protected final TokenSyntax tokenSyntax;
    protected final Class<?> type;
    protected final boolean isOptional;

    public GenericCommandToken(String token, TokenSyntax tokenSyntax, Class<?> type, boolean isOptional) {
        this.token = token;
        this.tokenSyntax = tokenSyntax;
        this.type = type;
        this.isOptional = isOptional;
    }

    public String getToken() {
        return this.token;
    }

    public TokenSyntax getTokenType() {
        return this.tokenSyntax;
    }

    public Class<?> getType() {
        return this.type;
    }

    @Override
    public boolean isOptional() {
        return this.isOptional;
    }

    @Override
    public boolean matches(@NotNull String invocationToken)
    {
        return false;
    }

    @Override
    public String toString() {
        return String.format("CommandToken{token=%s, tokenType=%s, type=%s, isOptional=%s}",
                this.token, this.tokenSyntax, this.type.getSimpleName(), this.isOptional);
    }
}
