package nz.pumbas.halpbot.commands.chemmat;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;

import org.springframework.stereotype.Service;

import java.util.List;

import javax.annotation.PreDestroy;

import nz.pumbas.halpbot.commands.annotations.Command;
import nz.pumbas.halpbot.commands.annotations.Source;
import nz.pumbas.halpbot.commands.annotations.Unrequired;
import nz.pumbas.halpbot.hibernate.models.UserStatistics;
import nz.pumbas.halpbot.hibernate.services.UserStatisticsService;
import nz.pumbas.halpbot.utilities.HalpbotUtils;

@Service
public class QuestionStatisticCommands
{
    private static final int TOP_AMOUNT = 10;
    private final UserStatisticsService userStatisticsService;

    public QuestionStatisticCommands(UserStatisticsService userStatisticsService) {
        this.userStatisticsService = userStatisticsService;
    }

    @Command(description = "Returns the top " + TOP_AMOUNT + " users and their stats for a particular column")
    public MessageEmbed top(JDA jda, @Unrequired("ANSWERED") UserStatColumn column) {
        List<UserStatistics> topUserStatistics = this.userStatisticsService.getTop(TOP_AMOUNT, column.getColumn());
        StringBuilder stringBuilder = new StringBuilder();

        for (int i = 0; i < topUserStatistics.size(); i++) {
            UserStatistics userStatistic = topUserStatistics.get(i);
            stringBuilder.append("**").append(i + 1).append(".** ")
                .append(jda.retrieveUserById(userStatistic.getUserId()).complete().getName())
                .append(" - **").append(column.getValueGetter().apply(userStatistic))
                .append("**\n");
        }
        return new EmbedBuilder()
            .setTitle("Top " + TOP_AMOUNT + " by " + column.getDisplayName())
            .setColor(HalpbotUtils.Blurple)
            .setDescription(stringBuilder.toString())
            .build();
    }

    @Command(description = "Returns the question statistics for a particular user")
    public MessageEmbed stats(@Source User author) {
        UserStatistics userStatistics = this.userStatisticsService.getByUserId(author.getIdLong());

        return new EmbedBuilder()
            .setTitle(author.getName())
            .setThumbnail(author.getEffectiveAvatarUrl())
            .setDescription(userStatistics.toDiscordString())
            .setColor(HalpbotUtils.Blurple)
            .build();
    }

    @PreDestroy
    public void onShutDown() {
        HalpbotUtils.logger().info("Saving modified statistics");
        this.userStatisticsService.saveModifiedStatistics();
    }
}
