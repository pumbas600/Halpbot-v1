package nz.pumbas.halpbot.adapters;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;

import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import nz.pumbas.halpbot.reactions.ReactionCallback;
import nz.pumbas.halpbot.utilities.ConcurrentManager;
import nz.pumbas.halpbot.utilities.ErrorManager;
import nz.pumbas.halpbot.utilities.HalpbotUtils;

public class ReactionAdapter extends HalpbotAdapter
{
    protected final ConcurrentManager concurrentManager = HalpbotUtils.context().get(ConcurrentManager.class);
    protected final Map<Long, Map<String, ReactionCallback>> reactionCallbacks = new ConcurrentHashMap<>();

    @Override
    public void onMessageReactionAdd(@NotNull MessageReactionAddEvent event) {
        long userId = event.getUserIdLong();
        long messageId = event.getMessageIdLong();

        if (event.getUser().isBot() || !this.reactionCallbacks.containsKey(messageId))
            return;

        var callbacks = this.reactionCallbacks.get(messageId);
        if (!callbacks.containsKey(event.getReactionEmote().getAsCodepoints()))
            return;

        if (this.halpBotCore.hasCooldown(event, userId, messageId))
            return;

        ReactionCallback callback = callbacks.get(event.getReactionEmote().getAsCodepoints());
        callback.invokeCallback(event)
            .present(value -> this.halpBotCore.displayMessage(event, value))
            .caught(exception -> ErrorManager.handle(event, exception));

        if (callback.hasCooldown()) {
            this.halpBotCore.addCooldown(
                userId,
                messageId,
                callback.createCooldown());
        }
    }

    public void registerCallback(Message message, ReactionCallback reactionCallback) {
        long messageId = message.getIdLong();
        if (this.reactionCallbacks.containsKey(messageId)) {
            Map<String, ReactionCallback> messageCallbacks = new ConcurrentHashMap<>();
            messageCallbacks.put(reactionCallback.getCodepointEmoji(), reactionCallback);
            this.reactionCallbacks.put(messageId, messageCallbacks);
        }
        else this.reactionCallbacks
                .get(messageId)
                .put(reactionCallback.getCodepointEmoji(), reactionCallback);

        message.addReaction(reactionCallback.getCodepointEmoji())
            .queue(success -> {
                if (0 < reactionCallback.getCooldownDuration()) {
                    this.concurrentManager.schedule(
                        reactionCallback.getDeleteAfterDuration(),
                        reactionCallback.getDeleteAfterTimeUnit(),
                        () -> this.removeReactionCallback(message, reactionCallback));
                }
            });
    }

    private void removeReactionCallback(Message message, ReactionCallback reactionCallback) {
        long messageId = message.getIdLong();
        this.reactionCallbacks
            .get(messageId)
            .remove(reactionCallback.getCodepointEmoji());

        message.clearReactions(reactionCallback.getCodepointEmoji()).queue();
        if (this.reactionCallbacks.get(messageId).isEmpty()) {
            this.reactionCallbacks.remove(messageId);
        }
    }

}
