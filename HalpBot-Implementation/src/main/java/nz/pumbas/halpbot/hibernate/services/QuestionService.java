package nz.pumbas.halpbot.hibernate.services;

import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import nz.pumbas.halpbot.hibernate.exceptions.ResourceAlreadyExistsException;
import nz.pumbas.halpbot.hibernate.exceptions.ResourceNotFoundException;
import nz.pumbas.halpbot.hibernate.models.Question;
import nz.pumbas.halpbot.hibernate.models.Status;
import nz.pumbas.halpbot.hibernate.repositories.QuestionRepository;

@Service
public class QuestionService
{
    private final QuestionRepository questionRepository;

    @Autowired
    public QuestionService(QuestionRepository questionRepository) {
        this.questionRepository = questionRepository;
    }

    public boolean existsById(@Nullable Long id) {
        return null != id && this.questionRepository.existsById(id);
    }

    public List<Question> list() {
        return this.questionRepository.findAll();
    }

    public List<Question> getAllWaitingConfirmation() {
        return this.questionRepository.getAllWaitingConfirmation();
    }

    public List<Long> getAllConfirmedIds() {
        return this.questionRepository.getAllConfirmedIds();
    }

    public List<Question> getAllWaitingConfirmationNotIn(Set<Long> ids) {
        if (ids.isEmpty())
            return this.getAllWaitingConfirmation();
        return this.questionRepository.getAllWaitingConfirmationNotIn(ids);
    }

    public long countWaitingConfirmation() {
        return this.questionRepository.countWaitingConfirmation();
    }

    public void deleteAllWaitingConfirmation() {
        this.questionRepository.deleteAllWaitingConfirmation();
    }

    public Question getById(Long id) throws ResourceNotFoundException {
        Question question = this.questionRepository.findById(id).orElse(null);
        if (null == question) {
            throw new ResourceNotFoundException("Cannot find Question with the id: " + id);
        }
        return question;
    }

    public Question save(Question question) throws ResourceAlreadyExistsException {
        if (this.existsById(question.getId())) {
            throw new ResourceAlreadyExistsException("Question with id: " + question.getId() + " Already exists");
        }
        return this.questionRepository.save(question);
    }

    public void bulkSave(List<Question> questions) {
        List<Question> filteredQuestions = questions.stream()
            .filter(q -> !this.existsById(q.getId()))
            .collect(Collectors.toList());
        this.questionRepository.saveAll(filteredQuestions);
    }

    public void update(Question question) throws ResourceNotFoundException {
        if (!this.existsById(question.getId())) {
            throw new ResourceNotFoundException("Cannot find Question with the id: " + question.getId());
        }
        this.questionRepository.save(question);
    }

    public void deleteById(@Nullable Long id) throws ResourceNotFoundException {
        if (!this.existsById(id)) {
            throw new ResourceNotFoundException("Cannot find a Question with the id: " + id);
        }
        this.questionRepository.deleteById(id);
    }
}
