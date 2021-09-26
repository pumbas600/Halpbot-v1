package nz.pumbas.halpbot.commands.chemmat;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.awt.Color;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import nz.pumbas.halpbot.adapters.ReactionAdapter;
import nz.pumbas.halpbot.commands.annotations.Command;
import nz.pumbas.halpbot.commands.permissions.HalpbotPermissions;
import nz.pumbas.halpbot.hibernate.exceptions.ResourceAlreadyExistsException;
import nz.pumbas.halpbot.hibernate.exceptions.ResourceNotFoundException;
import nz.pumbas.halpbot.hibernate.models.Modification;
import nz.pumbas.halpbot.hibernate.models.Question;
import nz.pumbas.halpbot.hibernate.models.QuestionModification;
import nz.pumbas.halpbot.hibernate.services.QuestionModificationService;
import nz.pumbas.halpbot.hibernate.services.QuestionService;
import nz.pumbas.halpbot.hibernate.services.TopicService;
import nz.pumbas.halpbot.reactions.ReactionCallback;
import nz.pumbas.halpbot.reactions.ReactionCallback.ReactionCallbackBuilder;
import nz.pumbas.halpbot.utilities.ErrorManager;
import nz.pumbas.halpbot.utilities.HalpbotUtils;

@Component
public class QuestionModificationCommands
{
    private static final int MODIFICATION_DISPLAY_AMOUNT = 5;

    private final QuestionModificationService questionModificationService;
    private final QuestionService questionService;
    private final TopicService topicService;

    private final ReactionCallbackBuilder callbackBuilder = ReactionCallback.builder()
        .addPermissions(HalpbotPermissions.ADMIN)
        .setSingleUse()
        .setDeleteAfter(-1, TimeUnit.MINUTES);

    @Autowired
    public QuestionModificationCommands(QuestionModificationService questionModificationService,
                                        QuestionService questionService,
                                        TopicService topicService)
    {
        this.questionModificationService = questionModificationService;
        this.questionService = questionService;
        this.topicService = topicService;
    }

    @Command(alias = "modificationsCount", description = "Returns the number of modifications waiting to be approved")
    public long modificationsCount() {
        return this.questionModificationService.count();
    }


    @Command(alias = "declineAll", description = "Declines all the modifications currently waiting for approval",
             permissions = HalpbotPermissions.BOT_OWNER)
    public String declineAll() {
        long modificationsCount = this.modificationsCount();
        if (0 != modificationsCount) {
            this.questionModificationService.deleteAll();
        }
        return "Declined all " + modificationsCount + " modicatications";
    }

    @Command(alias = "modifications", description = "Lists all the modifications currently waiting for approval",
             permissions = HalpbotPermissions.BOT_OWNER)
    public @Nullable String modifications(ReactionAdapter reactionAdapter, MessageReceivedEvent event) {
        List<QuestionModification> questions = this.questionModificationService.list();
        if (questions.isEmpty())
            return "There are currently no pending modifications :tada:";

        questions.forEach(question ->
            event.getChannel()
                .sendMessageEmbeds(this.createMessageEmbed(question))
                .queue(m -> {
                    reactionAdapter.registerCallback(m, this.callbackBuilder.setEmoji("U+2705")
                        .setRunnable(() -> this.acceptModification(question))
                        .build());
                    reactionAdapter.registerCallback(m, this.callbackBuilder.setEmoji("U+274C")
                        .setRunnable(() -> this.deleteModification(question.getId()))
                        .build());
        }));
        return null;
    }

    private void acceptModification(QuestionModification question) {
        try {
            if (Modification.ADD == question.getModification())
                this.questionService.save(question.asQuestion());
            else
                this.questionService.update(question.asQuestion());
        }
        catch (ResourceNotFoundException | ResourceAlreadyExistsException e) {
            ErrorManager.handle(e);
        }
        this.deleteModification(question.getId());
    }

    private void deleteModification(Long modificationId) {
        try {
            this.questionModificationService.deleteById(modificationId);
        } catch (ResourceNotFoundException e) {
            ErrorManager.handle(e);
        }
    }

    private MessageEmbed createMessageEmbed(QuestionModification question) {
        EmbedBuilder builder = new EmbedBuilder();
        if (Modification.ADD == question.getModification())
            this.addModificationEmbed(builder, question);
        else this.editModificationEmbed(builder, question, null);

        if (StringUtils.hasLength(question.getImage()))
            builder.setImage(question.getImage());

        builder.setFooter("ModificationId: " + question.getId() + " - " + question.getModification());
        return builder.build();
    }

    private void addModificationEmbed(EmbedBuilder builder, QuestionModification question) {
        builder.setTitle(HalpbotUtils.capitaliseEachWord(
            this.topicService.topicFromId(question.getTopicId())));
        builder.setColor(Color.GREEN);

        builder.setDescription(
            this.buildDescription(question.getQuestion(), question.getAnswer(), question.getOptionB(),
                question.getOptionC(), question.getOptionD(), question.getExplanation()));
    }

    private void editModificationEmbed(EmbedBuilder builder, QuestionModification question, Question original) {
        builder.setColor(Color.ORANGE);
    }

    private String buildDescription(@NotNull String question, @NotNull String answer, @NotNull String optionB,
                                    @Nullable String optionC, @Nullable String optionD, @Nullable String explanation)
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
            builder.append("\n**Explanation:**\n").append(explanation);
        return builder.toString();
    }

    private String getEditedString(String edited, String original) {
        if (Objects.equals(edited, original)) {
            return original;
        }
        else return "**EDITED:** " + edited + "\n**ORIGINAL:** " + original;
    }
}
