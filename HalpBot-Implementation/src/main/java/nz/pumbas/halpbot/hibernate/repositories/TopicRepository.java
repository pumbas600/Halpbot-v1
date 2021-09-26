package nz.pumbas.halpbot.hibernate.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import nz.pumbas.halpbot.hibernate.models.Topic;

public interface TopicRepository extends JpaRepository<Topic, Long>
{
}