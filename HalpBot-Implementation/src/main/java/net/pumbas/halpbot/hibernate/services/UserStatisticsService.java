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

package net.pumbas.halpbot.hibernate.services;

import net.pumbas.halpbot.hibernate.models.UserStatistics;
import net.pumbas.halpbot.hibernate.repositories.UserStatisticsRepository;
import net.pumbas.halpbot.objects.expiring.ConcurrentExpiringMap;
import net.pumbas.halpbot.objects.expiring.ExpiringMap;

import org.dockbox.hartshorn.core.annotations.stereotype.Service;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class UserStatisticsService
{
    private final ExpiringMap<Long, UserStatistics> userStatistics
        = new ConcurrentExpiringMap<>(30, TimeUnit.MINUTES, this::saveModifiedStatistic);

    private final UserStatisticsRepository userStatisticsRepository;

    public UserStatisticsService() {
        this.userStatisticsRepository = null;
    }

    //@Autowired
    public UserStatisticsService(UserStatisticsRepository userStatisticsRepository) {
        this.userStatisticsRepository = userStatisticsRepository;
    }

    public UserStatistics getByUserId(@NotNull Long userId) {
        if (this.userStatistics.containsKey(userId)) {
            this.userStatistics.renewKey(userId);
            return this.userStatistics.get(userId);
        }
//        UserStatistics userStatistics = this.userStatisticsRepository.findById(userId)
//            .orElse(new UserStatistics(userId));
//        this.userStatistics.put(userId, userStatistics);
//        return userStatistics;
        return new UserStatistics();
    }

    public List<UserStatistics> getTop(int amount, String column) {
//        PageRequest pageRequest = PageRequest.of(0, amount, Sort.by(column).descending());
//        return this.userStatisticsRepository.findAll(pageRequest).getContent();
        return new ArrayList<>();
    }

    public List<UserStatistics> list() {
        //return this.userStatisticsRepository.findAll();
        return new ArrayList<>();
    }

    private void saveModifiedStatistic(Long userId, UserStatistics userStatistic) {
        if (!userStatistic.isEmpty()) {
            //this.userStatisticsRepository.save(userStatistic);
        }
    }

    public void saveModifiedStatistics() {
//        this.userStatisticsRepository.saveAll(
//            this.userStatistics.values()
//                .stream()
//                .filter(userStatistic -> !userStatistic.isEmpty())
//                .collect(Collectors.toList()));
    }
}
