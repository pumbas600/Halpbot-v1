package nz.pumbas.halpbot.commands.customconstructors;


import org.dockbox.hartshorn.core.annotations.inject.Binds;
import org.dockbox.hartshorn.core.annotations.inject.Bound;
import org.dockbox.hartshorn.core.context.element.ExecutableElementContext;

import java.util.List;

import nz.pumbas.halpbot.commands.context.parsing.CommandParsingContext;
import nz.pumbas.halpbot.converters.tokens.Token;

@Binds(CustomConstructorContext.class)
public record HalpbotCustomConstructorContext(String usage,
                                              ExecutableElementContext<?> executable,
                                              CommandParsingContext parsingContext,
                                              List<Token> tokens)
    implements CustomConstructorContext
{
    @Bound
    public HalpbotCustomConstructorContext {}
}
