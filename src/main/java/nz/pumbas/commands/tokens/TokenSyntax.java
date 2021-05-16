package nz.pumbas.commands.tokens;

public enum TokenSyntax
{

    OPTIONAL("<[^<>]+>"),
    OBJECT("#[^#]+\\[.+\\]"),
    TYPE("#[^#]+");

    private final String syntax;

    TokenSyntax(String syntax) {
        this.syntax = syntax;
    }

    public String getSyntax() {
        return this.syntax;
    }
}
