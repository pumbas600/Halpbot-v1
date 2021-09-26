package nz.pumbas.halpbot.hibernate.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

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

    public List<QuestionModification> listFirstAmount(int amount) {
        List<QuestionModification> questions = new ArrayList<>();
        Pageable pageable = PageRequest.of(1, amount);
        this.questionModificationRepository.findAll(pageable).forEach(questions::add);
        return questions;
    }

    public List<QuestionModification> list() {
        return this.questionModificationRepository.findAll();
    }

    public long count() {
        return this.questionModificationRepository.count();
    }
}
