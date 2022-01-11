package nz.pumbas.halpbot.commands.games;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class BlackjackSet
{
    private final List<Card> cards = new ArrayList<>();

    public BlackjackSet() {
        this.hit();
        this.hit();
    }

    public boolean containsAce() {
        return this.cards.stream().anyMatch(Card::isAce);
    }

    public boolean is21() {
        return this.value() == 21;
    }

    public boolean exceeds21() {
        return this.value() > 21;
    }

    public boolean gameover() {
        return this.value() >= 21;
    }

    public void hit() {
        this.cards.add(Card.random());
    }

    public int value() {
        return this.cards.stream().mapToInt(Card::value).sum();
    }

    public String cardsString(boolean revealAll) {
        if(revealAll)
            return this.cards.stream().map(Card::name).collect(Collectors.joining(" "));

        StringBuilder result = new StringBuilder();
        int index = 0;
        for (Card card : this.cards) {
            if (index++ == 0)
                result.append(card.name());
            else result.append(" XX");
        }
        return result.toString();
    }

    public String fieldString(boolean reveallAll) {
        return "%s\nValue: %d".formatted(
                this.cardsString(reveallAll),
                reveallAll ? this.value() : this.cards.get(0).value());
    }

}
