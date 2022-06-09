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
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Emoji;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.interactions.Interaction;
import net.dv8tion.jda.api.interactions.components.Button;
import net.pumbas.halpbot.actions.cooldowns.Cooldown;
import net.pumbas.halpbot.buttons.ButtonAdapter;
import net.pumbas.halpbot.buttons.ButtonHandler;
import net.pumbas.halpbot.commands.annotations.Command;
import net.pumbas.halpbot.commands.annotations.SlashCommand;
import net.pumbas.halpbot.converters.annotations.parameter.Description;
import net.pumbas.halpbot.converters.annotations.parameter.Remaining;
import net.pumbas.halpbot.converters.annotations.parameter.Source;
import net.pumbas.halpbot.converters.annotations.parameter.Unrequired;
import net.pumbas.halpbot.hibernate.exceptions.ResourceNotFoundException;
import net.pumbas.halpbot.hibernate.models.Question;
import net.pumbas.halpbot.hibernate.models.Topic;
import net.pumbas.halpbot.hibernate.models.UserStatistics;
import net.pumbas.halpbot.hibernate.services.QuestionService;
import net.pumbas.halpbot.hibernate.services.TopicService;
import net.pumbas.halpbot.hibernate.services.UserStatisticsService;
import net.pumbas.halpbot.objects.expiring.ExpiringHashSet;
import net.pumbas.halpbot.permissions.HalpbotPermissions;
import net.pumbas.halpbot.permissions.Permissions;
import net.pumbas.halpbot.utilities.ErrorManager;
import net.pumbas.halpbot.utilities.HalpbotUtils;

