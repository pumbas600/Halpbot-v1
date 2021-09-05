package nz.pumbas.halpbot.commands.chemmat;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.ReadyEvent;
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
import nz.pumbas.halpbot.sql.SQLManager;
import nz.pumbas.halpbot.sql.SQLUtils;
import nz.pumbas.halpbot.sql.table.Table;
import nz.pumbas.halpbot.sql.table.TableRow;
import nz.pumbas.halpbot.sql.table.column.ColumnIdentifier;
import nz.pumbas.halpbot.sql.table.column.SimpleColumnIdentifier;
import nz.pumbas.halpbot.utilities.ErrorManager;
import nz.pumbas.halpbot.utilities.HalpbotUtils;

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
        this.driver = HalpbotUtils.context()
            .get(SQLManager.class)
            .getDriver("chemmatnotes");

        // Cache the tables
        this.driver.onLoad(this::loadDatabase);
    }

    private void loadDatabase(Connection connection) throws SQLException{
        ResultSet resultSet = this.driver.executeQuery(connection,
            "SELECT topic, question, answer, optionA, optionB, optionC, optionD FROM notes INNER JOIN topics " +
                "ON notes.topicId = topics.id");

        this.quizNotes = SQLUtils.asTable(resultSet,
            TOPIC, QUESTION, ANSWER, OPTIONA, OPTIONB, OPTIONC, OPTIOND);

        resultSet = this.driver.executeQuery(connection, "SELECT * FROM topics");
        this.topics = SQLUtils.asTable(resultSet, ID, TOPIC);
    }

    @Command(alias = "quiz", description = "Retrieves a random chemmat quiz on the specified topic")
    public void quiz(AbstractCommandAdapter commandAdapter,
                     @Source MessageChannel channel,
                     @Remaining String topic)
    {
        Table table = this.quizNotes.where(TOPIC, topic.toLowerCase(Locale.ROOT));
        if (0 == table.count()) {
            channel.sendMessage(String.format("There appears to be no quizzes for the topic '%s'",
                HalpbotUtils.capitaliseEachWord(topic))).queue();
            return;
        }

        Quiz quiz = SQLUtils.asModel(Quiz.class, this.getRandomRow(table));
        quiz.shuffleAnswers();
        String[] emojis = { "\uD83C\uDDE6",  "\uD83C\uDDE7", "\uD83C\uDDE8", "\uD83C\uDDE9" };

        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setTitle(HalpbotUtils.capitaliseEachWord(topic));
        embedBuilder.setColor(Color.ORANGE);
        embedBuilder.setFooter("Chemmat notes");

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

    @Command(alias = "addQuiz", description = "Adds a new chemmat quiz question to the database")
    public String addQuiz(@Explicit String topic, @Explicit String question, int answer,
                          @Explicit String optionA, @Explicit String optionB,
                          @Explicit @Unrequired String optionC, @Explicit @Unrequired String optionD)
    {
        topic = topic.toLowerCase(Locale.ROOT);
        Table topicTable = this.topics.where(TOPIC, topic);

        if (0 == topicTable.count()) {
            return "There doesn't seem to be a topic '" + topic + "'. Check your spelling or create your own one " +
                "using the **addTopic** command";
        }

        int topicId = topicTable.first().get().value(ID).or(-1);
        this.insertQuizNote(topic, topicId, question, answer, optionA,
            optionB, optionC, optionD);

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
        return "Sucessfully created the topic '" + topic + "' :tada:";
    }

    private void onAnswerReaction(MessageReactionAddEvent event, int answerOption, int correctAnswer) {
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
            .queue(m -> m.delete().queueAfter(30L, TimeUnit.SECONDS,
                t -> event.getReaction().removeReaction(event.getUser()).queue()));
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
            this.driver.executeUpdate(connection,"INSERT INTO topics (topic) VALUES (?)",topic);
            ResultSet resultSet = this.driver.executeQuery(connection,
                "SELECT id FROM topics WHERE topic == ?", topic);

            int id = resultSet.getInt("id");
            this.topics.addRow(id, topic);
            return id;
        } catch (SQLException e) {
            ErrorManager.handle(e);
        }
        return -1;
    }

    private void insertQuizNote(String topic, int topicId, String question, int answer, String optionA,
                                String optionB, String optionC, String optionD) {
        try (Connection connection = this.driver.createConnection()) {
            String sql = "INSERT INTO notes (topicId, question, answer, optionA, optionB, optionC, optionD) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";

            this.driver.executeUpdate(connection, sql, topicId, question, answer,
                optionA, optionB, optionC, optionD);
            this.quizNotes.addRow(topic, question, answer, optionA, optionB, optionC, optionD);
        } catch (SQLException e) {
            ErrorManager.handle(e);
        }
    }
}
