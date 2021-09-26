package nz.pumbas.halpbot.hibernate.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import nz.pumbas.halpbot.hibernate.models.Question;

public interface QuestionRepository extends JpaRepository<Question, Long>
{
}