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
import net.dv8tion.jda.api.entities.Message.Attachment;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;

import org.jetbrains.annotations.NotNull;

import java.awt.Color;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import nz.pumbas.halpbot.commands.OnReady;
import nz.pumbas.halpbot.commands.annotations.Command;
import nz.pumbas.halpbot.commands.annotations.Explicit;
import nz.pumbas.halpbot.commands.annotations.Remaining;
import nz.pumbas.halpbot.commands.annotations.Source;
import nz.pumbas.halpbot.commands.annotations.Unrequired;
import nz.pumbas.halpbot.commands.commandadapters.AbstractCommandAdapter;
import nz.pumbas.halpbot.sql.SQLDriver;
import nz.pumbas.halpbot.sql.SQLUtils;
import nz.pumbas.halpbot.sql.table.Table;
import nz.pumbas.halpbot.sql.table.TableRow;
import nz.pumbas.halpbot.sql.table.column.ColumnIdentifier;
import nz.pumbas.halpbot.sql.table.column.SimpleColumnIdentifier;
import nz.pumbas.halpbot.utilities.ErrorManager;
import nz.pumbas.halpbot.utilities.HalpbotUtils;

@SuppressWarnings("ClassWithTooManyFields")
public class ChemmatCommands implements OnReady
{
    private static final ColumnIdentifier<Integer> ID       = new SimpleColumnIdentifier<>("id", Integer.class);
    private static final ColumnIdentifier<String> TOPIC     = new SimpleColumnIdentifier<>("topic", String.class);

    private static final ColumnIdentifier<Integer> TOPIC_ID = new SimpleColumnIdentifier<>("topicId", Integer.class);
    private static final ColumnIdentifier<String> QUESTION  = new SimpleColumnIdentifier<>("question", String.class);
    private static final ColumnIdentifier<Integer> ANSWER   = new SimpleColumnIdentifier<>("answer", Integer.class);
    private static final ColumnIdentifier<String> OPTIONA   = new SimpleColumnIdentifier<>("optionA", String.class);
    private static final ColumnIdentifier<String> OPTIONB   = new SimpleColumnIdentifier<>("optionB", String.class);
    private static final ColumnIdentifier<String> OPTIONC   = new SimpleColumnIdentifier<>("optionC", String.class);
    private static final ColumnIdentifier<String> OPTIOND   = new SimpleColumnIdentifier<>("optionD", String.class);
    private static final ColumnIdentifier<String> IMAGE     = new SimpleColumnIdentifier<>("image", String.class);
    private static final ColumnIdentifier<String> EXPLANATION = new SimpleColumnIdentifier<>("explanation", String.class);

    private static final Color Blurple = new Color(85, 57, 204);

    private final Random random = new Random();
    private SQLDriver driver;

    private Table quizNotes;
    private Table topics;

    /**
     * A method that is called once after the bot has been initialised.
     *
     * @param event
     *     The JDA {@link ReadyEvent}.
     */
    @Override
    public void onReady(@NotNull ReadyEvent event) {
        this.driver = SQLDriver.of("chemmatnotes");

        // Cache the tables
        this.driver.onLoad(this::loadDatabase);
    }

    private void loadDatabase(Connection connection) throws SQLException{
        ResultSet resultSet = this.driver.executeQuery(connection,
            "SELECT notes.id, topic, question, answer, optionA, optionB, optionC, optionD, image, explanation FROM " +
                "notes INNER JOIN topics ON notes.topicId = topics.id");

        this.quizNotes = SQLUtils.asTable(resultSet,
            ID, TOPIC, QUESTION, ANSWER, OPTIONA, OPTIONB, OPTIONC, OPTIOND, IMAGE, EXPLANATION);

        resultSet = this.driver.executeQuery(connection, "SELECT * FROM topics");
        this.topics = SQLUtils.asTable(resultSet, ID, TOPIC);
    }

    @Command(alias = "quiz", description = "Retrieves a random chemmat quiz on the specified topic")
    public void quiz(AbstractCommandAdapter commandAdapter,
                     @Source MessageChannel channel,
                     @Unrequired("-1") int quizId,
                     @Unrequired("") @Remaining String topic)
    {
        Table table;
        if (topic.isEmpty())
            table = this.quizNotes;
        else
            table = this.quizNotes.where(TOPIC, topic.toLowerCase(Locale.ROOT));

        if (0 == table.count()) {
            channel.sendMessage(String.format("There appears to be no quizzes for the topic '%s'",
                HalpbotUtils.capitaliseEachWord(topic))).queue();
            return;
        }

        TableRow tableRow = (0 >= quizId || quizId > this.quizNotes.count())
            ? this.getRandomRow(table)
            : this.quizNotes.rows().get(quizId - 1);

        Quiz quiz = SQLUtils.asModel(Quiz.class, tableRow);
        quiz.shuffleAnswers();
        String[] emojis = { "\uD83C\uDDE6", "\uD83C\uDDE7", "\uD83C\uDDE8", "\uD83C\uDDE9" };

        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setTitle(HalpbotUtils.capitaliseEachWord(quiz.getTopic()));
        embedBuilder.setColor(Color.ORANGE);
        embedBuilder.setFooter("Chemmat notes - Question id: " + quiz.getId());
        if (null != quiz.getImage())
            embedBuilder.setImage(quiz.getImage());

        StringBuilder builder = new StringBuilder();
        builder.append(quiz.getQuestion())
            .append("\n\n");

        int index = 0;
        for (String option : quiz.getOptions()) {
            if (null != option) {
                builder.append(emojis[index]).append(" : ").append(option).append('\n');
                index++;
            }
        }

        embedBuilder.setDescription(builder.toString());
        int answer = quiz.getAnswer();

        channel.sendMessageEmbeds(embedBuilder.build())
            .queue(m -> {
                int optionIndex = 0;
                List<String> options = quiz.getOptions();
                for (int i = 0; i <  options.size(); i++) {
                    if (null != options.get(i)) {
                        int finalI = i + 1;
                        commandAdapter.addReactionCallback(m, emojis[optionIndex],
                            e -> this.onAnswerReaction(e, finalI, answer));
                        optionIndex++;
                    }
                }
                commandAdapter.addReactionCallback(m, "U+2753",
                    e -> this.onRevealAnswer(e, options.get(answer - 1), quiz));
        });
    }

