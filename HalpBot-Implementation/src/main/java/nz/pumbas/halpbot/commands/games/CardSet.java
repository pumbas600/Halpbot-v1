package nz.pumbas.halpbot.commands.games;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class CardSet
{
    private final List<Card> cards = new ArrayList<>();

    public CardSet(int decks, Random random) {
        for (int i = 0; i < decks; i++) {
            this.cards.addAll(List.of(Card.values()));
        }
        Collections.shuffle(this.cards, random);
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

    public Card next() {
        return this.cards.remove(0);
    }
}
