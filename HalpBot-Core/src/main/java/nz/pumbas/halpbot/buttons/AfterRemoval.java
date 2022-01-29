package nz.pumbas.halpbot.buttons;

import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.interactions.components.ActionRow;

import java.util.List;
import java.util.function.Function;

import lombok.Getter;
import nz.pumbas.halpbot.utilities.HalpbotUtils;

public enum AfterRemoval
{
    /**
     * Just remove the dynamic component, but do nothing afterwards.
     */
    NOTHING(e -> e.getMessage().getActionRows()),

    /**
     * Disable the specific component removed.
     */
    DISABLE(e -> {
        final String id = e.getComponentId();
        return e.getMessage().getActionRows()
                .stream()
                .map(row -> ActionRow.of(row.getComponents()
                        .stream()
                        .map(component -> id.equals(component.getId())
                                ? HalpbotUtils.disable(component) : component)
                        .toList()))
                .toList();
    }),

    /**
     * Disable all the components on the same message as the removed component.
     */
    DISABLE_ALL(e -> e.getMessage().getActionRows()
            .stream()
            .map(row -> ActionRow.of(row.getComponents()
                    .stream()
                    .map(HalpbotUtils::disable)
                    .toList()))
            .toList());

    @Getter
    private final Function<ButtonClickEvent, List<ActionRow>> strategy;

    AfterRemoval(Function<ButtonClickEvent, List<ActionRow>> strategy) {
        this.strategy = strategy;
    }
}
