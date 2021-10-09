package nz.pumbas.halpbot.commands.chemmat;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.awt.Color;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import nz.pumbas.halpbot.Halpbot;
import nz.pumbas.halpbot.actions.ActionCallback;
import nz.pumbas.halpbot.adapters.HalpbotCore;
import nz.pumbas.halpbot.adapters.ReactionAdapter;
import nz.pumbas.halpbot.commands.annotations.Command;
import nz.pumbas.halpbot.commands.annotations.Source;
import nz.pumbas.halpbot.permissions.HalpbotPermissions;
import nz.pumbas.halpbot.hibernate.exceptions.ResourceNotFoundException;
import nz.pumbas.halpbot.hibernate.models.Question;
import nz.pumbas.halpbot.hibernate.models.Status;
import nz.pumbas.halpbot.hibernate.services.QuestionService;
import nz.pumbas.halpbot.hibernate.services.TopicService;
import nz.pumbas.halpbot.actions.ActionCallbackBuilder;
import nz.pumbas.halpbot.utilities.ConcurrentManager;
import nz.pumbas.halpbot.utilities.ErrorManager;
import nz.pumbas.halpbot.utilities.HalpbotUtils;

@Service
public class QuestionConfirmationCommands
{
    private static final int DISPLAY_QUESTION_GROUPING_AMOUNT = 8;
    private final Set<Long> waitingConfirmationIds = new HashSet<>();
    private final QuestionService questionService;
    private final TopicService topicService;
    private long displayChangesChannel = -1;

    private final ActionCallbackBuilder callbackBuilder = ActionCallback.builder()
        .addPermissions(HalpbotPermissions.ADMIN)
        .setSingleUse()
        .setDeleteAfter(-1, TimeUnit.MINUTES);

    @Autowired
    public QuestionConfirmationCommands(QuestionService questionService,
                                        TopicService topicService)
    {
        this.questionService = questionService;
        this.topicService = topicService;
    }

    private void checkForNewChanges() {
        if (-1 == this.displayChangesChannel) return;

        final TextChannel channel = HalpbotCore.getJDA().getTextChannelById(this.displayChangesChannel);

        this.questionService.getAllWaitingConfirmationNotIn(this.waitingConfirmationIds)
            .forEach(question -> {
                this.waitingConfirmationIds.add(question.getId());
                channel.sendMessageEmbeds(this.createMessageEmbed(question))
                    .queue(m -> this.addReactionCallbacks(m, question));
            });
    }

    @Command(description = "Sets the current channel to be the location where new changes are automatically sent",
             permissions = HalpbotPermissions.BOT_OWNER)
    public String setConfirmationChannel(@Source TextChannel textChannel) {
        boolean startThread = -1 == this.displayChangesChannel;
        this.displayChangesChannel = textChannel.getIdLong();
        if (startThread) {
            HalpbotUtils.context().get(ConcurrentManager.class)
                .scheduleRegularly(1, 5, TimeUnit.MINUTES, this::checkForNewChanges);
        }
        return "Set the current channel as the location to automatically display new changes";
    }

    @Command(description = "Displays the number of changes waiting to be approved")
    public long changesCount() {
        return this.questionService.countWaitingConfirmation();
    }

    @Command(description = "Declines all the changes currently waiting for approval",
             permissions = HalpbotPermissions.BOT_OWNER)
    public String declineAll() {
        long modificationsCount = this.changesCount();
        if (0 != modificationsCount) {
            this.questionService.deleteAllWaitingConfirmation();
        }
        return "Declined all " + modificationsCount + " modicatications";
    }

    @Command(description = "Lists all the changes currently waiting for approval",
             permissions = HalpbotPermissions.BOT_OWNER)
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
        ReactionAdapter reactionAdapter = Halpbot.getReactionAdapter();

        reactionAdapter.registerCallback(message, this.callbackBuilder.setEmoji("U+2705")
            .setRunnable(() -> this.acceptChange(question))
            .buildReactionCallback());
        reactionAdapter.registerCallback(message, this.callbackBuilder.setEmoji("U+274C")
            .setRunnable(() -> this.deleteChange(question.getId()))
            .buildReactionCallback());
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

        if (StringUtils.hasLength(question.getImage()))
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
        if (StringUtils.hasLength(optionC))
            builder.append(":regional_indicator_c: : ").append(optionC).append("\n");
        if (StringUtils.hasLength(optionD))
            builder.append(":regional_indicator_d: : ").append(optionD).append("\n");
        if (StringUtils.hasLength(explanation))
            builder.append("\n**Explanation:**\n").append(explanation).append("\n");
        if (StringUtils.hasLength(image))
            builder.append("\n**Image:**\n").append(image);
        return builder.toString();
    }

    private String getEditedString(String edited, String original) {
        if (Objects.equals(edited, original)) {
            return original;
        }
        else return "**EDITED:** " + edited + "\n**ORIGINAL:** " + original;
    }
}
