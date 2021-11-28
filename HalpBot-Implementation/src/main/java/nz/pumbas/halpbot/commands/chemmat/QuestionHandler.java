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

package nz.pumbas.halpbot.commands.chemmat;

import org.dockbox.hartshorn.core.domain.Exceptional;

import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Set;

import nz.pumbas.halpbot.hibernate.models.Question;
import nz.pumbas.halpbot.hibernate.services.QuestionService;

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
