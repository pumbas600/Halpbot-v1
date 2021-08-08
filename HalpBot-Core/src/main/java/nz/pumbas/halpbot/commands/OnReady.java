package nz.pumbas.halpbot.commands;

import net.dv8tion.jda.api.events.ReadyEvent;

import org.jetbrains.annotations.NotNull;

@SuppressWarnings("InterfaceMayBeAnnotatedFunctional")
public interface OnReady
{
    /**
     * A method that is called once after the bot has been initialised.
     *
     * @param event
     *     The JDA {@link ReadyEvent}.
     */
    void onReady(@NotNull ReadyEvent event);
}
