package nz.pumbas.halpbot.actions.invokable;

import org.dockbox.hartshorn.core.annotations.Factory;
import org.dockbox.hartshorn.core.annotations.stereotype.Service;

import java.util.List;

import nz.pumbas.halpbot.buttons.ButtonContext;
import nz.pumbas.halpbot.buttons.ButtonInvocationContext;
import nz.pumbas.halpbot.commands.actioninvokable.context.CommandInvocationContext;
import nz.pumbas.halpbot.converters.tokens.ParsingToken;
import nz.pumbas.halpbot.events.HalpbotEvent;
import nz.pumbas.halpbot.events.MessageEvent;
import nz.pumbas.halpbot.mocks.MockMessageEvent;

@Service
public interface InvocationContextFactory
{
    @Factory
    CommandInvocationContext command(String content,
                                     HalpbotEvent halpbotEvent);

    default CommandInvocationContext command(String content) {
        return this.command(content, new MessageEvent(MockMessageEvent.INSTANCE));
    }

    @Factory
    ButtonInvocationContext button(HalpbotEvent halpbotEvent,
                                   List<ParsingToken> nonCommandParameterTokens,
                                   Object[] passedParameters);

    default ButtonInvocationContext button(HalpbotEvent halpbotEvent, ButtonContext buttonContext) {
        return this.button(halpbotEvent, buttonContext.nonCommandParameterTokens(), buttonContext.passedParameters());
    }

    @Factory
    SourceInvocationContext source(HalpbotEvent halpbotEvent,
                                   List<ParsingToken> nonCommandParameterTokens);
}
