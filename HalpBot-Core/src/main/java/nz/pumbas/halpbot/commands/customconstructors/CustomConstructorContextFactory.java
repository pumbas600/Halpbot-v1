package nz.pumbas.halpbot.commands.customconstructors;

import org.dockbox.hartshorn.core.annotations.Factory;
import org.dockbox.hartshorn.core.annotations.service.Service;
import org.dockbox.hartshorn.core.context.element.ExecutableElementContext;

import nz.pumbas.halpbot.commands.commandmethods.parsing.ParsingContext;

@Service
public interface CustomConstructorContextFactory
{
    @Factory
    CustomConstructorContext create(String usage,
                                    ExecutableElementContext<?> executable,
                                    ParsingContext parsingContext);
}
