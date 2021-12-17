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

package nz.pumbas.halpbot.hibernate.services;

import org.dockbox.hartshorn.core.annotations.service.Service;
import org.dockbox.hartshorn.core.domain.Exceptional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import nz.pumbas.halpbot.hibernate.models.Topic;
import nz.pumbas.halpbot.hibernate.repositories.TopicRepository;

@Service
public class TopicService
{
    private final Map<Long, String> topicMappings = new HashMap<>();
    private final TopicRepository topicRepository;

    //@Autowired
    public TopicService(TopicRepository topicRepository) {
        this.topicRepository = topicRepository;
        this.list().forEach(topic -> this.topicMappings.put(topic.getId(), topic.getTopic()));
    }

    public boolean existsById(Long id) {
        //return null != id && this.topicRepository.existsById(id);
        return false;
    }

    public List<Topic> list() {
        //return this.topicRepository.findAll();
        return new ArrayList<>();
    }

    public String topicFromId(Long id) {
        return this.topicMappings.getOrDefault(id, "INVALID ID");
    }

    public Exceptional<Long> getIdFromTopic(String topic) {
        return this.topicRepository.getFirstIdFromTopic(topic.toLowerCase(Locale.ROOT));
    }
}
