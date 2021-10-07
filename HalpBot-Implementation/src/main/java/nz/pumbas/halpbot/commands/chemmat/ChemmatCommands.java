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
import net.dv8tion.jda.api.entities.Emoji;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.interactions.Interaction;
import net.dv8tion.jda.api.interactions.components.Button;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import nz.pumbas.halpbot.actions.annotations.Action;
import nz.pumbas.halpbot.actions.annotations.ButtonAction;
import nz.pumbas.halpbot.actions.annotations.Cooldown;
import nz.pumbas.halpbot.adapters.ButtonAdapter;
import nz.pumbas.halpbot.commands.OnReady;
import nz.pumbas.halpbot.commands.annotations.Command;
import nz.pumbas.halpbot.commands.annotations.Description;
import nz.pumbas.halpbot.commands.annotations.SlashCommand;
import nz.pumbas.halpbot.commands.annotations.Unrequired;
import nz.pumbas.halpbot.permissions.HalpbotPermissions;
import nz.pumbas.halpbot.hibernate.exceptions.ResourceNotFoundException;
import nz.pumbas.halpbot.hibernate.models.Question;
import nz.pumbas.halpbot.hibernate.models.Topic;
import nz.pumbas.halpbot.hibernate.models.UserStatistics;
import nz.pumbas.halpbot.hibernate.services.QuestionService;
import nz.pumbas.halpbot.hibernate.services.TopicService;
import nz.pumbas.halpbot.hibernate.services.UserStatisticsService;
import nz.pumbas.halpbot.objects.Exceptional;
import nz.pumbas.halpbot.utilities.ErrorManager;
import nz.pumbas.halpbot.utilities.HalpbotUtils;

@Service
public class ChemmatCommands implements OnReady
{
    private static final Emoji[] EMOJIS = {
        Emoji.fromMarkdown("\uD83C\uDDE6"),
        Emoji.fromMarkdown("\uD83C\uDDE7"),
        Emoji.fromMarkdown("\uD83C\uDDE8"),
        Emoji.fromMarkdown("\uD83C\uDDE9")
    };

    private static final Emoji QUESTION_MARK = Emoji.fromMarkdown("U+2754");
    private final Random random = new Random();

    private final QuestionService questionService;
    private final TopicService topicService;
    private final UserStatisticsService userStatisticsService;

    private List<Long> questionIds;
    private int questionIndex;

    @Autowired
    public ChemmatCommands(QuestionService questionService, TopicService topicService, UserStatisticsService userStatisticsService) {
        this.questionService = questionService;
        this.topicService = topicService;
        this.userStatisticsService = userStatisticsService;
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

    @SlashCommand
    @Command(description = "Retrieves a random chemmat quiz")
    public @Nullable String quiz(ButtonAdapter buttonAdapter, Interaction interaction,
                                 @Description("The id of the quiz") @Unrequired("-1") long quizId,
                                 @Description("The chemmat topic to get quizzed on") @Unrequired("") String topic)
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
            Exceptional<Question> eQuestion = this.getRandomQuestionByTopic(topic);
            if (eQuestion.caught()) return eQuestion.error().getMessage();
            question = eQuestion.orNull();
        }
        if (null == question)
            return "There seemed to be an issue retrieving the question";

        this.userStatisticsService.getByUserId(interaction.getUser().getIdLong()).incrementQuizzesStarted();
        List<String> shuffledOptions = question.getShuffledOptions(this.random);
        List<Button> buttons = new ArrayList<>();

        int index = 0;
        for (String option : shuffledOptions) {
            boolean isCorrect = question.getAnswer().equals(option);
            buttons.add(buttonAdapter.register(
                Button.primary("answeredQuestion", ((char)('A' + index++)) + ""),
                isCorrect));

        }
        buttons.add(buttonAdapter.register(Button.danger("revealAnswer", QUESTION_MARK), question));

        MessageEmbed quizEmbed = this.buildQuestionEmbed(question, shuffledOptions);
        interaction.replyEmbeds(quizEmbed)
            .addActionRow(buttons)
            .queue();
        return null;
    }

    @Cooldown
    @ButtonAction
    @Action(listeningDuration = 15, displayDuration = 25)
    private MessageEmbed answeredQuestion(ButtonClickEvent event, boolean isCorrect) {
        UserStatistics userStatistics = this.userStatisticsService.getByUserId(event.getUser().getIdLong());
        userStatistics.incrementQuestionsAnswered();

        EmbedBuilder builder = new EmbedBuilder();
        if (isCorrect) {
            userStatistics.incrementQuestionsAnsweredCorrectly();
            builder.setTitle("Correct: :white_check_mark:");
            builder.setColor(Color.GREEN);
            builder.setFooter(event.getUser().getName(), event.getUser().getAvatarUrl());
            if (userStatistics.isOnFire()) {
                builder.setDescription(event.getUser().getName() + " is on **fire!** :fire:");
            }
        }
        else {
            userStatistics.resetAnswerStreak();
            builder.setTitle("Incorrect: :x:");
            builder.setColor(Color.RED);
        }
        return builder.build();
    }

    @Cooldown
    @ButtonAction(isEphemeral = true)
    @Action(listeningDuration = 15)
    private MessageEmbed revealAnswer(ButtonClickEvent event, Question question) {
        EmbedBuilder builder = new EmbedBuilder();

        builder.setTitle("Answer - " + question.getAnswer());
        builder.setColor(HalpbotUtils.Blurple);
        builder.setFooter(HalpbotUtils.capitalise(this.topicService.topicFromId(question.getTopicId())) + " - " +
            "Question id: " + question.getId());

        if (null != question.getExplanation()) {
            builder.setDescription(question.getExplanation());
        }
        return builder.build();
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
            builder.append(EMOJIS[index].getName()).append(" : ").append(option).append('\n');
            index++;
        }

        embedBuilder.setDescription(builder.toString());
        return embedBuilder.build();
    }

    private Exceptional<Question> getRandomQuestionByTopic(String topic) {
        return this.topicService.getIdFromTopic(topic)
            .map(topicId -> {
                List<Long> questionIds = this.questionService.getAllConfirmedIdsByTopicId(topicId);
                if (questionIds.isEmpty()) {
                    throw new ResourceNotFoundException(
                        String.format("There appears to be no questions for the topic '%s'",
                            HalpbotUtils.capitaliseWords(topic)));
                }
                return this.getRandomQuestion(questionIds);
            });
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
    public String addQuiz() {
        return "You can add questions using the Halpbot Dashboard here: https://www.pumbas.net/questions";
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
