package nz.pumbas.halpbot.hibernate.services;

import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import nz.pumbas.halpbot.hibernate.models.UserStatistics;
import nz.pumbas.halpbot.hibernate.repositories.UserStatisticsRepository;

@Service
public class UserStatisticsService
{
    private final Map<Long, UserStatistics> userStatistics = new HashMap<>();
    private final UserStatisticsRepository userStatisticsRepository;

    @Autowired
    public UserStatisticsService(UserStatisticsRepository userStatisticsRepository) {
        this.userStatisticsRepository = userStatisticsRepository;
    }

    public UserStatistics getByUserId(@NotNull Long userId) {
        if (this.userStatistics.containsKey(userId)) {
            return this.userStatistics.get(userId);
        }
        UserStatistics userStatistics = this.userStatisticsRepository.findById(userId)
            .orElse(new UserStatistics(userId));
        this.userStatistics.put(userId, userStatistics);
        return userStatistics;
    }

    public List<UserStatistics> getTop(int amount, String column) {
        PageRequest pageRequest = PageRequest.of(0, amount, Sort.by(column).descending());
        return this.userStatisticsRepository.findAll(pageRequest).getContent();
    }

    public void saveModifiedStatistics() {
        this.userStatisticsRepository.saveAll(
            this.userStatistics.values()
                .stream()
                .filter(userStatistic -> !userStatistic.isEmpty())
                .collect(Collectors.toList()));
    }
}
