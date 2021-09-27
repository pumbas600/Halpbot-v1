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
        //this.questionModificationService.addListener(this::newChangeAdded);
    }

//    @Command(alias = "changesChannel", description = "Sets the current channel to be the location where new changes " +
//            "are automatically sent", permissions = HalpbotPermissions.BOT_OWNER)
//    public String changesChannel(@Source TextChannel textChannel) {
//        this.displayChangesChannel = textChannel.getIdLong();
//        return "Set the current channel as the location to automatically display new changes";
//    }

    @Command(alias = "changesCount", description = "Returns the number of changes waiting to be approved")
    public long modificationsCount() {
        return this.questionModificationService.count();
    }


    @Command(alias = "declineAll", description = "Declines all the changes currently waiting for approval",
             permissions = HalpbotPermissions.BOT_OWNER)
    public String declineAll() {
        long modificationsCount = this.modificationsCount();
        if (0 != modificationsCount) {
            this.questionModificationService.deleteAll();
        }
        return "Declined all " + modificationsCount + " modicatications";
    }

    @Command(alias = "changes", description = "Lists all the changes currently waiting for approval",
             permissions = HalpbotPermissions.BOT_OWNER)
    public @Nullable String changes(ReactionAdapter reactionAdapter, MessageReceivedEvent event) {
        List<QuestionModification> questions = this.questionModificationService.list();
        if (questions.isEmpty())
            return "There are currently no pending changes :tada:";

        questions.forEach(question ->
            event.getChannel()
                .sendMessageEmbeds(this.createMessageEmbed(question))
                .queue(m -> {
                    reactionAdapter.registerCallback(m, this.callbackBuilder.setEmoji("U+2705")
                        .setRunnable(() -> this.acceptChange(question))
                        .build());
                    reactionAdapter.registerCallback(m, this.callbackBuilder.setEmoji("U+274C")
                        .setRunnable(() -> this.deleteChange(question.getId()))
                        .build());
        }));
        return null;
    }

    private void acceptChange(QuestionModification question) {
        try {
            if (Modification.ADD == question.getModification())
                this.questionService.save(question.asQuestion());
            else
                this.questionService.update(question.asQuestion());
        }
        catch (ResourceNotFoundException | ResourceAlreadyExistsException e) {
            ErrorManager.handle(e);
        }
        this.deleteChange(question.getId());
    }

    private void deleteChange(Long modificationId) {
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
        else {
            try {
                Question originalQuestion = this.questionService.getById(question.getId());
                this.editModificationEmbed(builder, question, originalQuestion);
            } catch (ResourceNotFoundException e) {
                ErrorManager.handle(e);
            }
        }

        if (StringUtils.hasLength(question.getImage()))
            builder.setImage(question.getImage());

        builder.setFooter("ModificationId: " + question.getId() + " - " + question.getModification());
        return builder.build();
    }

    private void addModificationEmbed(EmbedBuilder builder, QuestionModification question) {
        builder.setColor(Color.GREEN);
        builder.setTitle(HalpbotUtils.capitaliseWords(
            this.topicService.topicFromId(question.getTopicId())));
        builder.setDescription(
            this.buildDescription(question.getQuestion(), question.getAnswer(), question.getOptionB(),
                question.getOptionC(), question.getOptionD(), question.getExplanation()));
    }

    private void editModificationEmbed(EmbedBuilder builder, QuestionModification question, Question original) {
        builder.setColor(Color.ORANGE);
        String editedTopic = HalpbotUtils.capitaliseWords(this.topicService.topicFromId(question.getTopicId()));
        String originalTopic = HalpbotUtils.capitaliseWords(this.topicService.topicFromId(original.getTopicId()));

        builder.setTitle(this.getEditedString(editedTopic, originalTopic));
        builder.setDescription(
            this.buildDescription(
                this.getEditedString(question.getQuestion(), original.getQuestion()),
                this.getEditedString(question.getAnswer(), original.getAnswer()),
                this.getEditedString(question.getOptionB(), original.getOptionB()),
                this.getEditedString(question.getOptionC(), original.getOptionC()),
                this.getEditedString(question.getOptionD(), original.getOptionD()),
                this.getEditedString(question.getExplanation(), original.getExplanation())
        ));
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
