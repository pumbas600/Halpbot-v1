package nz.pumbas.halpbot.commands.chemmat;

import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Set;

import nz.pumbas.halpbot.hibernate.models.Question;
import nz.pumbas.halpbot.hibernate.services.QuestionService;
import nz.pumbas.halpbot.objects.Exceptional;

public class QuestionHandler
{
    private final QuestionService questionService;
    private final Random random;
    private final Set<Long> topicIds;
    private List<Long> questionIds;
    private int currentQuestionIndex;

    public QuestionHandler(QuestionService questionService, Random random, Long... topicIds) {
        this(questionService, random, Set.of(topicIds));
    }

    public QuestionHandler(QuestionService questionService, Random random, Set<Long> topicIds) {
        this.questionService = questionService;
        this.random = random;
        this.topicIds = topicIds;
        this.shuffleQuestions();
    }

    public void shuffleQuestions() {
        if (this.topicIds.isEmpty())
            this.questionIds = this.questionService.getAllConfirmedIds();
        else {
            this.questionIds = this.questionService.getAllConfirmedIdsInTopicIds(this.topicIds);
        }
        Collections.shuffle(this.questionIds, this.random);
    }

    public Exceptional<Question> getNextQuestion() {
        if (this.currentQuestionIndex >= this.questionIds.size()) {
            this.shuffleQuestions();
            this.currentQuestionIndex = 0;
        }
        return Exceptional.of(() -> this.questionService.getById(this.questionIds.get(this.currentQuestionIndex++)));
    }
}
