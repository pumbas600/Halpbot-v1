package nz.pumbas.halpbot.commands;

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

import nz.pumbas.halpbot.commands.annotations.Command;
import nz.pumbas.halpbot.commands.annotations.Explicit;
import nz.pumbas.halpbot.commands.annotations.Remaining;
import nz.pumbas.halpbot.commands.annotations.Source;
import nz.pumbas.halpbot.commands.annotations.Unrequired;
import nz.pumbas.halpbot.commands.commandadapters.AbstractCommandAdapter;
import nz.pumbas.halpbot.sql.SqlDriver;
import nz.pumbas.halpbot.sql.SqlManager;
import nz.pumbas.halpbot.sql.table.Table;
import nz.pumbas.halpbot.sql.table.TableRow;
import nz.pumbas.halpbot.sql.table.column.ColumnIdentifier;
import nz.pumbas.halpbot.sql.table.column.SimpleColumnIdentifier;
import nz.pumbas.halpbot.utilities.ErrorManager;
import nz.pumbas.halpbot.utilities.HalpbotUtils;

public class ChemmatCommands implements OnReady
{
    private static final ColumnIdentifier<Integer> ID       = new SimpleColumnIdentifier<>("id", Integer.class);
    private static final ColumnIdentifier<String> SUBTOPIC  = new SimpleColumnIdentifier<>("subtopic", String.class);

    private static final ColumnIdentifier<Integer> SUBTOPIC_ID = new SimpleColumnIdentifier<>("subtopicId", Integer.class);
    private static final ColumnIdentifier<String> QUESTION      = new SimpleColumnIdentifier<>("question", String.class);
    private static final ColumnIdentifier<Integer> ANSWER   = new SimpleColumnIdentifier<>("answer", Integer.class);
    private static final ColumnIdentifier<String> OPTIONA   = new SimpleColumnIdentifier<>("optionA", String.class);
    private static final ColumnIdentifier<String> OPTIONB   = new SimpleColumnIdentifier<>("optionB", String.class);
    private static final ColumnIdentifier<String> OPTIONC   = new SimpleColumnIdentifier<>("optionC", String.class);
    private static final ColumnIdentifier<String> OPTIOND   = new SimpleColumnIdentifier<>("optionD", String.class);

    private final Random random = new Random();
    private SqlDriver driver;

    private Table quizNotes;
    private Table subtopics;

    /**
     * A method that is called once after the bot has been initialised.
     *
     * @param event
     *     The JDA {@link ReadyEvent}.
     */
    @Override
    public void onReady(@NotNull ReadyEvent event) {
        this.driver = HalpbotUtils.context()
            .get(SqlManager.class)
            .getDriver("chemmatnotes");

        // Cache the tables
        this.driver.registerLoadInitialision(this::loadDatabase);
    }

    private void loadDatabase(Connection connection) throws SQLException{
        ResultSet resultSet = this.driver.createStatement(connection,
            "SELECT subtopic, question, answer, optionA, optionB, optionC, optionD FROM notes INNER JOIN subtopics " +
                "ON notes.subtopicId = subtopics.id").executeQuery();

        this.quizNotes = new Table(SUBTOPIC, QUESTION, ANSWER, OPTIONA, OPTIONB, OPTIONC, OPTIOND);
        this.quizNotes.populateTable(resultSet);

        resultSet = this.driver.createStatement(connection, "SELECT * FROM subtopics").executeQuery();
        this.subtopics = new Table(ID, SUBTOPIC);
        this.subtopics.populateTable(resultSet);
    }

    @Command(alias = "quiz", description = "Retrieves a random chemmat quiz on the specified subtopic")
    public void quiz(AbstractCommandAdapter commandAdapter,
                     @Source MessageChannel channel,
                     @Remaining String subtopic)
    {
        TableRow row = this.getRandomRow(this.quizNotes.where(SUBTOPIC, subtopic.toLowerCase(Locale.ROOT)));

        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setTitle(HalpbotUtils.capitaliseEachWord(subtopic));
        embedBuilder.setColor(Color.ORANGE);
        embedBuilder.setFooter("Chemmat notes");

        StringBuilder builder = new StringBuilder();
        builder.append(row.value(QUESTION).orNull())
            .append("\n\n");

        row.value(OPTIONA)
            .present(option -> builder.append(":regional_indicator_a: : ").append(option).append('\n'));
        row.value(OPTIONB)
            .present(option -> builder.append(":regional_indicator_b: : ").append(option).append('\n'));
        row.value(OPTIONC)
            .present(option -> builder.append(":regional_indicator_c: : ").append(option).append('\n'));
        row.value(OPTIOND)
            .present(option -> builder.append(":regional_indicator_d: : ").append(option).append('\n'));

        embedBuilder.setDescription(builder.toString());

        int answer = row.value(ANSWER).get();
        channel.sendMessageEmbeds(embedBuilder.build())
            .queue(m -> {
                row.value(OPTIONA).present(v ->
                    commandAdapter.addReactionCallback(m, "\uD83C\uDDE6",
                        e -> this.onAnswerReaction(e, 1, answer)));
                row.value(OPTIONA).present(v ->
                    commandAdapter.addReactionCallback(m, "\uD83C\uDDE7",
                        e -> this.onAnswerReaction(e, 2, answer)));
                row.value(OPTIONA).present(v ->
                    commandAdapter.addReactionCallback(m, "\uD83C\uDDE8",
                        e -> this.onAnswerReaction(e, 3, answer)));
                row.value(OPTIONA).present(v ->
                    commandAdapter.addReactionCallback(m, "\uD83C\uDDE9",
                        e -> this.onAnswerReaction(e, 4, answer)));
        });
    }

