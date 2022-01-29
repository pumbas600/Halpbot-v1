package nz.pumbas.halpbot.buttons;

import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.interactions.components.ActionRow;

import java.util.List;
import java.util.function.Function;

public interface AfterRemovalStrategy extends Function<ButtonClickEvent, List<ActionRow>>
{
}
