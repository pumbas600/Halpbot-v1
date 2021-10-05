package nz.pumbas.halpbot.adapters;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;

import org.jetbrains.annotations.NotNull;

import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import nz.pumbas.halpbot.actions.ReactionActionCallback;
import nz.pumbas.halpbot.commands.events.MessageEvent;
import nz.pumbas.halpbot.permissions.PermissionManager;
import nz.pumbas.halpbot.utilities.ConcurrentManager;
import nz.pumbas.halpbot.utilities.ErrorManager;
import nz.pumbas.halpbot.utilities.HalpbotUtils;

public class ReactionAdapter extends HalpbotAdapter
{
    protected final ConcurrentManager concurrentManager = HalpbotUtils.context().get(ConcurrentManager.class);
    protected final Map<Long, Map<String, ReactionActionCallback>> reactionCallbacks = new ConcurrentHashMap<>();

    @Override
    public void onMessageReactionAdd(@NotNull MessageReactionAddEvent event) {
        long userId = event.getUserIdLong();
        long messageId = event.getMessageIdLong();

        if (event.getUser().isBot() || !this.reactionCallbacks.containsKey(messageId))
            return;

        // Trying to get the codepoint of a custom emoji will cause an error to be thrown
        if (!event.getReactionEmote().isEmoji())
            return;

        String emoji = convertCodepointToValidCase(event.getReactionEmote().getAsCodepoints());

        var callbacks = this.reactionCallbacks.get(messageId);
        if (!callbacks.containsKey(emoji))
            return;

        ReactionActionCallback callback = callbacks.get(emoji);
        if (!callback.hasPermission(userId)) {
            this.halpBotCore.displayMessage(
                event, "You don't have the required permission to use this reaction callback");
            return;
        }

        if (this.halpBotCore.hasCooldown(new MessageEvent(event), userId, event.getMessageId())) {
            if (callback.removeReactionIfCoolingDown()) {
                event.retrieveMessage()
                    .flatMap(m -> m.removeReaction(emoji, event.getUser()))
                    .queue();
            }
            return;
        }

        callback.invokeCallback(new MessageEvent(event))
            .present(value -> this.halpBotCore.displayMessage(event, value))
            .caught(exception -> ErrorManager.handle(event, exception));

        if (callback.isSingleUse()) {
            event.retrieveMessage()
                .queue(m -> {
                    this.reactionCallbacks.get(messageId)
                        .forEach((registeredEmoji, registeredCallback) ->
                            m.removeReaction(registeredEmoji, event.getJDA().getSelfUser())
                                .queue());
                    this.reactionCallbacks.remove(messageId);
                });
        }

        if (callback.hasCooldown()) {
            this.halpBotCore.addCooldown(
                userId,
                event.getMessageId(),
                callback.createCooldown());
        }
    }

    public static String convertCodepointToValidCase(String codepoint) {
        return "U+" + codepoint.substring(2).toUpperCase(Locale.ROOT);
    }

    public void registerCallback(Message message, ReactionActionCallback actionCallback) {
        long messageId = message.getIdLong();
        if (!this.reactionCallbacks.containsKey(messageId)) {
            Map<String, ReactionActionCallback> messageCallbacks = new ConcurrentHashMap<>();
            messageCallbacks.put(actionCallback.getCodepointEmoji(), actionCallback);
            this.reactionCallbacks.put(messageId, messageCallbacks);
        }
        else this.reactionCallbacks
                .get(messageId)
                .put(actionCallback.getCodepointEmoji(), actionCallback);

        message.addReaction(actionCallback.getCodepointEmoji())
            .queue(success -> {
                if (0 < actionCallback.getDeleteAfterDuration()) {
                    this.concurrentManager.schedule(
                        actionCallback.getDeleteAfterDuration(),
                        actionCallback.getDeleteAfterTimeUnit(),
                        () -> this.removeReactionCallbackAndEmoji(message, actionCallback));
                }
            });
    }

    private boolean removeReactionCallback(long messageId, ReactionActionCallback callback) {
        if (this.reactionCallbacks.containsKey(messageId)) {
            this.reactionCallbacks
                .get(messageId)
                .remove(callback.getCodepointEmoji());
            return true;
        }

        if (this.reactionCallbacks.get(messageId).isEmpty()) {
            this.reactionCallbacks.remove(messageId);
        }
        return false;
    }

    private void removeReactionCallbackAndEmoji(Message message, ReactionActionCallback callback) {
        if (this.removeReactionCallback(message.getIdLong(), callback)) {
            message.clearReactions(callback.getCodepointEmoji()).queue();
        }
    }

}
