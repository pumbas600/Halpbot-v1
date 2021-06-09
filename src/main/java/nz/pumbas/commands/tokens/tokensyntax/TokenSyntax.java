package nz.pumbas.commands.tokens.tokensyntax;

import java.util.regex.Pattern;

public enum TokenSyntax
{
    //TODO: Either remove these or move them to TokenSyntaxDefinitions class

    OPTIONAL("<[^<>]+>"),
    TYPE("#[^#]+"),
    MULTICHOICE("\\[[^#]+\\]");

    private final Pattern syntaxPattern;

    TokenSyntax(String syntax) {
        this.syntaxPattern = Pattern.compile(syntax);
    }

    public boolean matches(String token) {
        return this.syntaxPattern.matcher(token).matches();
    }

    public Pattern getSyntaxPattern()
    {
        return this.syntaxPattern;
    }

}
