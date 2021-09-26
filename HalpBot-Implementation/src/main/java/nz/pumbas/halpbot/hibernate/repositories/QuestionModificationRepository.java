package nz.pumbas.halpbot.hibernate.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

import nz.pumbas.halpbot.hibernate.models.QuestionModification;

public interface QuestionModificationRepository extends PagingAndSortingRepository<QuestionModification, Long>,
    JpaRepository<QuestionModification, Long>
{
}