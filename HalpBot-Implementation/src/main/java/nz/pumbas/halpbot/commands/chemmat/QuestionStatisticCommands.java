package nz.pumbas.halpbot.commands.chemmat;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;

import java.awt.Color;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import nz.pumbas.halpbot.commands.annotations.Command;
import nz.pumbas.halpbot.converters.annotations.parameter.Source;
import nz.pumbas.halpbot.converters.annotations.parameter.Unrequired;
import nz.pumbas.halpbot.hibernate.models.UserStatistics;
import nz.pumbas.halpbot.hibernate.services.UserStatisticsService;
import nz.pumbas.halpbot.objects.expiring.ConcurrentExpiringMap;
import nz.pumbas.halpbot.objects.expiring.ExpiringMap;
import nz.pumbas.halpbot.utilities.HalpbotUtils;

public class QuestionStatisticCommands
{
    private final ExpiringMap<Long, String> cachedUsernames = new ConcurrentExpiringMap<>(1, TimeUnit.HOURS);
    private static final int TOP_AMOUNT = 10;
    private final UserStatisticsService userStatisticsService;

    public QuestionStatisticCommands(UserStatisticsService userStatisticsService) {
        this.userStatisticsService = userStatisticsService;
    }

    @Command(description = "Returns the top " + TOP_AMOUNT + " users and their stats for a particular column")
    public Object top(@Source MessageChannel channel, JDA jda, @Unrequired("Answered") UserStatColumn column) {
        this.userStatisticsService.saveModifiedStatistics();
        List<UserStatistics> topUserStatistics = this.userStatisticsService.getTop(TOP_AMOUNT, column.getColumn());

        if (topUserStatistics.isEmpty())
            return "There was an error retrieving the user statistics :sob:";

        List<String> usernames = topUserStatistics.stream()
            .map(statistic -> {
                if (!this.cachedUsernames.containsKey(statistic.getUserId())) {
                    User user = jda.retrieveUserById(statistic.getUserId()).complete();
                    this.cachedUsernames.put(statistic.getUserId(), user.getName());
                }
                return this.cachedUsernames.get(statistic.getUserId());
            })
            .collect(Collectors.toList());

        EmbedBuilder embedBuilder = new EmbedBuilder()
            .setTitle("Top " + TOP_AMOUNT + " by " + column.getDisplayName())
            .setColor(HalpbotUtils.Blurple);

        for (int i = 0; i < topUserStatistics.size(); i++) {
            UserStatistics userStatistic = topUserStatistics.get(i);

            if (0 == i)
                embedBuilder.appendDescription(":crown: ");
            else
                embedBuilder.appendDescription("** ").appendDescription("" + (i + 1)).appendDescription(".  **");

            embedBuilder.appendDescription(usernames.get(i))
                .appendDescription(" - **").appendDescription("" + column.getValueGetter().apply(userStatistic))
                .appendDescription("**\n");
        }
        return embedBuilder.build();
    }

    @Command(description = "Returns the cumulative stats for quizzes started, questions answered and questions " +
                           "answered correctly")
    public MessageEmbed cumulativeStats() {
        List<UserStatistics> userStatistics = this.userStatisticsService.list();
        int uniqueUsers = userStatistics.size();

        int quizzesStarted = 0;
        int questionsAnswered = 0;
        int questionsAnsweredCorrectly = 0;

        for (UserStatistics statistic : userStatistics) {
            quizzesStarted += statistic.getQuizzesStarted();
            questionsAnswered += statistic.getQuestionsAnswered();
            questionsAnsweredCorrectly += statistic.getQuestionsAnsweredCorrectly();
        }

        EmbedBuilder builder = new EmbedBuilder();
        builder.setTitle("Cumulative Statistics");
        builder.setColor(Color.ORANGE);
        builder.appendDescription("Unique users: ").appendDescription("**" + uniqueUsers).appendDescription("**\n")
            .appendDescription("Quizzes started: ").appendDescription("**" + quizzesStarted).appendDescription("**\n")
            .appendDescription("Questions answered: ").appendDescription("**" + questionsAnswered).appendDescription("**\n")
            .appendDescription("Questions answered correctly: ").appendDescription("**" + questionsAnsweredCorrectly).appendDescription("**");

        return builder.build();
    }

    @Command(description = "Returns the question statistics for a particular user")
    public MessageEmbed stats(@Source User author, @Unrequired @Nullable User user) {
        return this.buildUserStatisticsEmbed(null == user ? author : user);
    }

    private MessageEmbed buildUserStatisticsEmbed(User user) {
        UserStatistics userStatistics = this.userStatisticsService.getByUserId(user.getIdLong());
        return new EmbedBuilder()
            .setTitle(user.getName())
            .setThumbnail(user.getEffectiveAvatarUrl())
            .setDescription(userStatistics.toDiscordString())
            .setColor(HalpbotUtils.Blurple)
            .build();
    }

//    @PreDestroy
//    public void onShutDown() {
//        //HalpbotUtils.logger().info("Saving modified statistics");
//        this.userStatisticsService.saveModifiedStatistics();
//    }
}
