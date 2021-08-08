package nz.pumbas.halpbot.commands;

import net.dv8tion.jda.api.events.ShutdownEvent;

import org.jetbrains.annotations.NotNull;

@SuppressWarnings("InterfaceMayBeAnnotatedFunctional")
public interface OnShutdown
{
    /**
     * A method that is called just before the bot is shutdown.
     *
     * @param event
     *     The JDA {@link ShutdownEvent}
     */
    void onShutDown(@NotNull ShutdownEvent event);
}
