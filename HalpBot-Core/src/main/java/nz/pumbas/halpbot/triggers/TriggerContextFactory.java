package nz.pumbas.halpbot.triggers;

import org.dockbox.hartshorn.core.annotations.Factory;
import org.dockbox.hartshorn.core.annotations.stereotype.Service;

import java.time.Duration;
import java.util.List;

import nz.pumbas.halpbot.actions.invokable.ActionInvokable;
import nz.pumbas.halpbot.actions.invokable.SourceInvocationContext;
import nz.pumbas.halpbot.converters.tokens.ParsingToken;
import nz.pumbas.halpbot.permissions.Merger;

@Service
public interface TriggerContextFactory
{
    @Factory
    TriggerContext create(List<String> triggers,
                          String description,
                          TriggerStrategy strategy,
                          Merger merger,
                          List<ParsingToken> nonCommandParameterTokens,
                          ActionInvokable<SourceInvocationContext> actionInvokable,
                          Duration displayDuration,
                          boolean isEphemeral);
}
