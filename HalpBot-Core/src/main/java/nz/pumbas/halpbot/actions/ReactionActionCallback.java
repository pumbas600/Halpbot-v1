package nz.pumbas.halpbot.actions;

import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import nz.pumbas.halpbot.commands.events.HalpbotEvent;
import nz.pumbas.halpbot.objects.Exceptional;

public class ReactionActionCallback extends AbstractActionCallback
{
    protected final String codepointEmoji;
    protected final Function<MessageReactionAddEvent, Object> callback;
    protected final boolean removeReactionIfCoolingDown;

    protected ReactionActionCallback(
        String codepointEmoji,
        Function<MessageReactionAddEvent, Object> callback,
        boolean removeReactionIfCoolingDown,
        long deleteAfterDuration,
        TimeUnit deleteAfterTimeUnit,
        long cooldownDuration,
        TimeUnit cooldownTimeUnit,
        List<String> permissions,
        boolean singleUse)
    {
        super(deleteAfterDuration, deleteAfterTimeUnit, cooldownDuration, cooldownTimeUnit, permissions, singleUse);
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
        return Exceptional.of(() -> this.callback.apply(event.getEvent(MessageReactionAddEvent.class)));
    }

    public String getCodepointEmoji() {
        return this.codepointEmoji;
    }

}
