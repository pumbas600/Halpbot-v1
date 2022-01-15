package nz.pumbas.halpbot.triggers;

import org.dockbox.hartshorn.core.annotations.inject.Binds;
import org.dockbox.hartshorn.core.annotations.inject.Bound;

import java.time.Duration;
import java.util.List;

import nz.pumbas.halpbot.actions.invokable.ActionInvokable;
import nz.pumbas.halpbot.actions.invokable.SourceInvocationContext;
import nz.pumbas.halpbot.converters.tokens.ParsingToken;
import nz.pumbas.halpbot.utilities.Require;

@Binds(TriggerContext.class)
public record HalpbotTriggerContext(List<String> triggers,
                                    String description,
                                    TriggerStrategy strategy,
                                    Require require,
                                    List<ParsingToken> nonCommandParameterTokens,
                                    ActionInvokable<SourceInvocationContext> actionInvokable,
                                    Duration displayDuration,
                                    boolean isEphemeral)
    implements TriggerContext
{
    @Bound
    public HalpbotTriggerContext { }
}
