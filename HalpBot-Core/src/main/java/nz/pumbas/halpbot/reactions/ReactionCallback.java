package nz.pumbas.halpbot.reactions;

import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.internal.utils.EncodingUtil;

import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import nz.pumbas.halpbot.objects.Exceptional;

public final class ReactionCallback
{
    private final String codepointEmoji;
    private Function<MessageReactionAddEvent, Object> callback;
    private long deleteAfterDuration = 10;
    private TimeUnit timeUnit = TimeUnit.MINUTES;

    private ReactionCallback(String codepointEmoji) {
        this.codepointEmoji = codepointEmoji;
    }

    public ReactionCallback callback(Runnable callback) {
        this.callback = event -> { callback.run(); return null; };
        return this;
    }

    public ReactionCallback callback(Consumer<MessageReactionAddEvent> callback) {
        this.callback = event -> { callback.accept(event); return null; };
        return this;
    }

    public ReactionCallback callback(Supplier<Object> callback) {
        this.callback = event -> { return callback.get(); };
        return this;
    }

    public ReactionCallback callback(Function<MessageReactionAddEvent, Object> callback) {
        this.callback = callback;
        return this;
    }

    public ReactionCallback deleteAfter(long duration, TimeUnit timeUnit) {
        this.deleteAfterDuration = duration;
        this.timeUnit = timeUnit;
        return this;
    }

    public static ReactionCallback of(String emoji) {
        return new ReactionCallback(emoji);
    }

    public String getCodepointEmoji() {
        return this.codepointEmoji;
    }

    public long getDeleteAfterDuration() {
        return this.deleteAfterDuration;
    }

    public TimeUnit getTimeUnit() {
        return this.timeUnit;
    }

    public Exceptional<Object> invokeCallback(MessageReactionAddEvent event) {
        //Use of supplier means that any exception thrown in the callback will be automatically caught.
        return Exceptional.of(() -> this.callback.apply(event));
    }
}
