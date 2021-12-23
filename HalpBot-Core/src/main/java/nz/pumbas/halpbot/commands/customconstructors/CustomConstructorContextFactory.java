package nz.pumbas.halpbot.commands.customconstructors;

import org.dockbox.hartshorn.core.annotations.Factory;
import org.dockbox.hartshorn.core.annotations.service.Service;
import org.dockbox.hartshorn.core.context.element.ExecutableElementContext;

import java.util.List;

import nz.pumbas.halpbot.commands.context.parsing.ParsingContext;
import nz.pumbas.halpbot.converters.tokens.Token;

@Service
public interface CustomConstructorContextFactory
{
    @Factory
    CustomConstructorContext create(String usage,
                                    ExecutableElementContext<?> executable,
                                    ParsingContext parsingContext,
                                    List<Token> tokens);
}
