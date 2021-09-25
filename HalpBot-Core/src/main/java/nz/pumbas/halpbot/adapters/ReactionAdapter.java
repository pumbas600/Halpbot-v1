package nz.pumbas.halpbot.adapters;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;

import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import nz.pumbas.halpbot.commands.permissions.PermissionManager;
import nz.pumbas.halpbot.reactions.ReactionCallback;
import nz.pumbas.halpbot.utilities.ConcurrentManager;
import nz.pumbas.halpbot.utilities.ErrorManager;
import nz.pumbas.halpbot.utilities.HalpbotUtils;

public class ReactionAdapter extends HalpbotAdapter
{
    private final PermissionManager permissionManager = HalpbotUtils.context().get(PermissionManager.class);

    protected final ConcurrentManager concurrentManager = HalpbotUtils.context().get(ConcurrentManager.class);
    protected final Map<Long, Map<String, ReactionCallback>> reactionCallbacks = new ConcurrentHashMap<>();

    @Override
    public void onMessageReactionAdd(@NotNull MessageReactionAddEvent event) {
        long userId = event.getUserIdLong();
        long messageId = event.getMessageIdLong();
        String emoji = event.getReactionEmote().getAsCodepoints();

        if (event.getUser().isBot() || !this.reactionCallbacks.containsKey(messageId))
            return;

        var callbacks = this.reactionCallbacks.get(messageId);
        if (!callbacks.containsKey(emoji))
            return;

        ReactionCallback callback = callbacks.get(emoji);
        if (!this.permissionManager.hasPermissions(userId, callback.getPermissions())) {
            this.halpBotCore.displayMessage(
                event, "You don't have the required permission to use this reaction callback");
        }


        if (this.halpBotCore.hasCooldown(event, userId, messageId)) {
            if (callback.removeReactionIfCoolingDown()) {
                event.retrieveMessage()
                    .flatMap(m -> m.removeReaction(emoji, event.getUser()))
                    .queue();
            }
            return;
        }

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
        if (!this.reactionCallbacks.containsKey(messageId)) {
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
