package nz.pumbas.halpbot.hibernate.services;

import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import nz.pumbas.halpbot.hibernate.models.UserStatistics;
import nz.pumbas.halpbot.hibernate.repositories.UserStatisticsRepository;
import nz.pumbas.halpbot.objects.expiring.ConcurrentExpiringMap;
import nz.pumbas.halpbot.objects.expiring.ExpiringMap;

@Service
public class UserStatisticsService
{
    private final ExpiringMap<Long, UserStatistics> userStatistics
        = new ConcurrentExpiringMap<>(30, TimeUnit.MINUTES, this::saveModifiedStatistic);

    private final UserStatisticsRepository userStatisticsRepository;

    @Autowired
    public UserStatisticsService(UserStatisticsRepository userStatisticsRepository) {
        this.userStatisticsRepository = userStatisticsRepository;
    }

    public UserStatistics getByUserId(@NotNull Long userId) {
        if (this.userStatistics.containsKey(userId)) {
            this.userStatistics.renewKey(userId);
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

    private void saveModifiedStatistic(Long userId, UserStatistics userStatistic) {
        if (!userStatistic.isEmpty()) {
            this.userStatisticsRepository.save(userStatistic);
        }
    }

    public void saveModifiedStatistics() {
        this.userStatisticsRepository.saveAll(
            this.userStatistics.values()
                .stream()
                .filter(userStatistic -> !userStatistic.isEmpty())
                .collect(Collectors.toList()));
    }
}
