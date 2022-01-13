package nz.pumbas.halpbot.commands.games;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class CardSet
{
    private final List<Card> cards = new ArrayList<>();
    private final Random random = new Random();

    public CardSet(int decks) {
        for (int i = 0; i < decks; i++) {
            this.cards.addAll(List.of(Card.values()));
        }
    }

    public boolean isEmpty() {
        return this.cards.isEmpty();
    }

    public int count() {
        return this.cards.size();
    }

    public void remove(Card card) {
        this.cards.remove(card);
    }

    public Card removeRandom() {
        return this.cards.remove(this.random.nextInt(this.count()));
    }
}
