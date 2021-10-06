package nz.pumbas.halpbot.hibernate.models;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import nz.pumbas.halpbot.commands.DiscordString;

@Entity
@Table(name = "USER_STATISTICS")
public class UserStatistics implements DiscordString, Serializable
{
    private final static int IS_ON_FIRE_THRESHOLD = 5;

    @Id
    private Long userId;
    private Long quizzesStarted;
    private Long questionsAnswered;
    private Long questionsAnsweredCorrectly;
    private Long bestAnswerStreak;
    private Long currentAnswerStreak;

    public UserStatistics() {
    }

    public UserStatistics(Long userId) {
        this.userId = userId;
        this.quizzesStarted = 0L;
        this.questionsAnswered = 0L;
        this.questionsAnsweredCorrectly = 0L;
        this.bestAnswerStreak = 0L;
        this.currentAnswerStreak = 0L;
    }

    public Long getUserId() {
        return this.userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getQuizzesStarted() {
        return this.quizzesStarted;
    }

    public void setQuizzesStarted(Long quizzesStarted) {
        this.quizzesStarted = quizzesStarted;
    }

    public Long getQuestionsAnswered() {
        return this.questionsAnswered;
    }

    public void setQuestionsAnswered(Long questionsAnswered) {
        this.questionsAnswered = questionsAnswered;
    }

    public Long getQuestionsAnsweredCorrectly() {
        return this.questionsAnsweredCorrectly;
    }

    public void setQuestionsAnsweredCorrectly(Long questionsAnsweredCorrectly) {
        this.questionsAnsweredCorrectly = questionsAnsweredCorrectly;
        this.currentAnswerStreak++;
        if (this.currentAnswerStreak > this.bestAnswerStreak) {
            this.bestAnswerStreak = this.currentAnswerStreak;
        }
    }

    public Long getBestAnswerStreak() {
        return this.bestAnswerStreak;
    }

    public void setBestAnswerStreak(Long bestAnswerStreak) {
        this.bestAnswerStreak = bestAnswerStreak;
    }

    public Long getCurrentAnswerStreak() {
        return this.currentAnswerStreak;
    }

    public void setCurrentAnswerStreak(Long currentAnswerStreak) {
        this.currentAnswerStreak = currentAnswerStreak;
    }

    public void resetAnswerStreak() {
        this.currentAnswerStreak = 0L;
    }

    public void incrementQuizzesStarted() {
        this.setQuizzesStarted(this.getQuizzesStarted() + 1);
    }

    public void incrementQuestionsAnswered() {
        this.setQuestionsAnswered(this.getQuestionsAnswered() + 1);
    }

    public void incrementQuestionsAnsweredCorrectly() {
        this.setQuestionsAnsweredCorrectly(this.getQuestionsAnsweredCorrectly() + 1);
    }

    public boolean isOnFire() {
        return IS_ON_FIRE_THRESHOLD <= this.currentAnswerStreak;
    }

    public boolean isEmpty() {
        return 0 == this.getQuizzesStarted()
            && 0 == this.getQuestionsAnswered(); // Note that the others will also be 0 if these are both 0.
    }

    @Override
    public String toDiscordString() {
        return "**Quizzes Started** - " + this.getQuizzesStarted()
            + "\n**Questions Answered** - " + this.getQuestionsAnswered()
            + "\n**Questions Answered Correctly** - " + this.getQuestionsAnsweredCorrectly()
            + "\n**Best Answer Streak** - " + this.getBestAnswerStreak()
            + "\n**Current Answer Streak** - " + this.getCurrentAnswerStreak() + (this.isOnFire() ? " :fire:" : "");
    }
}
