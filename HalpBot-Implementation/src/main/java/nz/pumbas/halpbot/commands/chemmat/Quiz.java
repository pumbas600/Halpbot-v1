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

package nz.pumbas.halpbot.commands.chemmat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Quiz
{
    private int id;
    private String topic;
    private String question;
    private int answer;
    private String optionA;
    private String optionB;
    private String optionC;
    private String optionD;
    private String image;
    private String explanation;

    public void shuffleAnswers() {
        List<String> options = this.getOptions();
        String correctAnswer = options.get(this.answer - 1);
        Collections.shuffle(options);

        this.answer = options.indexOf(correctAnswer) + 1;
        this.optionA = options.get(0);
        this.optionB = options.get(1);
        this.optionC = options.get(2);
        this.optionD = options.get(3);
    }

    public List<String> getOptions() {
        List<String> options = new ArrayList<>();
        options.add(this.optionA);
        options.add(this.optionB);
        options.add(this.optionC);
        options.add(this.optionD);
        return options;
    }


    public int getId() {
        return this.id;
    }

    public String getTopic() {
        return (null == this.topic) ? "Generic" : this.topic;
    }

    public String getQuestion() {
        return this.question;
    }

    public int getAnswer() {
        return this.answer;
    }

    public String getOptionA() {
        return this.optionA;
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

    public String getImage() {
        return this.image;
    }

    public String getExplanation() {
        return this.explanation;
    }
}
