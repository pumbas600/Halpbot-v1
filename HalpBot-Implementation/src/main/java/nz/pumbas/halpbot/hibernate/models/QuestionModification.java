package nz.pumbas.halpbot.hibernate.models;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

//@Entity
@Table(name = "QUESTION_MODIFICATIONS")
public class QuestionModification implements Serializable
{
    @Id
    @GeneratedValue
    private Long id;
    private Long modifiedQuestionId;
    private Long topicId;
    private String question;
    private String answer;
    private String optionB;
    private String optionC;
    private String optionD;
    private String explanation;
    private String image;
    private Modification modification;
    private final String dType = this.getClass().getSimpleName();

    public QuestionModification() {}

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getModifiedQuestionId() {
        return this.modifiedQuestionId;
    }

    public void setModifiedQuestionId(Long modifiedQuestionId) {
        this.modifiedQuestionId = modifiedQuestionId;
    }

    public Long getTopicId() {
        return this.topicId;
    }

    public void setTopicId(Long topicId) {
        this.topicId = topicId;
    }

    public String getQuestion() {
        return this.question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public String getAnswer() {
        return this.answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public String getOptionB() {
        return this.optionB;
    }

    public void setOptionB(String optionB) {
        this.optionB = optionB;
    }

    public String getOptionC() {
        return this.optionC;
    }

    public void setOptionC(String optionC) {
        this.optionC = optionC;
    }

    public String getOptionD() {
        return this.optionD;
    }

    public void setOptionD(String optionD) {
        this.optionD = optionD;
    }

    public String getExplanation() {
        return this.explanation;
    }

    public void setExplanation(String explanation) {
        this.explanation = explanation;
    }

    public String getImage() {
        return this.image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getdType() {
        return this.dType;
    }

    public Modification getModification() {
        return this.modification;
    }

    public void setModification(Modification modification) {
        this.modification = modification;
    }
}
