package net.pumbas.halpbot.converters.invocation;

import net.pumbas.halpbot.actions.invokable.SourceInvocationContext;
import net.pumbas.halpbot.converters.tokens.ParsingToken;
import net.pumbas.halpbot.events.HalpbotEvent;

import org.dockbox.hartshorn.component.Service;
import org.dockbox.hartshorn.component.factory.Factory;

import java.util.List;

@Service
public interface SourceInvocationContextFactory {

    @Factory
    SourceInvocationContext create(HalpbotEvent halpbotEvent,
                                   List<ParsingToken> nonCommandParameterTokens);
}
