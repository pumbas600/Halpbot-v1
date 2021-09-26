package nz.pumbas.halpbot.hibernate.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import nz.pumbas.halpbot.hibernate.models.Topic;
import nz.pumbas.halpbot.hibernate.repositories.TopicRepository;;

@Service
public class TopicService
{
    private final Map<Long, String> topicMappings = new HashMap<>();
    private final TopicRepository topicRepository;

    @Autowired
    public TopicService(TopicRepository topicRepository) {
        this.topicRepository = topicRepository;
        this.list().forEach(topic -> this.topicMappings.put(topic.getId(), topic.getTopic()));
    }

    public boolean existsById(Long id) {
        return null != id && this.topicRepository.existsById(id);
    }

    public List<Topic> list() {
        return this.topicRepository.findAll();
    }

    public String topicFromId(Long id) {
        return this.topicMappings.getOrDefault(id, "INVALID ID");
    }
}
