package nz.pumbas.halpbot.adapters;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import nz.pumbas.halpbot.reactions.ReactionCallback;
import nz.pumbas.halpbot.utilities.ConcurrentManager;
import nz.pumbas.halpbot.utilities.HalpbotUtils;

public class ReactionAdapter extends ListenerAdapter implements HalpbotAdapter
{
    protected final ConcurrentManager concurrentManager = HalpbotUtils.context().get(ConcurrentManager.class);
    protected final Map<Long, Map<String, ReactionCallback>> reactionCallbacks = new ConcurrentHashMap<>();

    public void registerCallback(Message message, ReactionCallback reactionCallback) {
        long messageId = message.getIdLong();
        if (this.reactionCallbacks.containsKey(messageId)) {
            Map<String, ReactionCallback> messageCallbacks = new ConcurrentHashMap<>();
            messageCallbacks.put(reactionCallback.getCodepointEmoji(), reactionCallback);
            this.reactionCallbacks.put(messageId, messageCallbacks);
        }
        else
            this.reactionCallbacks
                .get(messageId)
                .put(reactionCallback.getCodepointEmoji(), reactionCallback);

        message.addReaction(reactionCallback.getCodepointEmoji())
            .queue(success ->
                this.concurrentManager.schedule(10));
    }

}
