package nz.pumbas.halpbot.hibernate.models;

import java.io.Serializable;
import java.util.Objects;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "QUESTIONS")
public class Question implements Serializable
{
    @Id
    @GeneratedValue
    private Long id;
    private Long topicId;
    private String question;
    private String answer;
    private String optionB;
    private String optionC;
    private String optionD;
    private String explanation;
    private String image;
    private final String dType = this.getClass().getSimpleName();

    public Question() {}

    public Question(Long id, Long topicId, String question, String answer, String optionB, String optionC,
                    String optionD, String explanation, String image) {
        this.id = id;
        this.topicId = topicId;
        this.question = question;
        this.answer = answer;
        this.optionB = optionB;
        this.optionC = optionC;
        this.optionD = optionD;
        this.explanation = explanation;
        this.image = image;
    }

    public Long getId() {
        return this.id;
    }

    public Long getTopicId() {
        return this.topicId;
    }

    public String getQuestion() {
        return this.question;
    }

    public String getAnswer() {
        return this.answer;
    }

    public String getOptionB() {
        return this.optionB;
    }

    public String getOptionC() {
        return this.optionC;
    }

    public String getOptionD() {
        return this.optionD;
    }

    public String getExplanation() {
        return this.explanation;
    }

    public String getImage() {
        return this.image;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setTopicId(Long topicId) {
        this.topicId = topicId;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public void setOptionB(String optionB) {
        this.optionB = optionB;
    }

    public void setOptionC(String optionC) {
        this.optionC = optionC;
    }

    public void setOptionD(String optionD) {
        this.optionD = optionD;
    }

    public void setExplanation(String explanation) {
        this.explanation = explanation;
    }

    public void setImage(String image) {
        this.image = image;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (null == o || this.getClass() != o.getClass()) return false;
        Question question1 = (Question) o;
        return Objects.equals(this.id, question1.id)
            && Objects.equals(this.topicId, question1.topicId)
            && Objects.equals(this.question, question1.question)
            && Objects.equals(this.answer, question1.answer)
            && Objects.equals(this.optionB, question1.optionB)
            && Objects.equals(this.optionC, question1.optionC)
            && Objects.equals(this.optionD, question1.optionD)
            && Objects.equals(this.explanation, question1.explanation)
            && Objects.equals(this.image, question1.image);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id, this.topicId, this.question, this.answer, this.optionB, this.optionC,
            this.optionD, this.explanation, this.image);
    }
}
