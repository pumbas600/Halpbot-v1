package nz.pumbas.halpbot.actions;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.internal.utils.EncodingUtil;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import nz.pumbas.halpbot.actions.annotations.Action;
import nz.pumbas.halpbot.buttons.ButtonAction;
import nz.pumbas.halpbot.actions.annotations.Cooldown;
import nz.pumbas.halpbot.adapters.ReactionAdapter;

public class ActionCallbackBuilder {

    private String codepointEmoji;
    private Function<MessageReactionAddEvent, Object> callback;

    private Object instance;
    private Method methodCallback;
    private boolean isEphemeral;
    private Object[] parameters = new Object[0];

    private long deleteAfterDuration = 10;
    private TimeUnit deleteAfterTimeUnit = TimeUnit.MINUTES;
    private long cooldownDuration = -1;
    private TimeUnit cooldownTimeUnit = TimeUnit.SECONDS;
    private boolean removeReactionIfCoolingDown;
    private final List<String> permissions = new ArrayList<>();
    private boolean singleUse;
    private long displayDuration;

    public ActionCallbackBuilder setButtonAction(Object instance, Method method) {
        ButtonAction buttonAction = method.getAnnotation(ButtonAction.class);
        this.isEphemeral = buttonAction.isEphemeral();
        this.instance = instance;
        this.methodCallback = method;

        this.setActionFields(method);

        return this;
    }

    private void setActionFields(Method method) {
        if (method.isAnnotationPresent(Action.class)) {
            Action action = method.getAnnotation(Action.class);

            this.setDeleteAfter(action.listeningDuration(), action.listeningDurationUnit());
            this.addPermissions(action.permissions());
            this.singleUse = action.isSingleUse();
            this.displayDuration = action.displayDuration();
        }
        if (method.isAnnotationPresent(Cooldown.class)) {
            Cooldown cooldown = method.getAnnotation(Cooldown.class);
            this.setCooldown(cooldown.duration(), TimeUnit.of(cooldown.unit()));
        }
    }

    /**
     * Sets the parameters which should be passed to the action callback when invoked.
     *
     * @param parameters
     *      The parameters to pass
     *
     * @return Itself for chaining
     */
    public ActionCallbackBuilder setParameters(Object... parameters) {
        this.parameters = parameters;
        return this;
    }

    /**
     * The emoji to be used for this reaction callback. Note that the emoji should be compatable with
     * {@link Message#addReaction(String)}.
     *
     * @param emoji
     *      The emoji
     *
     * @return Itself for chaining
     */
    public ActionCallbackBuilder setEmoji(String emoji) {
        this.codepointEmoji = emoji.startsWith("U+")
            ? emoji : EncodingUtil.encodeCodepoints(emoji);

        this.codepointEmoji = ReactionAdapter.convertCodepointToValidCase(this.codepointEmoji);
        return this;
    }

    public ActionCallbackBuilder setFunction(Function<MessageReactionAddEvent, Object> callback) {
        this.callback = callback;
        return this;
    }

    public ActionCallbackBuilder setRunnable(Runnable callback) {
        this.callback = event -> { callback.run(); return null; };
        return this;
    }

    public ActionCallbackBuilder setConsumer(Consumer<MessageReactionAddEvent> callback) {
        this.callback = event -> { callback.accept(event); return null; };
        return this;
    }

    public ActionCallbackBuilder setSupplier(Supplier<Object> callback) {
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
    public ActionCallbackBuilder setDeleteAfter(long duration, TimeUnit timeUnit) {
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
    public ActionCallbackBuilder addPermissions(String... permissions) {
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
    public ActionCallbackBuilder setCooldown(long duration, TimeUnit timeUnit) {
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
    public ActionCallbackBuilder setRemoveReactionIfCoolingDown() {
        this.removeReactionIfCoolingDown = true;
        return this;
    }

    /**
     * Sets that the action can only be used once. After that first use, the callback will then be
     * automatically removed, along with all other callbacks on the same message. By default, this is false.
     *
     * @return Itself for chaining
     */
    public ActionCallbackBuilder setSingleUse() {
        this.singleUse = true;
        return this;
    }

    /**
     * Builds the {@link ReactionActionCallback} from the specified fields.
     *
     * @return The built reaction callback
     * @throws NullPointerException
     *         If no emoji or callback has been set.
     */
    public ReactionActionCallback buildReactionCallback() {
        if (null == this.codepointEmoji)
            throw new NullPointerException("You must specify an emoji for this emoji callback");
        if (null == this.callback)
            throw new NullPointerException("You must specify a callback to use when someone reacts with the emoji");
        return new ReactionActionCallback(
            this.codepointEmoji, this.callback,
            this.removeReactionIfCoolingDown,
            this.deleteAfterDuration, this.deleteAfterTimeUnit,
            this.cooldownDuration, this.cooldownTimeUnit,
            this.permissions, this.singleUse,
            this.displayDuration);
    }

    public ButtonActionCallback buildButtonCallback() {
        if (null == this.methodCallback)
            throw new NullPointerException("You must specify a method callback for this action");
        return new ButtonActionCallback(
            this.methodCallback, this.instance,
            this.isEphemeral, this.parameters,
            this.deleteAfterDuration, this.deleteAfterTimeUnit,
            this.cooldownDuration, this.cooldownTimeUnit,
            this.permissions, this.singleUse,
            this.displayDuration);
    }
}