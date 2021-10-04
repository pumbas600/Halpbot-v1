package nz.pumbas.halpbot.commands.chemmat;

import java.util.function.Function;

import nz.pumbas.halpbot.hibernate.models.UserStatistics;

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
