package nz.pumbas.halpbot.hibernate.services;

import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

import nz.pumbas.halpbot.hibernate.exceptions.ResourceNotFoundException;
import nz.pumbas.halpbot.hibernate.models.QuestionModification;
import nz.pumbas.halpbot.hibernate.repositories.QuestionModificationRepository;

@Service
public class QuestionModificationService
{
    private final QuestionModificationRepository questionModificationRepository;

    @Autowired
    public QuestionModificationService(QuestionModificationRepository questionModificationRepository) {
        this.questionModificationRepository = questionModificationRepository;
    }

    public boolean existsById(@Nullable Long id) {
        return null != id && this.questionModificationRepository.existsById(id);
    }

    public List<QuestionModification> listFirstAmount(int amount) {
        List<QuestionModification> questions = new ArrayList<>();
        Pageable pageable = PageRequest.of(1, amount);
        this.questionModificationRepository.findAll(pageable).forEach(questions::add);
        return questions;
    }

    public void deleteById(@Nullable Long id) throws ResourceNotFoundException {
        if (!this.existsById(id)) {
            throw new ResourceNotFoundException("Cannot find question modification with id: " + id);
        }
        this.questionModificationRepository.deleteById(id);
    }

    public void deleteAll() {
        this.questionModificationRepository.deleteAll();
    }

    public List<QuestionModification> list() {
        return this.questionModificationRepository.findAll();
    }

    public long count() {
        return this.questionModificationRepository.count();
    }
}