import org.dockbox.hartshorn.util.ApplicationException;
import org.dockbox.hartshorn.util.Result;
import org.jetbrains.annotations.Nullable;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class ChemmatCommands {

    private static final Emoji[] EMOJIS = {
        Emoji.fromMarkdown("\uD83C\uDDE6"),
        Emoji.fromMarkdown("\uD83C\uDDE7"),
        Emoji.fromMarkdown("\uD83C\uDDE8"),
        Emoji.fromMarkdown("\uD83C\uDDE9")
    };
    private static final Emoji QUESTION_MARK = Emoji.fromMarkdown("U+2754");
    private static final long LISTENING_DURATION = 20;

    private static final String[] STREAK_MESSAGES = {
        "{NAME} is on **fire!** :fire:", "Someone call the firefighters, {NAME} is **blazing!**",
        "Everyone stand back, :fire: **{NAME}** :fire: is here!", "{NAME} is an **inferno!** :fire:",
        "Can **{NAME}** even be stopped??", "I think **{NAME}** actually knows ALL these questions :exploding_head:",
        "**{NAME}** is a chemmat legend!", "Clearly these questions are just too easy for {NAME} :fire:",
        "**{NAME}** is an overlord!", "These questions are just another thing for {NAME} to **dominate!**",
        "{NAME} has a **hooouuuge** streak!", "I think {NAME} just qualified for part 2 chemmat!",
        "What is this...? {NAME} is **transcending!**", "What is **{NAME}** becoming...",
        "My lord... What has {NAME} achieved... :face_with_spiral_eyes:", "{NAME} has evolved into a chemmat captain!",
        "Did that feel a little anticlimactic {NAME}?", "I'm not sure if I should be impressed or terrified that " +
        "{NAME} has reached this streak", "How did we get here {NAME}? :flushed:",
        "At this point {NAME} must have done all the possible questions!??", "Does this qualify {NAME} to be a chemmat lecturer? :thinking:",
        "{NAME} has become a **EUTECOID BEAST KING** :crown:", "The lohonators welcome you {NAME} :pray:",
    };

    private final Set<String> clickedButtons = new ExpiringHashSet<>(LISTENING_DURATION, TimeUnit.MINUTES);
    private final Random random = new Random();

    private final QuestionService questionService;
    private final TopicService topicService;
    private final UserStatisticsService userStatisticsService;

    private final QuestionHandler defaultQuestionHandler;
    private final Map<Long, QuestionHandler> questionHandlers = new HashMap<>();

    //@Autowired
    public ChemmatCommands(final QuestionService questionService, final TopicService topicService, final UserStatisticsService userStatisticsService) {
        this.questionService = questionService;
        this.topicService = topicService;
        this.userStatisticsService = userStatisticsService;
        this.defaultQuestionHandler = new QuestionHandler(this.questionService, this.random);
    }

    @Permissions(user = Permission.ADMINISTRATOR)
    @Command(description = "Checks all the confirmed questions for any image links that are invalid")
    public List<Long> checkLinks() {
        return this.questionService
            .getAllConfirmedQuestions()
            .stream()
            .filter(q -> Objects.nonNull(q.getImage()) && !EmbedBuilder.URL_PATTERN.matcher(q.getImage()).matches())
            .map(Question::getId)
            .collect(Collectors.toList());
    }

    @Permissions(permissions = HalpbotPermissions.BOT_OWNER)
    @Command(description = "Thank everyone who used Halpbot")
    public String thankYou(final JDA jda) {
        final long part1EngineeringId = 813905691713994802L;
        final Guild guild = jda.getGuildById(part1EngineeringId);
        if (null == guild)
            return "There was an error retrieving the guild";

        final TextChannel channel = guild.getTextChannelById(814269336990253057L);
        if (null == channel)
            return "There was an error retrieving the channel";


        final String message = "Thank you for using Halpbot everyone, I hope I was able to halp!\n\n" +
            "Goodluck with your future studies.";

        channel.sendMessage(message).queue();

        return "Message sent";


    }

    @Command(description = "Configuration the current channel with the topics to be quizzed on")
    public String configure(@Source final TextChannel textChannel, @Remaining final String topicInput) {
        final String[] topics = topicInput.toLowerCase(Locale.ROOT).split(", ");
        final Set<Long> topicIds = Arrays.stream(topics)
            .map(this.topicService::getIdFromTopic)
            .filter(Result::present)
            .map(Result::get)
            .collect(Collectors.toSet());

        if (topicIds.isEmpty())
            return "None of those topics seemed valid sorry :(";

        this.questionHandlers.put(
            textChannel.getIdLong(), new QuestionHandler(this.questionService, this.random, topicIds));
        return "Created a new configuration for this channel!";
    }


    @Permissions(user = Permission.ADMINISTRATOR)
    @Command(description = "Reloads the questions from the database and reshuffles them at the same time")
    public String reloadQuestions() {
        this.defaultQuestionHandler.shuffleQuestions();
        this.questionHandlers.values().forEach(QuestionHandler::shuffleQuestions);
        return "Reloaded the questions";
    }

    @SlashCommand
    @Command(description = "Retrieves a random chemmat quiz")
    public @Nullable String quiz(final ButtonAdapter buttonAdapter, final Interaction interaction,
                                 @Description("The id of the quiz") @Unrequired("-1") final long quizId,
                                 @Description("The chemmat topic to get quizzed on") @Unrequired("") final String topic)
    {
        // If the command is called from a thread, the channel will be null
        if (null == interaction.getChannel())
            return "Sorry, this command can't be used from a thread :pensive:";

        final Result<Question> eQuestion;
        if (0 <= quizId) {
            eQuestion = Result.of(() -> this.questionService.getById(quizId));
        } else if (topic.isEmpty()) {
            eQuestion = this.questionHandlers
                .getOrDefault(interaction.getChannel().getIdLong(), this.defaultQuestionHandler)
                .getNextQuestion();
        } else {
            eQuestion = this.getRandomQuestionByTopic(topic);
        }
        if (eQuestion.caught())
            return eQuestion.error().getMessage();
        if (eQuestion.absent())
            return "There seemed to be an issue retrieving the question";

        final Question question = eQuestion.get();
        this.userStatisticsService.getByUserId(interaction.getUser().getIdLong()).incrementQuizzesStarted();
        final List<String> shuffledOptions = question.getShuffledOptions(this.random);
        final List<Button> buttons = new ArrayList<>();

        int index = 0;
        for (final String option : shuffledOptions) {
            final boolean isCorrect = question.getAnswer().equals(option);
            buttons.add(buttonAdapter.register(
                Button.primary("answeredQuestion", ((char) ('A' + index++)) + ""),
                isCorrect));

        }
        buttons.add(buttonAdapter.register(Button.danger("revealAnswer", QUESTION_MARK), question));

        final MessageEmbed quizEmbed = this.buildQuestionEmbed(question, shuffledOptions);
        interaction.replyEmbeds(quizEmbed)
            .addActionRow(buttons)
            .queue();
        return null;
    }

    private Result<Question> getRandomQuestionByTopic(final String topic) {
        return this.topicService.getIdFromTopic(topic)
            .map(topicId -> {
                final List<Long> questionIds = this.questionService.getAllConfirmedIdsByTopicId(topicId);
                if (questionIds.isEmpty()) {
                    throw new ApplicationException(
                        new ResourceNotFoundException(
                            "There appears to be no questions for the topic '%s'".formatted(topic)));
                }
                return this.getRandomQuestion(questionIds);
            });
    }

    private MessageEmbed buildQuestionEmbed(final Question question, final List<String> shuffledOptions) {
        final EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setTitle(HalpbotUtils.capitaliseWords(this.topicService.topicFromId(question.getTopicId())));
        embedBuilder.setColor(Color.ORANGE);
        embedBuilder.setFooter("Chemmat notes - Question id: " + question.getId());
        if (null != question.getImage())
            embedBuilder.setImage(question.getImage());

        final StringBuilder builder = new StringBuilder();
        builder.append(question.getQuestion())
            .append("\n\n");

        int index = 0;
        for (final String option : shuffledOptions) {
            builder.append(EMOJIS[index].getName()).append(" : ").append(option).append('\n');
            index++;
        }

        embedBuilder.setDescription(builder.toString());
        return embedBuilder.build();
    }

    private @Nullable Question getRandomQuestion(final List<Long> ids) {
        if (!ids.isEmpty()) {
            final int randomRow = this.random.nextInt(ids.size());
            try {
                return this.questionService.getById(ids.get(randomRow));
            } catch (final ResourceNotFoundException e) {
                ErrorManager.handle(e);
            }
        }
        return null;
    }

    @Cooldown
    @ButtonHandler(id = "halpbot:answerquestion")
    private MessageEmbed answeredQuestion(final ButtonClickEvent event, final boolean isCorrect) {
        final User user = event.getUser();
        //Combines the user id with the button id to get a unique id describing the button this user clicked
        final String clickId = user.getId() + event.getComponentId();

        final EmbedBuilder builder = new EmbedBuilder();
        final UserStatistics userStatistics = this.userStatisticsService.getByUserId(user.getIdLong());
        if (!this.clickedButtons.contains(clickId)) {
            this.clickedButtons.add(clickId);

            userStatistics.incrementQuestionsAnswered();
            if (isCorrect)
                userStatistics.incrementQuestionsAnsweredCorrectly();
            else userStatistics.resetAnswerStreak();
        }

        if (isCorrect) {
            builder.setTitle("Correct: :white_check_mark:");
            builder.setColor(Color.GREEN);
            builder.setFooter(event.getUser().getName(), event.getUser().getAvatarUrl());
            if (userStatistics.isOnFire()) {
                final int response = Math.min(STREAK_MESSAGES.length - 1,
                    (int) (userStatistics.getCurrentAnswerStreak() / UserStatistics.IS_ON_FIRE_THRESHOLD) - 1);

                builder.setDescription(STREAK_MESSAGES[response].replace("{NAME}", event.getUser().getName()));
            }
        } else {
            builder.setTitle("Incorrect: :x:");
            builder.setColor(Color.RED);
        }
        return builder.build();
    }

    @Cooldown
    @ButtonHandler(id = "halpbot:revealanswers", isEphemeral = true)
    private MessageEmbed revealAnswer(final ButtonClickEvent event, final Question question) {
        final EmbedBuilder builder = new EmbedBuilder();
        final String userId = event.getUser().getId();

        event.getMessage()
            .getButtons()
            .forEach(button -> {
                final String clickId = userId + button.getId();
                this.clickedButtons.add(clickId);
            });

        builder.setTitle("Answer - " + question.getAnswer());
        builder.setColor(HalpbotUtils.Blurple);
        builder.setFooter("Question id: " + question.getId() + " - Note: you won't get any stats for this question now");

        if (null != question.getExplanation()) {
            builder.setDescription(question.getExplanation());
        }
        return builder.build();
    }

    @Command(description = "Retrieves the different topics available for the chemmat notes")
    public MessageEmbed topics() {
        final EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setTitle("Chemmat Topics");
        embedBuilder.setColor(Color.ORANGE);
        embedBuilder.setFooter("Chemmat notes");

        final StringBuilder builder = new StringBuilder();
        final List<Topic> topics = this.topicService.list();
        for (int i = 0; i < topics.size(); i++) {
            final Topic topic = topics.get(i);
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
}