    @Command(alias = "subtopics", description = "Retrieves the different subtopics available for the chemmat notes")
    public MessageEmbed subtopics() {
        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setTitle("Chemmat Subtopics");
        embedBuilder.setColor(Color.ORANGE);
        embedBuilder.setFooter("Chemmat notes");

        StringBuilder builder = new StringBuilder();
        List<TableRow> rows = this.subtopics.rows();
        for (int i = 0; i < rows.size(); i++) {
            TableRow row = rows.get(i);
            builder.append(i + 1)
                .append(". ")
                .append(row.value(SUBTOPIC).map(HalpbotUtils::capitaliseEachWord).or("Generic"))
                .append('\n');
        }
        embedBuilder.setDescription(builder.toString());

        return embedBuilder.build();
    }

    @Command(alias = "addQuiz", description = "Adds a new chemmat quiz question to the database")
    public String addQuiz(@Explicit String subtopic, @Explicit String question, int answer,
                          @Explicit String optionA, @Explicit String optionB,
                          @Explicit @Unrequired String optionC, @Explicit @Unrequired String optionD)
    {
        subtopic = subtopic.toLowerCase(Locale.ROOT);
        Table subtopicTable = this.subtopics.where(SUBTOPIC, subtopic);

        if (0 == subtopicTable.count()) {
            return "There doesn't seem to be a subtopic '" + subtopic + "'. Check your spelling or create your own one " +
                "using the **addSubtopic** command";
        }

        int subtopicId = subtopicTable.first().get().value(ID).or(-1);
        this.insertQuizNote(subtopic, subtopicId, question, answer, optionA,
            optionB, optionC, optionD);

        return "Inserted the quiz question to the '" + subtopic + "' subtopic :smiling_face_with_3_hearts:";
    }

    @Command(alias = "addSubtopic", description = "Adds a new chemmat subtopic to the database")
    public String addTopic(@Remaining String subtopic) {
        subtopic = subtopic.toLowerCase(Locale.ROOT);
        Table subtopicTable = this.subtopics.where(SUBTOPIC, subtopic);

        if (0 != subtopicTable.count()) {
            return "There already appears to be a '" + subtopic + "' subtopic";
        }

        if (-1 == this.insertSubtopic(subtopic))
            return "There was an error trying to insert the subtopic :sob:";
        return "Sucessfully created the subtopic '" + subtopic + "' :tada:";
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

    private int insertSubtopic(String subtopic) {
        try (Connection connection = this.driver.createConnection()) {
            this.driver.createStatement(connection,
                "INSERT INTO subtopics (subtopic) VALUES (?)", subtopic).executeUpdate();
            ResultSet resultSet = this.driver.createStatement(connection,
                "SELECT id FROM subtopics WHERE subtopic == ?", subtopic).executeQuery();

            int id = resultSet.getInt("id");
            this.subtopics.addRow(id, subtopic);
            return id;
        } catch (SQLException e) {
            ErrorManager.handle(e);
        }
        return -1;
    }

    private void insertQuizNote(String subtopic, int subtopicId, String question, int answer, String optionA,
                                String optionB, String optionC, String optionD) {
        try (Connection connection = this.driver.createConnection()) {
            String sql = "INSERT INTO notes (subtopicId, question, answer, optionA, optionB, optionC, optionD) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";

            this.driver.createStatement(connection, sql, subtopicId, question, answer,
                optionA, optionB, optionC, optionD).executeUpdate();
            this.quizNotes.addRow(subtopic, question, answer, optionA, optionB, optionC, optionD);
        } catch (SQLException e) {
            ErrorManager.handle(e);
        }
    }
}
