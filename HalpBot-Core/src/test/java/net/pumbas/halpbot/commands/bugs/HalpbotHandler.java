package net.pumbas.halpbot.commands.bugs;

import org.dockbox.hartshorn.inject.binding.Bound;
import org.dockbox.hartshorn.inject.binding.ComponentBinding;

@ComponentBinding(Handler.class)
public record HalpbotHandler(String name) implements Handler {

    @Bound
    public HalpbotHandler {}
}
