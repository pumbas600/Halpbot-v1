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

package nz.pumbas.halpbot.commands.chemmat;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import nz.pumbas.halpbot.adapters.ReactionAdapter;
import nz.pumbas.halpbot.commands.OnReady;
import nz.pumbas.halpbot.commands.annotations.Command;
import nz.pumbas.halpbot.commands.annotations.Remaining;
import nz.pumbas.halpbot.commands.annotations.Source;
import nz.pumbas.halpbot.commands.annotations.Unrequired;
import nz.pumbas.halpbot.commands.permissions.HalpbotPermissions;
import nz.pumbas.halpbot.hibernate.exceptions.ResourceNotFoundException;
import nz.pumbas.halpbot.hibernate.models.Question;
import nz.pumbas.halpbot.hibernate.models.Topic;
import nz.pumbas.halpbot.hibernate.services.QuestionService;
import nz.pumbas.halpbot.hibernate.services.TopicService;
import nz.pumbas.halpbot.objects.Exceptional;
import nz.pumbas.halpbot.reactions.ReactionCallback;
import nz.pumbas.halpbot.reactions.ReactionCallback.ReactionCallbackBuilder;
import nz.pumbas.halpbot.utilities.ErrorManager;
import nz.pumbas.halpbot.utilities.HalpbotUtils;

@Service
public class ChemmatCommands implements OnReady
{
    private static final Color Blurple = new Color(85, 57, 204);

    private final String[] EMOJIS = { "\uD83C\uDDE6", "\uD83C\uDDE7", "\uD83C\uDDE8", "\uD83C\uDDE9" };

    private final ReactionCallbackBuilder reactionCallbackBuilder = ReactionCallback.builder()
        .setDeleteAfter(10, TimeUnit.MINUTES)
        .setCooldown(5, TimeUnit.SECONDS)
        .setRemoveReactionIfCoolingDown();

    private final Random random = new Random();

    private final QuestionService questionService;
    private final TopicService topicService;
    private List<Long> questionIds;
    private int questionIndex;

    @Autowired
    public ChemmatCommands(QuestionService questionService, TopicService topicService) {
        this.questionService = questionService;
        this.topicService = topicService;
    }

    /**
     * A method that is called once after the bot has been initialised.
     *
     * @param event
     *     The JDA {@link ReadyEvent}.
     */
    @Override
    public void onReady(@NotNull ReadyEvent event) {
        this.shuffleQuestions();
    }

//    @Command(description = "A temporary command used to migrate the question from the SQL database to the Derby " +
//        "database", permissions = HalpbotPermissions.BOT_OWNER)
//    public String migrateQuestions() {
//        this.questionService.bulkSave(
//            this.quizNotes.rows()
//            .stream()
//            .map(row -> SQLUtils.asModel(Quiz.class, row))
//            .map(
//                q -> {
//                    Question question = new Question();
//                    question.setTopicId((long)this.topics.where(TOPIC, q.getTopic()).first().get().value(ID).get());
//                    question.setQuestion(q.getQuestion());
//                    question.setAnswer(q.getOptions().get(q.getAnswer() - 1));
//                    question.setOptionB(q.getOptions().get((q.getAnswer()) % 4));
//                    question.setOptionC(q.getOptions().get((q.getAnswer() + 1) % 4));
//                    question.setOptionD(q.getOptions().get((q.getAnswer() + 2) % 4));
//                    question.setExplanation(q.getExplanation());
//                    question.setImage(q.getImage());
//                    question.setStatus(Status.CONFIRMED);
//                    return question;
//                })
//            .collect(Collectors.toList()));
//        return "Successfully migrated all the questions";
//    }

    @Command(description = "Reloads the questions from the database and reshuffles them at the same time",
             permissions = HalpbotPermissions.ADMIN)
    public String reloadQuestions() {
        this.shuffleQuestions();
        return "Reloaded the questions";
    }

    @Command(description = "Retrieves a random chemmat quiz")
    public @Nullable String quiz(ReactionAdapter reactionAdapter,
                                 @Source MessageChannel channel,
                                 @Unrequired("-1") long quizId,
                                 @Unrequired("") @Remaining String topic)
    {
        Question question;
        if (0 <= quizId) {
            try {
                question = this.questionService.getById(quizId);
            } catch (ResourceNotFoundException e) {
                return e.getMessage();
            }
        }
        else if (topic.isEmpty()) {
            question = this.getNextQuestion();
        }
        else {
            Exceptional<Long> topicId = this.topicService.getIdFromTopic(topic);
            if (topicId.caught()) {
                return topicId.error().getMessage();
            }
            List<Long> questionIds = this.questionService.getAllConfirmedIdsByTopicId(topicId.get());
            if (questionIds.isEmpty()) {
                return String.format("There appears to be no questions for the topic '%s'",
                    HalpbotUtils.capitaliseWords(topic));
            }
            question = this.getRandomQuestion(questionIds);
        }
        if (null == question)
            return "There seemed to be an issue retrieving the question";

        List<String> shuffledOptions = question.getShuffledOptions(this.random);

        MessageEmbed quizEmbed = this.buildQuestionEmbed(question, shuffledOptions);
        channel.sendMessageEmbeds(quizEmbed)
            .queue(m -> {
                int index = 0;
                for (String option : shuffledOptions) {
                    reactionAdapter.registerCallback(m,
                        this.reactionCallbackBuilder
                            .setEmoji(this.EMOJIS[index])
                            .setConsumer(e -> this.onAnswerReaction(e, question.getAnswer().equals(option)))
                            .build());
                    index++;
                }

                reactionAdapter.registerCallback(m,
                    this.reactionCallbackBuilder
                        .setEmoji("U+2753")
                        .setConsumer(e -> this.onRevealAnswer(e, question))
                        .build());
        });
        return null;
    }

