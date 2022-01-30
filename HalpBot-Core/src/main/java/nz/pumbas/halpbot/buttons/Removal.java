package nz.pumbas.halpbot.buttons;

import org.jetbrains.annotations.Nullable;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import nz.pumbas.halpbot.utilities.HalpbotUtils;

@RequiredArgsConstructor
public enum Removal
{
    /**
     * Just remove the dynamic component, but do nothing afterwards.
     */
    NOTHING(null),

    /**
     * Disable the specific component removed.
     */
    DISABLE(HalpbotUtils::disable);

    @Getter
    @Nullable
    private final AfterRemovalFunction strategy;
}
