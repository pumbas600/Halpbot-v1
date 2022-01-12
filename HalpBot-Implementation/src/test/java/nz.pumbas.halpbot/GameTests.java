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

        set.add(Card.DJ);
        Assertions.assertEquals(14, set.value());
    }

    @Test
    public void valueTest() {
        BlackjackSet set = new BlackjackSet();
        set.add(Card.S7, Card.CA, Card.D3, Card.SQ);
        Assertions.assertEquals(21, set.value());
    }
}
