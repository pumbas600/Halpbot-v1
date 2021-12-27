package nz.pumbas.halpbot.actions;

import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;

import org.dockbox.hartshorn.core.domain.Exceptional;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import nz.pumbas.halpbot.actions.cooldowns.Coolable;
import nz.pumbas.halpbot.actions.cooldowns.CooldownTimer;
import nz.pumbas.halpbot.events.HalpbotEvent;

public class ReactionActionCallback extends AbstractActionCallback
{
    protected final String codepointEmoji;
    protected final Function<MessageReactionAddEvent, Object> callback;
    protected final boolean removeReactionIfCoolingDown;

    protected ReactionActionCallback(
        String codepointEmoji,
        Function<MessageReactionAddEvent, Object> callback,
        boolean removeReactionIfCoolingDown,
        long deleteAfterDuration, TimeUnit deleteAfterTimeUnit,
        long cooldownDuration, TimeUnit cooldownTimeUnit,
        List<String> permissions, boolean singleUse,
        long displayDuration)
    {
        super(deleteAfterDuration,
            deleteAfterTimeUnit,
            cooldownDuration,
            cooldownTimeUnit,
            permissions,
            singleUse,
            displayDuration);
        this.codepointEmoji = codepointEmoji;
        this.callback = callback;
        this.removeReactionIfCoolingDown = removeReactionIfCoolingDown;
    }

    public boolean removeReactionIfCoolingDown() {
        return this.removeReactionIfCoolingDown;
    }

    @Override
    public Exceptional<Object> invokeCallback(HalpbotEvent event) {
        //Use of supplier means that any exception thrown in the callback will be automatically caught.
        return Exceptional.of(() -> this.callback.apply(event.event(MessageReactionAddEvent.class)));
    }

    public String getCodepointEmoji() {
        return this.codepointEmoji;
    }

    @Override
    public @Nullable CooldownTimer cooldownTimer() {
        return null;
    }

    @Override
    public Coolable cooldownTimer(CooldownTimer cooldownTimer) {
        return null;
    }

    @Override
    public long cooldownDurationMs() {
        return 0;
    }
}
