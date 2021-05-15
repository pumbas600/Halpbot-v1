package nz.pumbas.commands.tokens;

public class CommandToken {

    protected final TokenType tokenType;
    protected final Class<?> type;
    protected final boolean isOptional;
    protected final String token;

    public CommandToken(String token, TokenType tokenType, Class<?> type, boolean isOptional) {
        this.token = token;
        this.tokenType = tokenType;
        this.type = type;
        this.isOptional = isOptional;
    }

    public String getToken() {
        return this.token;
    }

    public TokenType getTokenType() {
        return this.tokenType;
    }

    public Class<?> getType() {
        return this.type;
    }

    public boolean isOptional() {
        return this.isOptional;
    }

    @Override
    public String toString() {
        return String.format("CommandToken{token=%s, tokenType=%s, type=%s, isOptional=%s}",
                this.token, this.tokenType, this.type.getSimpleName(), this.isOptional);
    }
}
