package nz.pumbas.halpbot.actions.invokable;

import org.dockbox.hartshorn.core.annotations.Factory;
import org.dockbox.hartshorn.core.annotations.stereotype.Service;

import java.util.List;

import javax.annotation.Nullable;

import nz.pumbas.halpbot.buttons.ButtonInvocationContext;
import nz.pumbas.halpbot.commands.actioninvokable.context.CommandInvocationContext;
import nz.pumbas.halpbot.converters.tokens.ParsingToken;
import nz.pumbas.halpbot.events.HalpbotEvent;

@Service
public interface InvocationContextFactory
{
    @Factory
    CommandInvocationContext command(String content,
                                     @Nullable HalpbotEvent halpbotEvent);

    default CommandInvocationContext command(String content) {
        return this.command(content, null);
    }

    @Factory
    ButtonInvocationContext button(HalpbotEvent halpbotEvent,
                                   List<ParsingToken> nonCommandParameterTokens,
                                   Object[] passedParameters);
}
