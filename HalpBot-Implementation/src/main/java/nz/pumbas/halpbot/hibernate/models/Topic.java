package nz.pumbas.halpbot.hibernate.models;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "TOPICS")
public class Topic
{

    @Id
    @GeneratedValue
    private Long id;
    private String topic;

    public Topic() {}

    public Topic(Long id, String topic) {
        this.id = id;
        this.topic = topic;
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTopic() {
        return this.topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }
}
