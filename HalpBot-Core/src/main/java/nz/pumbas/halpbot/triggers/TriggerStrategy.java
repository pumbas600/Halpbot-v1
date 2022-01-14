package nz.pumbas.halpbot.triggers;

import java.util.function.BiPredicate;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum TriggerStrategy
{
    START(String::startsWith),
    ANYWHERE(String::contains);

    private final BiPredicate<String, String> validator;

    /**
     * If the message contains the specified trigger. Both the message and the trigger should be lowered prior to
     * calling this method.
     *
     * @param message
     *      The lowered message
     * @param trigger
     *      The lowered trigger to determine if contained within the message
     *
     * @return If the trigger is contained within the message
     */
    public boolean contains(String message, String trigger) {
        return this.validator.test(message, trigger);
    }
}
