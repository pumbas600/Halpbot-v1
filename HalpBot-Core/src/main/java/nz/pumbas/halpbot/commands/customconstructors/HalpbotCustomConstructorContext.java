package nz.pumbas.halpbot.commands.customconstructors;


import org.dockbox.hartshorn.core.annotations.inject.Bound;
import org.dockbox.hartshorn.core.context.element.ExecutableElementContext;

import nz.pumbas.halpbot.commands.commandmethods.parsing.ParsingContext;

public record HalpbotCustomConstructorContext(String usage,
                                              ExecutableElementContext<?> executable,
                                              ParsingContext parsingContext)
    implements CustomConstructorContext
{
    @Bound
    public HalpbotCustomConstructorContext {}
}
