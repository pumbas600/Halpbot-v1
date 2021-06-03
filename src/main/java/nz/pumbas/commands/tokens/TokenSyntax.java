package nz.pumbas.commands.tokens;

import java.util.regex.Pattern;

public enum TokenSyntax
{

    OPTIONAL("<[^<>]+>"),
    OBJECT("#[^#]+\\[.+\\]"),
    TYPE("#[^#]+"),
    ARRAY("\\[.*\\]"),
    MULTICHOICE("\\[[^#]+\\]");

    private final String syntax;
    private final Pattern syntaxPattern;

    TokenSyntax(String syntax) {
        this.syntax = syntax;
        this.syntaxPattern = Pattern.compile(syntax);
    }

    public boolean matches(String token) {
        return this.syntaxPattern.matcher(token).matches();
    }

    public String getSyntax() {
        return this.syntax;
    }
}
