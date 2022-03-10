/*
 * MIT License
 *
 * Copyright (c) 2021 pumbas600
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package net.pumbas.halpbot.adapters;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.pumbas.halpbot.actions.ReactionActionCallback;
import net.pumbas.halpbot.utilities.ConcurrentManager;

import org.jetbrains.annotations.NotNull;

import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

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
        } else this.reactionCallbacks
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
