package nz.pumbas.halpbot.hibernate.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import nz.pumbas.halpbot.hibernate.models.QuestionModification;

public interface QuestionModificationRepository extends JpaRepository<QuestionModification, Long>
{
}