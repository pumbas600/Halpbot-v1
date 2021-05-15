package nz.pumbas.commands.tokens;

public enum TokenType {

    OPTIONAL("<[^<>]+>"),
    OBJECT("#[^#]+\\[.+\\]"),
    TYPE("#[^#]+"),
    TEXT("[^\\s]+");

    private final String syntax;

    TokenType(String syntax) {
        this.syntax = syntax;
    }

    public String getSyntax() {
        return this.syntax;
    }
}
