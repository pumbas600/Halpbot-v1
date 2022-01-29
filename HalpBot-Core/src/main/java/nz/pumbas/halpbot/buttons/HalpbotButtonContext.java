package nz.pumbas.halpbot.buttons;

import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.interactions.components.ActionRow;

import org.dockbox.hartshorn.core.annotations.inject.ComponentBinding;
import org.dockbox.hartshorn.core.annotations.inject.Bound;

import java.time.Duration;
import java.util.List;
import java.util.function.Function;

import lombok.AllArgsConstructor;
import lombok.Getter;
import nz.pumbas.halpbot.actions.invokable.ActionInvokable;
import nz.pumbas.halpbot.converters.tokens.ParsingToken;


@Getter
@ComponentBinding(ButtonContext.class)
@AllArgsConstructor(onConstructor_ = @Bound)
public class HalpbotButtonContext implements ButtonContext
{
    private final String id;
    private final boolean isEphemeral;
    private final Duration displayDuration;
    private final ActionInvokable<ButtonInvocationContext> actionInvokable;
    private final Object[] passedParameters;
    private final List<ParsingToken> nonCommandParameterTokens;
    private int afterUsages;
    private final Duration after;
    private final Function<ButtonClickEvent, List<ActionRow>> afterRemoval;


    @Override
    public void deductUsage() {
        this.afterUsages--;
    }
}
