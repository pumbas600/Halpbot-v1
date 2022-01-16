package nz.pumbas.halpbot.adapters;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;

import org.jetbrains.annotations.NotNull;

import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import nz.pumbas.halpbot.actions.ReactionActionCallback;
import nz.pumbas.halpbot.utilities.ConcurrentManager;

public class ReactionAdapter extends AbstractHalpbotAdapter
{
    protected final ConcurrentManager concurrentManager = new ConcurrentManager();
    protected final Map<Long, Map<String, ReactionActionCallback>> reactionCallbacks = new ConcurrentHashMap<>();

    @Override
    public void onMessageReactionAdd(@NotNull MessageReactionAddEvent event) {
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

        ReactionActionCallback actionCallback = callbacks.get(emoji);
        //this.handle(actionCallback, new MessageEvent(event));
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

//    @Override
//    public String getActionId(HalpbotEvent event) {
//        return event.event(MessageReactionAddEvent.class).getMessageId();
//    }

//    @Override
//    public void removeActionCallbacks(HalpbotEvent halpbotEvent) {
//        MessageReactionAddEvent event = halpbotEvent.event(MessageReactionAddEvent.class);
//        long messageId = event.getMessageIdLong();
//
//        event.retrieveMessage()
//            .queue(m -> {
//                this.reactionCallbacks.get(messageId)
//                    .forEach((registeredEmoji, registeredCallback) ->
//                        m.removeReaction(registeredEmoji, event.getJDA().getSelfUser())
//                            .queue());
//                this.reactionCallbacks.remove(messageId);
//            });
//    }
}
