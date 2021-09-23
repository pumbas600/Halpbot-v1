package nz.pumbas.halpbot.reactions;

import net.dv8tion.jda.api.entities.Message;
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
    private final Function<MessageReactionAddEvent, Object> callback;
    private final long deleteAfterDuration;
    private final TimeUnit deleteAfterTimeUnit;
    private final long cooldownDuration;
    private final TimeUnit cooldownTimeUnit;

    private ReactionCallback(
        String codepointEmoji,
        Function<MessageReactionAddEvent, Object> callback,
        long deleteAfterDuration, TimeUnit deleteAfterTimeUnit,
        long cooldownDuration, TimeUnit cooldownTimeUnit)
    {
        this.codepointEmoji = codepointEmoji;
        this.callback = callback;
        this.deleteAfterDuration = deleteAfterDuration;
        this.deleteAfterTimeUnit = deleteAfterTimeUnit;
        this.cooldownDuration = cooldownDuration;
        this.cooldownTimeUnit = cooldownTimeUnit;
    }

    public String getCodepointEmoji() {
        return this.codepointEmoji;
    }

    public long getDeleteAfterDuration() {
        return this.deleteAfterDuration;
    }

    public TimeUnit getDeleteAfterTimeUnit() {
        return this.deleteAfterTimeUnit;
    }

    public long getCooldownDuration() {
        return this.cooldownDuration;
    }

    public TimeUnit getCooldownTimeUnit() {
        return this.cooldownTimeUnit;
    }

    public Exceptional<Object> invokeCallback(MessageReactionAddEvent event) {
        //Use of supplier means that any exception thrown in the callback will be automatically caught.
        return Exceptional.of(() -> this.callback.apply(event));
    }

    public static ReactionCallbackBuilder builder() {
        return new ReactionCallbackBuilder();
    }

    public static class ReactionCallbackBuilder {

        private String codepointEmoji;
        private Function<MessageReactionAddEvent, Object> callback;
        private long deleteAfterDuration = 10;
        private TimeUnit deleteAfterTimeUnit = TimeUnit.MINUTES;
        private long cooldownDuration = -1;
        private TimeUnit cooldownTimeUnit = TimeUnit.SECONDS;

        /**
         * The emoji to be used for this reaction callback. Note that the emoji should be compatable with
         * {@link Message#addReaction(String)}.
         *
         * @param emoji
         *      The emoji
         *
         * @return Itself for chaining
         */
        public ReactionCallbackBuilder emoji(String emoji) {
            this.codepointEmoji = emoji.startsWith("U+")
                ? emoji : EncodingUtil.encodeCodepoints(emoji);

            return this;
        }

        public ReactionCallbackBuilder callback(Function<MessageReactionAddEvent, Object> callback) {
            this.callback = callback;
            return this;
        }

        public ReactionCallbackBuilder callback(Runnable callback) {
            this.callback = event -> { callback.run(); return null; };
            return this;
        }

        public ReactionCallbackBuilder callback(Consumer<MessageReactionAddEvent> callback) {
            this.callback = event -> { callback.accept(event); return null; };
            return this;
        }

        public ReactionCallbackBuilder callback(Supplier<Object> callback) {
            this.callback = event ->  callback.get() ;
            return this;
        }

        /**
         * Specifies how long that this callback will be registered for. After the specified time it will be removed.
         * Specify a negative duration to indicate that this should never be removed. By default, this is 10 minutes.
         *
         * @param duration
         *      The duration
         * @param timeUnit
         *      The time unit that the duration is in
         *
         * @return Itself for chaining
         */
        public ReactionCallbackBuilder deleteAfter(long duration, TimeUnit timeUnit) {
            this.deleteAfterDuration = duration;
            this.deleteAfterTimeUnit = timeUnit;
            return this;
        }

        /**
         * Specifies how long a user must wait in between triggering this callback. Any attempts to trigger the
         * callback before the cooldown has finished will be ignored. Specify a negative duration to indicate that
         * there is no cooldown for this callback. By default, there is no cooldown.
         *
         * @param duration
         *      The duration
         * @param timeUnit
         *      The time unit that the duration is in
         *
         * @return Itself for chaining
         */
        public ReactionCallbackBuilder cooldown(long duration, TimeUnit timeUnit) {
            this.cooldownDuration = duration;
            this.cooldownTimeUnit = timeUnit;
            return this;
        }

        /**
         * Builds the {@link ReactionCallback} from the specified fields.
         *
         * @return The built reaction callback
         * @throws NullPointerException
         *         If no emoji or callback has been set.
         */
        public ReactionCallback build() {
            if (null == this.codepointEmoji)
                throw new NullPointerException("You must specify an emoji for this emoji callback");
            if (null == this.callback)
                throw new NullPointerException("You must specify a callback to use when someone reacts with the emoji");
            return new ReactionCallback(
                this.codepointEmoji, this.callback,
                this.deleteAfterDuration, this.deleteAfterTimeUnit,
                this.cooldownDuration, this.cooldownTimeUnit);
        }
    }
}
