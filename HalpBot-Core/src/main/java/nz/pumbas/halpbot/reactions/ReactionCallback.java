package nz.pumbas.halpbot.reactions;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.internal.utils.EncodingUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import nz.pumbas.halpbot.adapters.ReactionAdapter;
import nz.pumbas.halpbot.commands.cooldowns.CooldownAction;
import nz.pumbas.halpbot.objects.Exceptional;

public final class ReactionCallback implements CooldownAction
{
    private final String codepointEmoji;
    private final Function<MessageReactionAddEvent, Object> callback;
    private final long deleteAfterDuration;
    private final TimeUnit deleteAfterTimeUnit;
    private final long cooldownDuration;
    private final TimeUnit cooldownTimeUnit;
    private final boolean removeReactionIfCoolingDown;
    private final List<String> permissions;
    private final boolean singleUse;

    private ReactionCallback(
        String codepointEmoji,
        Function<MessageReactionAddEvent, Object> callback,
        long deleteAfterDuration, TimeUnit deleteAfterTimeUnit,
        long cooldownDuration, TimeUnit cooldownTimeUnit,
        boolean removeReactionIfCoolingDown,
        List<String> permissions,
        boolean singleUse)
    {
        this.codepointEmoji = codepointEmoji;
        this.callback = callback;
        this.deleteAfterDuration = deleteAfterDuration;
        this.deleteAfterTimeUnit = deleteAfterTimeUnit;
        this.cooldownDuration = cooldownDuration;
        this.cooldownTimeUnit = cooldownTimeUnit;
        this.removeReactionIfCoolingDown = removeReactionIfCoolingDown;
        this.permissions = permissions;
        this.singleUse = singleUse;
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

    public boolean removeReactionIfCoolingDown() {
        return this.removeReactionIfCoolingDown;
    }

    public List<String> getPermissions() {
        return this.permissions;
    }

    public boolean isSingleUse() {
        return this.singleUse;
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
        private boolean removeReactionIfCoolingDown;
        private final List<String> permissions = new ArrayList<>();
        private boolean singleUse;

        /**
         * The emoji to be used for this reaction callback. Note that the emoji should be compatable with
         * {@link Message#addReaction(String)}.
         *
         * @param emoji
         *      The emoji
         *
         * @return Itself for chaining
         */
        public ReactionCallbackBuilder setEmoji(String emoji) {
            this.codepointEmoji = emoji.startsWith("U+")
                ? emoji : EncodingUtil.encodeCodepoints(emoji);

            this.codepointEmoji = ReactionAdapter.convertCodepointToValidCase(this.codepointEmoji);
            return this;
        }

        public ReactionCallbackBuilder setFunction(Function<MessageReactionAddEvent, Object> callback) {
            this.callback = callback;
            return this;
        }

        public ReactionCallbackBuilder setRunnable(Runnable callback) {
            this.callback = event -> { callback.run(); return null; };
            return this;
        }

        public ReactionCallbackBuilder setConsumer(Consumer<MessageReactionAddEvent> callback) {
            this.callback = event -> { callback.accept(event); return null; };
            return this;
        }

        public ReactionCallbackBuilder setSupplier(Supplier<Object> callback) {
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
        public ReactionCallbackBuilder setDeleteAfter(long duration, TimeUnit timeUnit) {
            this.deleteAfterDuration = duration;
            this.deleteAfterTimeUnit = timeUnit;
            return this;
        }

        /**
         * Adds the permissions a user must have to use this callback.
         *
         * @param permissions
         *      The permissions that a user requires
         *
         * @return Itself for chaining
         */
        public ReactionCallbackBuilder addPermissions(String... permissions) {
            this.permissions.addAll(List.of(permissions));
            return this;
        }

        /**
         * Specifies how long a user must wait in between triggering this callback. Any attempts to trigger the
         * callback before the cooldown has finished will be ignored. Specify a negative duration to indicate that
         * there is no cooldown for this callback. By default, there is no cooldown. Note: That the time unit cannot
         * be smaller than milliseconds. Trying to specify a time in microseconds or nanoseconds will result in an
         * error.
         *
         * @param duration
         *      The duration
         * @param timeUnit
         *      The time unit that the duration is in
         *
         * @return Itself for chaining
         * @throws IllegalArgumentException
         *         If the specified time unit is microseconds or nanoseconds
         */
        public ReactionCallbackBuilder setCooldown(long duration, TimeUnit timeUnit) {
            if (TimeUnit.MICROSECONDS == timeUnit || TimeUnit.NANOSECONDS == timeUnit)
                throw new IllegalArgumentException(
                    "The time unit for a cooldown cannot be microseconds or nanoseconds");

            this.cooldownDuration = duration;
            this.cooldownTimeUnit = timeUnit;
            return this;
        }

        /**
         * Sets that the reaction should be removed if added while the callback is still cooling down for that user.
         * By default, this is false.
         *
         * @return Itself for chaining
         */
        public ReactionCallbackBuilder setRemoveReactionIfCoolingDown() {
            this.removeReactionIfCoolingDown = true;
            return this;
        }

        /**
         * Sets that the reaction can only be used once. After that first use, the callback will then be
         * automatically removed, along with all other callbacks on the same message. By default, this is false.
         *
         * @return Itself for chaining
         */
        public ReactionCallbackBuilder setSingleUse() {
            this.singleUse = true;
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
                this.cooldownDuration, this.cooldownTimeUnit,
                this.removeReactionIfCoolingDown,
                this.permissions, this.singleUse);
        }
    }
}