    private MessageEmbed buildQuestionEmbed(Question question, List<String> shuffledOptions) {
        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setTitle(HalpbotUtils.capitaliseWords(this.topicService.topicFromId(question.getTopicId())));
        embedBuilder.setColor(Color.ORANGE);
        embedBuilder.setFooter("Chemmat notes - Question id: " + question.getId());
        if (null != question.getImage())
            embedBuilder.setImage(question.getImage());

        StringBuilder builder = new StringBuilder();
        builder.append(question.getQuestion())
            .append("\n\n");

        int index = 0;
        for (String option : shuffledOptions) {
            builder.append(this.EMOJIS[index]).append(" : ").append(option).append('\n');
            index++;
        }

        embedBuilder.setDescription(builder.toString());
        return embedBuilder.build();
    }

    @Command(description = "Retrieves the different topics available for the chemmat notes")
    public MessageEmbed topics() {
        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setTitle("Chemmat Topics");
        embedBuilder.setColor(Color.ORANGE);
        embedBuilder.setFooter("Chemmat notes");

        StringBuilder builder = new StringBuilder();
        List<Topic> topics = this.topicService.list();
        for (int i = 0; i < topics.size(); i++) {
            Topic topic = topics.get(i);
            builder.append(i + 1)
                .append(". ")
                .append(HalpbotUtils.capitaliseWords(topic.getTopic()))
                .append('\n');
        }
        embedBuilder.setDescription(builder.toString());
        return embedBuilder.build();
    }

    @Command(description = "Returns the link to the halpbot dashboard where you can add questions")
    public String addQuiz()
    {
        return "You can add questions using the Halpbot Dashboard here: https://www.pumbas.net/questions";
    }

    private void onAnswerReaction(MessageReactionAddEvent event, boolean isCorrect) {
        // Hide the response as quickly as possible to avoid others from seeing it
        event.getReaction().removeReaction(event.getUser()).queue();

        EmbedBuilder builder = new EmbedBuilder();
        if (isCorrect) {
            builder.setTitle("Correct: :white_check_mark:");
            builder.setColor(Color.GREEN);
            builder.setFooter(event.getUser().getName(), event.getUser().getAvatarUrl());
        }
        else {
            builder.setTitle("Incorrect: :x:");
            builder.setColor(Color.RED);
        }
        event.getChannel().sendMessageEmbeds(builder.build())
            .queue(m -> m.delete().queueAfter(30L, TimeUnit.SECONDS));
    }

    private void onRevealAnswer(@NotNull MessageReactionAddEvent event, @NotNull Question question) {
        event.getReaction().removeReaction(event.getUser()).queue();
        User user = event.getUser();

        EmbedBuilder builder = new EmbedBuilder();

        builder.setTitle("Answer - " + question.getAnswer());
        builder.setColor(Blurple);
        builder.setFooter(HalpbotUtils.capitalise(this.topicService.topicFromId(question.getTopicId())) + " - " +
            "Question id: " + question.getId());

        if (null != question.getExplanation()) {
            builder.setDescription(question.getExplanation());
        }
        event.getChannel().sendMessageEmbeds(builder.build())
            .queue(m -> m.delete().queueAfter(30L, TimeUnit.SECONDS));
    }

    private @Nullable Question getRandomQuestion(List<Long> ids) {
        if (!ids.isEmpty()) {
            int randomRow = this.random.nextInt(ids.size());
            try {
                return this.questionService.getById(ids.get(randomRow));
            } catch (ResourceNotFoundException e) {
                ErrorManager.handle(e);
            }
        }
        return null;
    }

    private @Nullable Question getNextQuestion() {
        if (this.questionIndex >= this.questionIds.size()) {
            this.shuffleQuestions();
            this.questionIndex = 0;
        }
        try {
            return this.questionService.getById(this.questionIds.get(this.questionIndex++));
        } catch (ResourceNotFoundException e) {
            ErrorManager.handle(e);
            return null;
        }
    }

    private void shuffleQuestions() {
        // Updates the ids, so that newly added questions get added
        this.questionIds = this.questionService.getAllConfirmedIds();
        Collections.shuffle(this.questionIds, this.random);
    }
}
