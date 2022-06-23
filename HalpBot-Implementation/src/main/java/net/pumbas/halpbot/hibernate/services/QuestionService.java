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

package net.pumbas.halpbot.hibernate.services;

import net.pumbas.halpbot.hibernate.exceptions.ResourceAlreadyExistsException;
import net.pumbas.halpbot.hibernate.exceptions.ResourceNotFoundException;
import net.pumbas.halpbot.hibernate.models.Question;
import net.pumbas.halpbot.hibernate.repositories.QuestionRepository;

import org.dockbox.hartshorn.component.Service;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class QuestionService
{
    private QuestionRepository questionRepository;

    public QuestionService() { }

    //@Autowired
    public QuestionService(QuestionRepository questionRepository) {
        this.questionRepository = questionRepository;
    }

    public boolean existsById(@Nullable Long id) {
        //return null != id && this.questionRepository.existsById(id);
        return false;
    }

    public List<Question> list() {
        //return this.questionRepository.findAll();
        return new ArrayList<>();
    }

    public List<Question> getAllWaitingConfirmation() {
        return this.questionRepository.getAllWaitingConfirmation();
    }

    public List<Question> getAmountWaitingConfirmation(int amount) {
        //return this.questionRepository.getAllWaitingConfirmation(PageRequest.of(0, amount));
        return new ArrayList<>();
    }

    public List<Long> getAllConfirmedIds() {
        return this.questionRepository.getAllConfirmedIds();
    }

    public List<Question> getAllConfirmedQuestions() {
        return this.questionRepository.getAllConfirmedQuestions();
    }

    public List<Long> getAllConfirmedIdsInTopicIds(Set<Long> topicIds) {
        return this.questionRepository.getAllConfirmedIdsInTopicIds(topicIds);
    }

    public List<Question> getAllWaitingConfirmationNotIn(Set<Long> ids) {
        if (ids.isEmpty())
            return this.getAllWaitingConfirmation();
        return this.questionRepository.getAllWaitingConfirmationNotIn(ids);
    }

    public List<Long> getAllConfirmedIdsByTopicId(Long topicId) {
        return this.questionRepository.getAllConfirmedIdsByTopicId(topicId);
    }

    public long countWaitingConfirmation() {
        return this.questionRepository.countWaitingConfirmation();
    }

    public void deleteAllWaitingConfirmation() {
        this.questionRepository.deleteAllWaitingConfirmation();
    }

    public Question getById(Long id) throws ResourceNotFoundException {
        //Question question = this.questionRepository.findById(id).orElse(null);
//        if (null == question) {
//            throw new ResourceNotFoundException("Cannot find Question with the id: " + id);
//        }
//        return question;
        return new Question();
    }

    public Question save(Question question) throws ResourceAlreadyExistsException {
        if (this.existsById(question.getId())) {
            throw new ResourceAlreadyExistsException("Question with id: " + question.getId() + " Already exists");
        }
//        return this.questionRepository.save(question);
        return new Question();
    }

    public void bulkSave(List<Question> questions) {
        List<Question> filteredQuestions = questions.stream()
            .filter(q -> !this.existsById(q.getId()))
            .collect(Collectors.toList());
        //this.questionRepository.saveAll(filteredQuestions);
    }

    public void update(Question question) throws ResourceNotFoundException {
        if (!this.existsById(question.getId())) {
            throw new ResourceNotFoundException("Cannot find Question with the id: " + question.getId());
        }
        //this.questionRepository.save(question);
    }

    public void deleteById(@Nullable Long id) throws ResourceNotFoundException {
        if (!this.existsById(id)) {
            throw new ResourceNotFoundException("Cannot find a Question with the id: " + id);
        }
        //this.questionRepository.deleteById(id);
    }
}
