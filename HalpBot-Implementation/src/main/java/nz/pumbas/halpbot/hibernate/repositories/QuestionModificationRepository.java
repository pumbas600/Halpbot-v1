package nz.pumbas.halpbot.hibernate.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Set;

import nz.pumbas.halpbot.hibernate.models.QuestionModification;

public interface QuestionModificationRepository extends JpaRepository<QuestionModification, Long>
{
    @Query("SELECT q.id FROM QuestionModification q")
    List<Long> getAllIds();

    @Query("SELECT q FROM QuestionModification q WHERE q.id NOT IN :ids")
    List<QuestionModification> getAllNotInIds(@Param("ids") Set<Long> ids);
}