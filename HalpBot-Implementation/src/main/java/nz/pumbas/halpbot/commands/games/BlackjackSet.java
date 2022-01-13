package nz.pumbas.halpbot.commands.games;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import lombok.Getter;

public class BlackjackSet
{
    public static final int TARGET = 21;
    public static final int STARTING_CARDS = 2;
    public static final int DECKS = 2;

    private static final String BLANK_CARD = "<:CardBack:930710260660990014>";

    private final List<Card> cards = new ArrayList<>();
    private final List<Card> hiddenCards = new ArrayList<>();

    @Getter private int value;
    private int acesAs11Count;

    public int count() {
        return this.cards.size();
    }

    public void revealHiddenCards() {
        this.hiddenCards.forEach(this::add);
    }

    public boolean isSoft() {
        return this.acesAs11Count != 0;
    }

    public boolean hasHiddenCards() {
        return !this.hiddenCards.isEmpty();
    }

    public boolean is21() {
        return this.value() == TARGET;
    }

    public boolean isBlackjack() {
        return this.count() == 2 && this.is21();
    }

    public boolean exceeds21() {
        return this.value() > TARGET;
    }

    public boolean gameover() {
        return this.value() >= TARGET;
    }

    public void addHidden(Card card) {
        this.hiddenCards.add(card);
    }

    public void add(Card... cards) {
        for (Card card : cards) {
            this.add(card);
        }
    }

    public void add(Card card) {
        this.cards.add(card);
        this.value += card.value();

        if (card.isAce())
            this.acesAs11Count++;
        while (this.exceeds21() && this.isSoft()) {
            this.value -= 10;
            this.acesAs11Count--;
        }
    }

    public void hit(CardSet cards) {
        this.add(cards.removeRandom());
    }

    public String cardsString() {
        String cards = this.cards.stream().map(Card::emoji).collect(Collectors.joining(""));
        if (this.cards.size() >= STARTING_CARDS)
            return cards;

        StringBuilder result = new StringBuilder();
        result.append(cards);
        result.append(BLANK_CARD.repeat(STARTING_CARDS - this.cards.size()));

        return result.toString();
    }

    public String fieldString() {
        return "%s\nValue: %s".formatted(this.cardsString(),
                this.isBlackjack() ? "Blackjack" :
                        (this.isSoft() ? "Soft " : "") + this.value());
    }
}
