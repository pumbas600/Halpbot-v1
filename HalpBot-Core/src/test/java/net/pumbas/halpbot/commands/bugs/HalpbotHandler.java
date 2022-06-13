package net.pumbas.halpbot.commands.bugs;

import org.dockbox.hartshorn.inject.binding.Bound;

public record HalpbotHandler(String name) implements Handler {

    @Bound
    public HalpbotHandler {}
}
