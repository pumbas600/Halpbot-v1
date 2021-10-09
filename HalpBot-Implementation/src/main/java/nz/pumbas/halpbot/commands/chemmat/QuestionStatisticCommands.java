package nz.pumbas.halpbot.commands.chemmat;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.requests.RestAction;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;
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
    public @Nullable String top(@Source MessageChannel channel, JDA jda, @Unrequired("Answered") UserStatColumn column) {
        this.userStatisticsService.saveModifiedStatistics();
        List<UserStatistics> topUserStatistics = this.userStatisticsService.getTop(TOP_AMOUNT, column.getColumn());

        if (topUserStatistics.isEmpty())
            return "There was an error retrieving the user statistics :sob:";

        RestAction<List<String>> restAction = jda.retrieveUserById(topUserStatistics.get(0).getUserId())
            .map(user -> {
                List<String> usernames = new ArrayList<>();
                usernames.add(user.getName());
                return usernames;
            });

        for (int i = 1; i < topUserStatistics.size(); i++) {
            restAction = restAction.and(jda.retrieveUserById(topUserStatistics.get(i).getUserId()),
                (usernames, newUser) -> { usernames.add(newUser.getName()); return usernames; });
        }
        restAction.queue(usernames -> {
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
            channel.sendMessageEmbeds(embedBuilder.build()).queue();
        });
        return null;
    }

    @Command(description = "Returns the question statistics for a particular user")
    public MessageEmbed stats(@Source User author, @Unrequired User user) {
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

    @PreDestroy
    public void onShutDown() {
        HalpbotUtils.logger().info("Saving modified statistics");
        this.userStatisticsService.saveModifiedStatistics();
    }
}
