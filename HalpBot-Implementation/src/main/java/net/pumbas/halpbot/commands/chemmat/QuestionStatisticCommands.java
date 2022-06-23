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
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.pumbas.halpbot.commands.annotations.Command;
import net.pumbas.halpbot.converters.annotations.parameter.Source;
import net.pumbas.halpbot.converters.annotations.parameter.Unrequired;
import net.pumbas.halpbot.hibernate.models.UserStatistics;
import net.pumbas.halpbot.hibernate.services.UserStatisticsService;
import net.pumbas.halpbot.objects.expiring.ConcurrentExpiringMap;
import net.pumbas.halpbot.objects.expiring.ExpiringMap;
import net.pumbas.halpbot.utilities.HalpbotUtils;

import java.awt.Color;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

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
