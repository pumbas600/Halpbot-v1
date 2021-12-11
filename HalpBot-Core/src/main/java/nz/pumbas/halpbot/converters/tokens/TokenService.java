package nz.pumbas.halpbot.converters.tokens;

import org.dockbox.hartshorn.core.context.ContextCarrier;
import org.dockbox.hartshorn.core.context.element.ExecutableElementContext;

import java.util.List;

public interface TokenService extends ContextCarrier
{
    List<Token> tokens(ExecutableElementContext<?> executableContext);
}
