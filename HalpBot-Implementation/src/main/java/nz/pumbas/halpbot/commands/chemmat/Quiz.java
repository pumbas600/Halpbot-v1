package nz.pumbas.halpbot.commands.chemmat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Quiz
{
    private String topic;
    private String question;
    private int answer;
    private String optionA;
    private String optionB;
    private String optionC;
    private String optionD;
    private String image;

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

    public String getTopic() {
        return this.topic;
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
}
