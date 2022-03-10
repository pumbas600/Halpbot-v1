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

package net.pumbas.halpbot.commands.games;

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
        this.add(cards.next());
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
