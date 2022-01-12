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

    public void add(Card... cards) {
        for (Card card : cards) {
            this.add(card);
        }
    }

    public void add(Card card) {
        this.cards.add(card);

        if (card.isAce()) {
            List<Integer> tempValues = new ArrayList<>(this.values);
            this.values.clear();
            this.values.addAll(tempValues.stream()
                    .map(value -> value + 11)
                    .filter(value -> value <= TARGET)
                    .toList());
            this.values.addAll(tempValues.stream()
                    .map(value -> value + card.value())
                    .toList());
        }
        else this.values = this.values.stream()
                .map((value) -> value + card.value())
                .collect(Collectors.toList());

        this.value = this.findValue();
    }

    public void hit() {
        this.add(Card.random());
    }

    private int findValue() {
        int max = this.values.get(0);
        for (int i = 1; i < this.values.size(); i++) {
            int value = this.values.get(i);
            if (value > max || max > TARGET && value < max)
                max = value;
        }
        return max;
    }

    public int value() {
        return this.value;
    }

    public String cardsString() {
        String cards = this.cards.stream().map(Card::emoji).collect(Collectors.joining(" "));
        if (this.cards.size() >= STARTING_CARDS)
            return cards;

        StringBuilder result = new StringBuilder();
        result.append(cards);
        for (int i = this.cards.size(); i < STARTING_CARDS; i++) {
            if (i != 0)
                result.append(" ");
            result.append(Card.BLANK.emoji());
        }

        return result.toString();
    }

    public String fieldString() {
        return "%s\nValue: %d".formatted(this.cardsString(), this.value());
    }
}