    @Command(alias = "topics", description = "Retrieves the different topics available for the chemmat notes")
    public MessageEmbed topics() {
        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setTitle("Chemmat Topics");
        embedBuilder.setColor(Color.ORANGE);
        embedBuilder.setFooter("Chemmat notes");

        StringBuilder builder = new StringBuilder();
        List<TableRow> rows = this.topics.rows();
        for (int i = 0; i < rows.size(); i++) {
            TableRow row = rows.get(i);
            builder.append(i + 1)
                .append(". ")
                .append(row.value(TOPIC).map(HalpbotUtils::capitaliseEachWord).or("Generic"))
                .append('\n');
        }
        embedBuilder.setDescription(builder.toString());

        return embedBuilder.build();
    }

    @Command(alias = "addQuiz", description = "Adds a new chemmat quiz question to the database. Note: The correct " +
        "answer must be the first option")
    public String addQuiz(MessageReceivedEvent event, @Explicit String topic, @Explicit String question, int answer,
                          @Explicit String optionA, @Explicit String optionB,
                          @Explicit @Unrequired String optionC, @Explicit @Unrequired String optionD)
    {
        topic = topic.toLowerCase(Locale.ROOT);
        Table topicTable = this.topics.where(TOPIC, topic);

        if (0 == topicTable.count()) {
            return "There doesn't seem to be a topic '" + topic + "'. Check your spelling or create your own one " +
                "using the **addTopic** command";
        }
        String image = event.getMessage().getAttachments()
            .stream()
            .filter(Attachment::isImage)
            .findFirst()
            .map(Attachment::getUrl)
            .orElse(null);

        int topicId = topicTable.first().get().value(ID).or(-1);
        this.insertQuizNote(topic, topicId, question, answer, optionA,
            optionB, optionC, optionD, image);

        return "Inserted the quiz question to the '" + topic + "' topic :smiling_face_with_3_hearts:";
    }

    @Command(alias = "addTopic", description = "Adds a new chemmat topic to the database")
    public String addTopic(@Remaining String topic) {
        topic = topic.toLowerCase(Locale.ROOT);
        Table topicTable = this.topics.where(TOPIC, topic);

        if (0 != topicTable.count()) {
            return "There already appears to be a '" + topic + "' topic";
        }

        if (-1 == this.insertTopic(topic))
            return "There was an error trying to insert the topic :sob:";
        return "Sucessfully created the topic '" + topic + "' :tada: *(This won't be available until the database is reloaded though)*";
    }

    private void onAnswerReaction(MessageReactionAddEvent event, int answerOption, int correctAnswer) {
        // Hide the response as quickly as possible to avoid others from seeing it
        event.getReaction().removeReaction(event.getUser()).queue();

        EmbedBuilder builder = new EmbedBuilder();
        if (answerOption == correctAnswer) {
            builder.setTitle("Correct: :white_check_mark:");
            builder.setColor(Color.GREEN);
        }
        else {
            builder.setTitle("Incorrect: :x:");
            builder.setColor(Color.RED);
        }
        builder.setFooter(event.getUser().getName(), event.getUser().getAvatarUrl());
        event.getChannel().sendMessageEmbeds(builder.build())
            .queue(m -> m.delete().queueAfter(30L, TimeUnit.SECONDS));
    }

    private void onRevealAnswer(@NotNull MessageReactionAddEvent event, @NotNull String answer, @NotNull Quiz quiz) {
        event.getReaction().removeReaction(event.getUser()).queue();
        User user = event.getUser();

        EmbedBuilder builder = new EmbedBuilder();

        builder.setTitle("Answer - " + answer);
        builder.setColor(Blurple);
        builder.setFooter(HalpbotUtils.capitalise(quiz.getTopic()) + " - Question id: " + quiz.getId());

        if (null != quiz.getExplanation()) {
            builder.setDescription(quiz.getExplanation());
        }
        event.getChannel().sendMessageEmbeds(builder.build())
            .queue(m -> m.delete().queueAfter(30L, TimeUnit.SECONDS));
    }

    private TableRow getRandomRow(Table table) {
        List<TableRow> rows = table.rows();
        if (rows.isEmpty())
            return new TableRow();

        int randomRow = this.random.nextInt(rows.size());
        return rows.get(randomRow);
    }

    private int insertTopic(String topic) {
        try (Connection connection = this.driver.createConnection()) {
            return this.driver.executeUpdate(connection,"INSERT INTO topics (topic) VALUES (?)",topic);
        } catch (SQLException e) {
            ErrorManager.handle(e);
        }
        return -1;
    }

    private void insertQuizNote(String topic, int topicId, String question, int answer, String optionA,
                                String optionB, String optionC, String optionD, String image) {
        try (Connection connection = this.driver.createConnection()) {
            String sql = "INSERT INTO notes (topicId, question, answer, optionA, optionB, optionC, optionD, image) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

            this.driver.executeUpdate(connection, sql, topicId, question, answer,
                optionA, optionB, optionC, optionD, image);
            this.quizNotes.addRow(-1, topic, question, answer, optionA, optionB, optionC, optionD, image, null);
        } catch (SQLException e) {
            ErrorManager.handle(e);
        }
    }
}
