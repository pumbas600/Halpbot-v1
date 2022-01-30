package nz.pumbas.halpbot.commands.games;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.Button;

import org.dockbox.hartshorn.core.annotations.stereotype.Service;
import org.jetbrains.annotations.Nullable;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.inject.Inject;

import nz.pumbas.halpbot.buttons.Removal;
import nz.pumbas.halpbot.buttons.ButtonAction;
import nz.pumbas.halpbot.buttons.ButtonAdapter;
import nz.pumbas.halpbot.commands.annotations.Command;
import nz.pumbas.halpbot.decorators.log.Log;
import nz.pumbas.halpbot.decorators.time.Time;
import nz.pumbas.halpbot.utilities.LogLevel;

@Time
@Log(LogLevel.INFO)
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
            List<Button> buttons = this.disableButtons(event);
            if (botSet.hasHiddenCards())
                buttons.add(this.buttonAdapter.register(
                        Button.secondary("halpbot:bj:reveal", "Reveal"), userSet, botSet));

            event.getHook().editOriginalComponents(ActionRow.of(buttons)).queue();
        }
        return null;
    }

    @Nullable
    @ButtonAction(id = "halpbot:blackjack:stand", isEphemeral = true)
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

        List<Button> disabledButtons = this.disableButtons(event);
        event.getHook().editOriginalComponents(ActionRow.of(disabledButtons)).queue();
        return null;
    }

    @ButtonAction(id = "halpbot:bj:reveal", uses = 1)
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

    private List<Button> disableButtons(ButtonClickEvent event) {
        List<ActionRow> actionRows = event.getMessage().getActionRows();
        if (!actionRows.isEmpty()) {
            return actionRows.get(0).getButtons()
                    .stream()
                    .map(Button::asDisabled)
                    .peek(this.buttonAdapter::unregister)
                    .collect(Collectors.toList());
        }
        return new ArrayList<>();
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
