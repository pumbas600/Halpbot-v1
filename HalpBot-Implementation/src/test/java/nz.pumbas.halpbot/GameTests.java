package nz.pumbas.halpbot;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import nz.pumbas.halpbot.commands.games.BlackjackSet;
import nz.pumbas.halpbot.commands.games.Card;

public class GameTests
{
    @Test
    public void acesValueTest() {
        BlackjackSet set = new BlackjackSet();
        set.add(Card.DA, Card.D3);
        Assertions.assertEquals(14, set.value());
        Assertions.assertTrue(set.isSoft());

        set.add(Card.DJ);
        Assertions.assertEquals(14, set.value());
        Assertions.assertFalse(set.isSoft());
    }

    @Test
    public void valueTest() {
        BlackjackSet set = new BlackjackSet();
        set.add(Card.S7, Card.CA, Card.D3, Card.SQ);
        Assertions.assertEquals(21, set.value());
        Assertions.assertFalse(set.isSoft());
    }

    @Test
    public void tieTest() {
        BlackjackSet userSet = new BlackjackSet();
        BlackjackSet botSet = new BlackjackSet();
        userSet.add(Card.HA, Card.HJ);
        botSet.add(Card.CA, Card.CJ);

        Assertions.assertTrue(userSet.isBlackjack());
        Assertions.assertTrue(botSet.isBlackjack());
        Assertions.assertTrue(userSet.is21());
        Assertions.assertTrue(botSet.is21());

        Assertions.assertTrue(userSet.is21() && botSet.is21() && userSet.isBlackjack() == botSet.isBlackjack());
    }
}
