package nz.pumbas.halpbot.buttons;

import net.dv8tion.jda.api.interactions.components.ActionRow;

import org.jetbrains.annotations.Nullable;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import nz.pumbas.halpbot.utilities.HalpbotUtils;

@RequiredArgsConstructor
public enum AfterRemoval
{
    /**
     * Just remove the dynamic component, but do nothing afterwards.
     */
    NOTHING(null),

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
    @Nullable
    private final AfterRemovalStrategy strategy;
}
