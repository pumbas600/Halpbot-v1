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

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.Button;
import net.pumbas.halpbot.buttons.ButtonAction;
import net.pumbas.halpbot.buttons.ButtonAdapter;
import net.pumbas.halpbot.commands.annotations.Command;
import net.pumbas.halpbot.decorators.log.Log;
import net.pumbas.halpbot.decorators.time.Time;

import org.dockbox.hartshorn.component.Service;
import org.jetbrains.annotations.Nullable;

import java.awt.Color;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import jakarta.inject.Inject;

@Log
@Time
@Service
public class GameCommands
{
    private static final Color PALE_GREEN = new Color(135, 231, 21);
    private static final String WON_DESCRIPTION = "You won! <a:roocheer:856015238129647667>";
    private static final String LOST_DESCRIPTION = "You lost <a:roosad:855361082037239808>";
    private static final String TIE_DESCRIPTION = "You tied";
    private static final String INFO_DESCRIPTION = """
            `Hit`: Take another card
            `Stand`: End the game
            """;

    private final Random random = new Random();

    @Inject
    private ButtonAdapter buttonAdapter;

    @Command(description = "Flips a coin")
    public String flip() {
        boolean isHeads = Math.random() < 0.5;
        return isHeads
                ? "https://tenor.com/view/heads-coinflip-flip-a-coin-coin-coins-gif-21479854"
                : "https://tenor.com/view/coins-tails-coin-flip-a-coin-coinflip-gif-21479856";
    }

    @Command(alias = { "bj", "blackjack" }, description = "Plays a game of black jack")
    public void blackjack(MessageReceivedEvent event) {
        final long userId = event.getAuthor().getIdLong();
        final BlackjackSet userSet = new BlackjackSet();
        final BlackjackSet botSet = new BlackjackSet();
        final CardSet cards = new CardSet(BlackjackSet.DECKS, this.random);

        userSet.hit(cards);
        userSet.hit(cards);
        botSet.hit(cards);
        botSet.addHidden(cards.next());

        String description = this.determineDescription(userSet);

        List<Button> buttons = Stream.of(
                    Button.primary("halpbot:blackjack:hit", "Hit"),
                    Button.success("halpbot:blackjack:stand", "Stand"))
                .map((button) -> userSet.gameover()
                        ? button.asDisabled()
                        : this.buttonAdapter.register(button, userId, userSet, botSet, cards))
                .collect(Collectors.toList());

        if (userSet.gameover() && botSet.hasHiddenCards())
            buttons.add(this.buttonAdapter.register(
                    Button.secondary("halpbot:bj:reveal", "Reveal"), userSet, botSet));

        event.getChannel().sendMessageEmbeds(
                this.blackjackEmbed(event.getAuthor(), userSet, botSet, description, this.footer(cards)))
                .setActionRow(buttons)
                .queue();
    }

    @Nullable
    @ButtonAction(id = "halpbot:blackjack:hit", isEphemeral = true)
    public String hit(ButtonClickEvent event, long userId, BlackjackSet userSet, BlackjackSet botSet, CardSet cards) {
        if (event.getUser().getIdLong() != userId)
            return "This is not your game";
        if (cards.isEmpty())
            this.stand(event, userId, userSet, botSet, cards);

        userSet.hit(cards);
        String description = this.determineDescription(userSet);
        event.editMessageEmbeds(
                this.blackjackEmbed(event.getUser(), userSet, botSet, description, this.footer(cards)))
                .queue();

        if (userSet.gameover()) {
            if (botSet.hasHiddenCards()) {
                List<Button> buttons = event.getMessage().getButtons()
                        .stream()
                        .peek(button -> this.buttonAdapter.unregister(button, false))
                        .map(Button::asDisabled)
                        .collect(Collectors.toList());

                Button reveal = Button.secondary("halpbot:bj:reveal", "Reveal");
                buttons.add(this.buttonAdapter.register(reveal, userSet, botSet));

                event.getHook().editOriginalComponents(ActionRow.of(buttons)).queue();
            }
            else this.unregisterButtons(event);
        }
        return null;
    }

    @Nullable
    @ButtonAction(id = "halpbot:blackjack:stand", isEphemeral = true, maxUses = 1)
    public String stand(ButtonClickEvent event, long userId, BlackjackSet userSet, BlackjackSet botSet, CardSet cards) {
        if (event.getUser().getIdLong() != userId)
            return "This is not your game";

        int userDiff = BlackjackSet.TARGET - userSet.value();
        int botDiff = BlackjackSet.TARGET - botSet.value();

        if (!userSet.gameover() && !botSet.exceeds21()) {
            if (userDiff <= botDiff && botSet.hasHiddenCards()) {
                botSet.revealHiddenCards();
                botDiff = BlackjackSet.TARGET - botSet.value();
            }

            while (userDiff <= botDiff) {
                botSet.hit(cards);
                botDiff = BlackjackSet.TARGET - botSet.value();
            }
        }

        String description = this.determineStandDescription(userSet, botSet);

        event.editMessageEmbeds(
                this.blackjackEmbed(event.getUser(), userSet, botSet, description, this.footer(cards)))
                .queue();

        this.unregisterButtons(event);
        return null;
    }

    @ButtonAction(id = "halpbot:bj:reveal", maxUses = 1)
    public void reveal(ButtonClickEvent event, BlackjackSet userSet, BlackjackSet botSet) {
        String description = this.determineStandDescription(userSet, botSet);

        botSet.revealHiddenCards();
        event.editMessageEmbeds(
                this.blackjackEmbed(event.getUser(), userSet, botSet,
                        description, "Dealer's cards revealed by " + event.getUser().getAsTag()))
                .queue();
    }

    @SuppressWarnings("OverlyComplexBooleanExpression")
    private String determineStandDescription(BlackjackSet userSet, BlackjackSet botSet) {
        int userDiff = BlackjackSet.TARGET - userSet.value();
        int botDiff = BlackjackSet.TARGET - botSet.value();

        if (userSet.is21() && botSet.is21() && userSet.isBlackjack() == botSet.isBlackjack())
            return TIE_DESCRIPTION;
        else if (userSet.exceeds21() || (botDiff < userDiff && !botSet.exceeds21()) || botSet.isBlackjack())
            return LOST_DESCRIPTION;
        else
            return WON_DESCRIPTION;
    }

    private String determineDescription(BlackjackSet userSet) {
        if (userSet.is21())
            return WON_DESCRIPTION;
        else if (userSet.exceeds21())
            return LOST_DESCRIPTION;
        else
            return INFO_DESCRIPTION;
    }

    private String footer(CardSet cards) {
        return "Remaining cards: %d - Decks: %d".formatted(cards.count(), BlackjackSet.DECKS);
    }

    private void unregisterButtons(ButtonClickEvent event) {
        event.getMessage()
                .getButtons()
                .forEach(this.buttonAdapter::unregister);
    }

    private MessageEmbed blackjackEmbed(User user, BlackjackSet userSet, BlackjackSet botSet,
                                        String description, @Nullable String footer) {
        return new EmbedBuilder()
                .setAuthor(user.getAsTag(), null, user.getAvatarUrl())
                .setColor(PALE_GREEN)
                .setDescription(description)
                .addField("Your Hand", userSet.fieldString(), true)
                .addField("Dealer Hand", botSet.fieldString(), true)
                .setFooter(footer)
                .build();
    }

}
