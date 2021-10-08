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

package nz.pumbas.halpbot.hibernate.repositories;

import org.springframework.data.domain.Pageable;
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

    @Query("SELECT q FROM Question q WHERE q.status <> 0")
    List<Question> getAllWaitingConfirmation(Pageable pageable);

    @Query("SELECT q.id FROM Question q WHERE q.status = 0")
    List<Long> getAllConfirmedIds();

    @Query("SELECT q FROM Question q WHERE q.status <> 0 AND q.id NOT IN ?1")
    List<Question> getAllWaitingConfirmationNotIn(Set<Long> ids);

    @Query("SELECT count(q) FROM Question q WHERE q.status <> 0")
    long countWaitingConfirmation();

    @Query("SELECT q.id FROM Question q WHERE q.status = 0 AND q.topicId in ?1")
    List<Long> getAllConfirmedIdsInTopicIds(Set<Long> topicIds);

    @Query("SELECT q.id FROM Question q WHERE q.status = 0 AND q.topicId = ?1")
    List<Long> getAllConfirmedIdsByTopicId(Long topicId);

    @Modifying
    @Query("DELETE FROM Question q WHERE q.status <> 0")
    void deleteAllWaitingConfirmation();
}