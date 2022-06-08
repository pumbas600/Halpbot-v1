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

package net.pumbas.halpbot.hibernate.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

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
