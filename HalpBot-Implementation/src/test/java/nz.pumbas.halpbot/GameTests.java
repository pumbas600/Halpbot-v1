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
