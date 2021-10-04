package nz.pumbas.halpbot.hibernate.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import nz.pumbas.halpbot.hibernate.models.UserStatistics;

public interface UserStatisticsRepository extends JpaRepository<UserStatistics, Long>
{
}