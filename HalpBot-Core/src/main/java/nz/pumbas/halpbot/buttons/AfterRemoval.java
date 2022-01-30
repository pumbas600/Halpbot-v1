package nz.pumbas.halpbot.buttons;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import nz.pumbas.halpbot.utilities.HalpbotUtils;

@RequiredArgsConstructor
public enum AfterRemoval
{
    /**
     * Just remove the dynamic component, but do nothing afterwards.
     */
    NOTHING(component -> component),

    /**
     * Disable the specific component removed.
     */
    DISABLE(HalpbotUtils::disable);

    @Getter
    private final AfterRemovalFunction strategy;
}
