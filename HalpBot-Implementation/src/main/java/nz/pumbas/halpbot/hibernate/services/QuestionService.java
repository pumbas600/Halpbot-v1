package nz.pumbas.halpbot.hibernate.services;

import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import nz.pumbas.halpbot.hibernate.exceptions.ResourceAlreadyExistsException;
import nz.pumbas.halpbot.hibernate.exceptions.ResourceNotFoundException;
import nz.pumbas.halpbot.hibernate.models.Question;
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

    public void update(Question question) throws ResourceNotFoundException {
        if (!this.existsById(question.getId())) {
            throw new ResourceNotFoundException("Cannot find Question with the id: " + question.getId());
        }
        this.questionRepository.save(question);
    }

}
