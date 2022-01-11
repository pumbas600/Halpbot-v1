package nz.pumbas.halpbot.commands.games;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.Button;

import org.dockbox.hartshorn.core.annotations.stereotype.Service;
import org.jetbrains.annotations.Nullable;

import java.awt.Color;
import java.util.List;
import java.util.stream.Stream;

import javax.inject.Inject;

import nz.pumbas.halpbot.buttons.ButtonAction;
import nz.pumbas.halpbot.buttons.ButtonAdapter;
import nz.pumbas.halpbot.commands.annotations.Command;

@Service
public class GameCommands
{
    private static final Color PALE_GREEN = new Color(135, 231, 21);
    private static final String WON_DESCRIPTION = "You won!";
    private static final String LOST_DESCRIPTION = "You lost :(";

    @Inject
    private ButtonAdapter buttonAdapter;

    @Command(alias = { "bj", "blackjack" }, description = "Plays a game of black jack")
    public void blackjack(MessageReceivedEvent event) {
        long userId = event.getAuthor().getIdLong();
        BlackjackSet userSet = new BlackjackSet();
        BlackjackSet botSet = new BlackjackSet();

        EmbedBuilder embed = this.blackjackEmbed(event.getAuthor(), userSet, botSet, false);
        List<Button> buttons =
                Stream.of(
                    Button.primary("halpbot:blackjack:hit", "Hit"),
                    Button.success("halpbot:blackjack:stand", "Stand"))
                .map((button) -> userSet.gameover()
                        ? button.asDisabled()
                        : this.buttonAdapter.register(button, userId, userSet, botSet))
                .toList();

        event.getChannel().sendMessageEmbeds(embed.build())
                .setActionRow(buttons)
                .queue();
    }

    @Nullable
    @ButtonAction(id = "halpbot:blackjack:hit", isEphemeral = true)
    public String hit(ButtonClickEvent event, long userId, BlackjackSet userSet, BlackjackSet botSet) {
        if (event.getUser().getIdLong() != userId)
            return "This is not your game";

        userSet.hit();
        EmbedBuilder updatedEmbed = this.blackjackEmbed(event.getUser(), userSet, botSet, false);

        event.editMessageEmbeds(updatedEmbed.build()).queue();
        if (userSet.gameover())
            event.getHook().editOriginalComponents(this.disabledButtons(event)).queue();
        return null;
    }

    @Nullable
    @ButtonAction(id = "halpbot:blackjack:stand", isEphemeral = true)
    public String stand(ButtonClickEvent event, long userId, BlackjackSet userSet, BlackjackSet botSet) {
        if (event.getUser().getIdLong() != userId)
            return "This is not your game";

        int userDiff = 21 - userSet.value();
        int botDiff = 21 - botSet.value();

        EmbedBuilder updatedEmbed = this.blackjackEmbed(event.getUser(), userSet, botSet, true);
        if (userDiff < botDiff)
            updatedEmbed.setDescription(WON_DESCRIPTION);
        else if (botDiff < userDiff)
            updatedEmbed.setDescription(LOST_DESCRIPTION);

        event.editMessageEmbeds(updatedEmbed.build()).queue();
        event.getHook().editOriginalComponents(this.disabledButtons(event)).queue();
        return null;
    }

    private List<ActionRow> disabledButtons(ButtonClickEvent event) {
        return event.getMessage()
                .getActionRows()
                .stream()
                .map((actionRow) -> ActionRow.of(actionRow.getComponents()
                        .stream()
                        .map((component) -> component instanceof Button button ? button.asDisabled() : component)
                        .toList()))
                .toList();
    }

    public EmbedBuilder blackjackEmbed(User user, BlackjackSet userSet, BlackjackSet botSet, boolean revealAll) {
        EmbedBuilder builder = new EmbedBuilder()
                .setAuthor(user.getAsTag(), null, user.getAvatarUrl())
                .setColor(PALE_GREEN)
                .addField("Your Hand", userSet.fieldString(true), true)
                .addField("Dealer Hand", botSet.fieldString(revealAll || userSet.gameover()), true);

        if (userSet.is21())
            builder.setDescription(WON_DESCRIPTION);
        else if (userSet.exceeds21())
            builder.setDescription(LOST_DESCRIPTION);
        return builder;
    }

}
