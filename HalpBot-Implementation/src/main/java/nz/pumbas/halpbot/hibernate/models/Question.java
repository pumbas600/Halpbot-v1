package nz.pumbas.halpbot.hibernate.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

//@Entity
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
    private Status status;
    private Long editedId;
    private final String dType = this.getClass().getSimpleName();

    public Question() {  }

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

    public Status getStatus() {
        return this.status;
    }

    public Long getEditedId() {
        return this.editedId;
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

    public void setEditedId(Long editedId) {
        this.editedId = editedId;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public List<String> getShuffledOptions(Random random) {
        List<String> options = new ArrayList<>();
        options.add(this.answer);
        options.add(this.optionB);

        if (null != this.optionC)
            options.add(this.optionC);
        if (null != this.optionD)
            options.add(this.optionD);

        Collections.shuffle(options, random);
        return options;
    }

    public void setEmptyFieldsNull() {
        if (this.optionC.isEmpty())
            this.optionC = null;
        if (this.optionD.isEmpty())
            this.optionD = null;
        if (this.explanation.isEmpty())
            this.explanation = null;
        if (this.image.isEmpty())
            this.image = null;
    }
}
