package nz.pumbas.halpbot.hibernate.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Set;

import nz.pumbas.halpbot.hibernate.models.Question;

public interface QuestionRepository extends JpaRepository<Question, Long>
{
    //Refer to https://attacomsian.com/blog/spring-data-jpa-query-annotation for @Query examples

    @Query("SELECT q FROM Question q WHERE q.status <> 0")
    List<Question> getAllWaitingConfirmation();

    @Query("SELECT q.id FROM Question q WHERE q.status = 0")
    List<Long> getAllConfirmedIds();

    @Query("SELECT q FROM Question q WHERE q.status <> 0 AND q.id NOT IN ?1")
    List<Question> getAllWaitingConfirmationNotIn(Set<Long> ids);

    @Query("SELECT count(q) FROM Question q WHERE q.status <> 0")
    long countWaitingConfirmation();

    @Modifying
    @Query("DELETE FROM Question q WHERE q.status <> 0")
    void deleteAllWaitingConfirmation();
}