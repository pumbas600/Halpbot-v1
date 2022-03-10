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

import net.pumbas.halpbot.hibernate.models.UserStatistics;

import java.util.function.Function;

public enum UserStatColumn
{
    STARTED("quizzesStarted", "Quizzes Started", UserStatistics::getQuizzesStarted),
    ANSWERED("questionsAnswered", "Questions Answered", UserStatistics::getQuestionsAnswered),
    CORRECT("questionsAnsweredCorrectly", "Questions Answered Correctly",
        UserStatistics::getQuestionsAnsweredCorrectly),
    STREAK("bestAnswerStreak", "Best Answer Streak", UserStatistics::getBestAnswerStreak),
    ;

    private final String column;
    private final String displayName;
    private final Function<UserStatistics, Long> valueGetter;

    UserStatColumn(String column, String displayName, Function<UserStatistics, Long> valueGetter) {
        this.column = column;
        this.displayName = displayName;
        this.valueGetter = valueGetter;
    }

    public String getColumn() {
        return this.column;
    }

    public String getDisplayName() {
        return this.displayName;
    }

    public Function<UserStatistics, Long> getValueGetter() {
        return this.valueGetter;
    }
}
