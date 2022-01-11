package nz.pumbas.halpbot.commands.games;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class BlackjackSet
{
    public static final int TARGET = 21;
    public static final int STARTING_CARDS = 2;

    private final List<Card> cards = new ArrayList<>();
    private List<Integer> values = new ArrayList<>();
    private int value;

    public BlackjackSet() {
        this.values.add(0);
    }

    public boolean is21() {
        return this.value() == TARGET;
    }

    public boolean exceeds21() {
        return this.value() > TARGET;
    }

    public boolean gameover() {
        return this.value() >= TARGET;
    }

    public void hit() {
        Card card = Card.random();
        this.cards.add(card);

        if (card.isAce()) {
            List<Integer> tempValues = new ArrayList<>(this.values);
            this.values.clear();
            this.values.addAll(tempValues.stream().map((value) -> value + 11).toList());
            this.values.addAll(tempValues.stream().map((value) -> value + card.value()).toList());
        }
        else this.values = this.values.stream().map((value) -> value + card.value()).toList();

        this.value = this.findValue();
    }

    private int findValue() {
        if (this.values.contains(TARGET))
            return TARGET;

        int min = this.values.get(0);
        for (int i = 1; i < this.values.size(); i++) {
            if (this.values.get(i) < min)
                min = this.values.get(i);
        }
        return min;
    }

    public int value() {
        return this.value;
    }

    public String cardsString() {
        if(this.cards.size() <= STARTING_CARDS)
            return this.cards.stream().map(Card::name).collect(Collectors.joining(" "));

        StringBuilder result = new StringBuilder();
        int index = 0;
        for (Card card : this.cards) {
            if (index++ != 0)
                result.append(" ");
            result.append(card.name());
        }
        for (int i = index; i < STARTING_CARDS; i++) {
            if (i == 0)
                result.append("XX");
            else result.append(" XX");
        }

        return result.toString();
    }

    public String fieldString() {
        return "%s\nValue: %d".formatted(this.cardsString(), this.value());
    }
}
