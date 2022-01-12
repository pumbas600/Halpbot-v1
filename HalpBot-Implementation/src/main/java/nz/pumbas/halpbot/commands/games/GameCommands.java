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
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.inject.Inject;

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

    @Inject
    private ButtonAdapter buttonAdapter;

    @Command(alias = { "bj", "blackjack" }, description = "Plays a game of black jack")
    public void blackjack(MessageReceivedEvent event) {
        long userId = event.getAuthor().getIdLong();
        BlackjackSet userSet = new BlackjackSet();
        BlackjackSet botSet = new BlackjackSet();
        userSet.hit();
        userSet.hit();
        botSet.hit();

        String description = this.determineDescription(userSet);

        List<Button> buttons =
                Stream.of(
                    Button.primary("halpbot:blackjack:hit", "Hit"),
                    Button.success("halpbot:blackjack:stand", "Stand"))
                .map((button) -> userSet.gameover()
                        ? button.asDisabled()
                        : this.buttonAdapter.register(button, userId, userSet, botSet))
                .collect(Collectors.toList());

        if (userSet.gameover() && botSet.hasHiddenCards())
            buttons.add(this.buttonAdapter.register(
                    Button.secondary("halpbot:bj:reveal", "Reveal"), userSet, botSet));

        event.getChannel().sendMessageEmbeds(this.blackjackEmbed(event.getAuthor(), userSet, botSet, description))
                .setActionRow(buttons)
                .queue();
    }

    @Nullable
    @ButtonAction(id = "halpbot:blackjack:hit", isEphemeral = true)
    public String hit(ButtonClickEvent event, long userId, BlackjackSet userSet, BlackjackSet botSet) {
        if (event.getUser().getIdLong() != userId)
            return "This is not your game";

        userSet.hit();
        String description = this.determineDescription(userSet);
        event.editMessageEmbeds(this.blackjackEmbed(event.getUser(), userSet, botSet, description)).queue();

        List<Button> buttons = this.disabledButtons(event);
        if (userSet.gameover() && botSet.hasHiddenCards())
            buttons.add(this.buttonAdapter.register(
                    Button.secondary("halpbot:bj:reveal", "Reveal"), userSet, botSet));

        if (userSet.gameover())
            event.getHook().editOriginalComponents(ActionRow.of(buttons)).queue();
        return null;
    }

    @Nullable
    @ButtonAction(id = "halpbot:blackjack:stand", isEphemeral = true)
    public String stand(ButtonClickEvent event, long userId, BlackjackSet userSet, BlackjackSet botSet) {
        if (event.getUser().getIdLong() != userId)
            return "This is not your game";

        int userDiff = BlackjackSet.TARGET - userSet.value();
        int botDiff = BlackjackSet.TARGET - botSet.value();

        if (!userSet.exceeds21() && !botSet.exceeds21()) {
            while (userDiff < botDiff) {
                botSet.hit();
                botDiff = BlackjackSet.TARGET - botSet.value();
            }
        }

        String description = this.determineStandDescription(userSet, botSet);

        event.editMessageEmbeds(this.blackjackEmbed(event.getUser(), userSet, botSet, description)).queue();
        List<Button> disabledButtons = this.disabledButtons(event);

        event.getHook().editOriginalComponents(ActionRow.of(disabledButtons)).queue();
        return null;
    }

    @ButtonAction(id = "halpbot:bj:reveal")
    public void reveal(ButtonClickEvent event, BlackjackSet userSet, BlackjackSet botSet) {
        String description = this.determineStandDescription(userSet, botSet);

        botSet.revealHiddenCards();
        event.editMessageEmbeds(this.blackjackEmbed(event.getUser(), userSet, botSet, description)).queue();
        event.getHook().editOriginalComponents(ActionRow.of(this.disabledButtons(event))).queue();
    }

    private String determineStandDescription(BlackjackSet userSet, BlackjackSet botSet) {
        int userDiff = BlackjackSet.TARGET - userSet.value();
        int botDiff = BlackjackSet.TARGET - botSet.value();

        if (userSet.value() == botSet.value())
            return TIE_DESCRIPTION;
        else if (botSet.exceeds21() || userDiff < botDiff)
            return WON_DESCRIPTION;
        else
            return LOST_DESCRIPTION;
    }

    private String determineDescription(BlackjackSet userSet) {
        if (userSet.is21())
            return WON_DESCRIPTION;
        else if (userSet.exceeds21())
            return LOST_DESCRIPTION;
        else
            return INFO_DESCRIPTION;
    }

    private List<Button> disabledButtons(ButtonClickEvent event) {
        List<ActionRow> actionRows = event.getMessage().getActionRows();
        if (!actionRows.isEmpty()) {
            return actionRows.get(0).getButtons()
                    .stream()
                    .map(Button::asDisabled)
                    .collect(Collectors.toList());
        }
        return new ArrayList<>();
    }

    private MessageEmbed blackjackEmbed(User user, BlackjackSet userSet, BlackjackSet botSet, String description) {
        return new EmbedBuilder()
                .setAuthor(user.getAsTag(), null, user.getAvatarUrl())
                .setColor(PALE_GREEN)
                .setDescription(description)
                .addField("Your Hand", userSet.fieldString(), true)
                .addField("Dealer Hand", botSet.fieldString(), true)
                .build();
    }

}
