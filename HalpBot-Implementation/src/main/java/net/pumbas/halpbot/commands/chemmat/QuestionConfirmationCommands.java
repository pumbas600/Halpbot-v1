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

package net.pumbas.halpbot.commands.chemmat;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.pumbas.halpbot.actions.ActionCallback;
import net.pumbas.halpbot.actions.ActionCallbackBuilder;
import net.pumbas.halpbot.commands.annotations.Command;
import net.pumbas.halpbot.converters.annotations.parameter.Source;
import net.pumbas.halpbot.hibernate.exceptions.ResourceNotFoundException;
import net.pumbas.halpbot.hibernate.models.Question;
import net.pumbas.halpbot.hibernate.models.Status;
import net.pumbas.halpbot.hibernate.services.QuestionService;
import net.pumbas.halpbot.hibernate.services.TopicService;
import net.pumbas.halpbot.permissions.HalpbotPermissions;
import net.pumbas.halpbot.permissions.Permissions;
import net.pumbas.halpbot.utilities.ErrorManager;
import net.pumbas.halpbot.utilities.HalpbotUtils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.Color;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class QuestionConfirmationCommands
{
    private static final int DISPLAY_QUESTION_GROUPING_AMOUNT = 8;
    private final Set<Long> waitingConfirmationIds = new HashSet<>();
    private final QuestionService questionService;
    private final TopicService topicService;
    private long displayChangesChannel = -1;

    private final ActionCallbackBuilder callbackBuilder = ActionCallback.builder()
        .setSingleUse()
        .setDeleteAfter(-1, TimeUnit.MINUTES);

    //@Autowired
    public QuestionConfirmationCommands(QuestionService questionService,
                                        TopicService topicService)
    {
        this.questionService = questionService;
        this.topicService = topicService;
    }

    private void checkForNewChanges() {
//        if (-1 == this.displayChangesChannel) return;
//
//        final TextChannel channel = HalpbotCore.getJDA().getTextChannelById(this.displayChangesChannel);
//
//        this.questionService.getAllWaitingConfirmationNotIn(this.waitingConfirmationIds)
//            .forEach(question -> {
//                this.waitingConfirmationIds.add(question.getId());
//                channel.sendMessageEmbeds(this.createMessageEmbed(question))
//                    .queue(m -> this.addReactionCallbacks(m, question));
//            });
    }

    @Permissions(permissions = HalpbotPermissions.BOT_OWNER)
    @Command(description = "Sets the current channel to be the location where new changes are automatically sent")
    public String setConfirmationChannel(@Source TextChannel textChannel) {
        boolean startThread = -1 == this.displayChangesChannel;
        this.displayChangesChannel = textChannel.getIdLong();
        if (startThread) {
//            HalpbotUtils.context().get(ConcurrentManager.class)
//                .scheduleRegularly(1, 5, TimeUnit.MINUTES, this::checkForNewChanges);
        }
        return "Set the current channel as the location to automatically display new changes";
    }

    @Command(description = "Displays the number of changes waiting to be approved")
    public long changesCount() {
        return this.questionService.countWaitingConfirmation();
    }

    @Permissions(permissions = HalpbotPermissions.BOT_OWNER)
    @Command(description = "Declines all the changes currently waiting for approval")
    public String declineAll() {
        long modificationsCount = this.changesCount();
        if (0 != modificationsCount) {
            this.questionService.deleteAllWaitingConfirmation();
        }
        return "Declined all " + modificationsCount + " modicatications";
    }

    @Permissions(permissions = HalpbotPermissions.BOT_OWNER)
    @Command(description = "Lists all the changes currently waiting for approval")
    public @Nullable String changes(MessageReceivedEvent event) {
        List<Question> questions = this.questionService.getAmountWaitingConfirmation(DISPLAY_QUESTION_GROUPING_AMOUNT);
        if (questions.isEmpty())
            return "There are currently no questions pending approval :tada:";

        questions.forEach(question ->
            event.getChannel()
                .sendMessageEmbeds(this.createMessageEmbed(question))
                .queue(m -> this.addReactionCallbacks(m, question)));
        return null;
    }

    private void addReactionCallbacks(Message message, Question question) {
//        ReactionAdapter reactionAdapter = Halpbot.getReactionAdapter();
//
//        reactionAdapter.registerCallback(message, this.callbackBuilder.setEmoji("U+2705")
//            .setRunnable(() -> this.acceptChange(question))
//            .buildReactionCallback());
//        reactionAdapter.registerCallback(message, this.callbackBuilder.setEmoji("U+274C")
//            .setRunnable(() -> this.deleteChange(question.getId()))
//            .buildReactionCallback());
    }

    private void acceptChange(Question question) {
        try {
            if (Status.EDITED == question.getStatus()) {
                // Delete the edit
                this.deleteChange(question.getId());

                question.setId(question.getEditedId());
                question.setEditedId(null);
            }
            question.setStatus(Status.CONFIRMED);
            this.questionService.update(question);
        }
        catch (ResourceNotFoundException e) {
            ErrorManager.handle(e);
        }
    }

    private void deleteChange(Long id) {
        try {
            this.questionService.deleteById(id);
            this.waitingConfirmationIds.remove(id);
        } catch (ResourceNotFoundException e) {
            ErrorManager.handle(e);
        }
    }

    private MessageEmbed createMessageEmbed(Question question) {
        EmbedBuilder builder = new EmbedBuilder();
        if (Status.ADDED == question.getStatus())
            this.addQuestionEmbed(builder, question);
        else {
            try {
                Question originalQuestion = this.questionService.getById(question.getEditedId());
                this.editQuestionEmbed(builder, question, originalQuestion);
            } catch (ResourceNotFoundException e) {
                ErrorManager.handle(e);
            }
        }

        if (this.hasLength(question.getImage()))
            builder.setImage(question.getImage());

        builder.setFooter("ID: " + question.getId() + " - " + question.getStatus());
        return builder.build();
    }

    private void addQuestionEmbed(EmbedBuilder builder, Question question) {
        builder.setColor(Color.GREEN);
        builder.setTitle(HalpbotUtils.capitaliseWords(
            this.topicService.topicFromId(question.getTopicId())));
        builder.setDescription(
            this.buildDescription(question.getQuestion(), question.getAnswer(), question.getOptionB(),
                question.getOptionC(), question.getOptionD(), question.getExplanation(),
                question.getImage()));
    }

    private void editQuestionEmbed(EmbedBuilder builder, Question question, Question original) {
        builder.setColor(Color.ORANGE);
        String editedTopic = HalpbotUtils.capitaliseWords(this.topicService.topicFromId(question.getTopicId()));
        String originalTopic = HalpbotUtils.capitaliseWords(this.topicService.topicFromId(original.getTopicId()));

        builder.setTitle(this.getEditedString(editedTopic, originalTopic));
        builder.setDescription(
            "**EDITING QUESTION ID: " + question.getEditedId() + "**\n" +
            this.buildDescription(
                this.getEditedString(question.getQuestion(), original.getQuestion()),
                this.getEditedString(question.getAnswer(), original.getAnswer()),
                this.getEditedString(question.getOptionB(), original.getOptionB()),
                this.getEditedString(question.getOptionC(), original.getOptionC()),
                this.getEditedString(question.getOptionD(), original.getOptionD()),
                this.getEditedString(question.getExplanation(), original.getExplanation()),
                this.getEditedString(question.getImage(), original.getImage())
        ));
    }

    private String buildDescription(@NotNull String question, @NotNull String answer, @NotNull String optionB,
                                    @Nullable String optionC, @Nullable String optionD, @Nullable String explanation,
                                    @Nullable String image)
    {
        StringBuilder builder = new StringBuilder()
            .append(question).append("\n\n")
            .append(":regional_indicator_a: : ").append(answer).append("\n")
            .append(":regional_indicator_b: : ").append(optionB).append("\n");
        if (this.hasLength(optionC))
            builder.append(":regional_indicator_c: : ").append(optionC).append("\n");
        if (this.hasLength(optionD))
            builder.append(":regional_indicator_d: : ").append(optionD).append("\n");
        if (this.hasLength(explanation))
            builder.append("\n**Explanation:**\n").append(explanation).append("\n");
        if (this.hasLength(image))
            builder.append("\n**Image:**\n").append(image);
        return builder.toString();
    }

    private String getEditedString(String edited, String original) {
        if (Objects.equals(edited, original)) {
            return original;
        }
        else return "**EDITED:** " + edited + "\n**ORIGINAL:** " + original;
    }

    private boolean hasLength(@Nullable String str) {
        return str != null && !str.isBlank();
    }
}
