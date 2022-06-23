package net.pumbas.halpbot.buttons;

import net.pumbas.halpbot.converters.tokens.ParsingToken;
import net.pumbas.halpbot.events.HalpbotEvent;

import org.dockbox.hartshorn.component.Service;
import org.dockbox.hartshorn.component.condition.RequiresActivator;
import org.dockbox.hartshorn.component.factory.Factory;

import java.util.List;

@Service
@RequiresActivator(UseButtons.class)
public interface ButtonInvocationContextFactory {

    default ButtonInvocationContext create(final HalpbotEvent halpbotEvent, final ButtonContext buttonContext) {
        return this.create(halpbotEvent, buttonContext.nonCommandParameterTokens(), buttonContext.passedParameters());
    }

    @Factory
    ButtonInvocationContext create(HalpbotEvent halpbotEvent,
                                   List<ParsingToken> nonCommandParameterTokens,
                                   Object[] passedParameters);
}
