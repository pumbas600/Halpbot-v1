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

package net.pumbas.halpbot.hibernate.models;

import net.pumbas.halpbot.objects.DiscordObject;

import java.io.Serializable;

import jakarta.persistence.Id;
import jakarta.persistence.Table;

//@Entity
@Table(name = "USER_STATISTICS")
public class UserStatistics implements DiscordObject, Serializable
{
    public final static int IS_ON_FIRE_THRESHOLD = 5;

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

    public double getCorrectAnswerPercentage() {
        if (0 == this.getQuestionsAnswered()) return 0;
        return 100 * ((double)this.getQuestionsAnsweredCorrectly() / this.getQuestionsAnswered());
    }

    @Override
    public String toDiscordString() {
        return "**" + this.getQuizzesStarted() + "** - Quizzes Started"
            + "\n**" + this.getQuestionsAnswered() + "** - Questions Answered"
            + "\n**" + this.getQuestionsAnsweredCorrectly() + "** - Questions Answered Correctly"
            + "\n**" + this.getBestAnswerStreak() + "** - Best Answer Streak"
            + "\n**" + this.getCurrentAnswerStreak() + "** - Current Answer Streak" + (this.isOnFire() ? " :fire:" : "")
            + String.format("\n**%.2f%%** - Correct Answer Rate", this.getCorrectAnswerPercentage());
    }
}
