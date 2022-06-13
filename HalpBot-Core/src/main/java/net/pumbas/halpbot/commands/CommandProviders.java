package net.pumbas.halpbot.commands;

import net.pumbas.halpbot.commands.annotations.UseCommands;

import org.dockbox.hartshorn.component.Service;
import org.dockbox.hartshorn.component.processing.Provider;

@Service(activators = UseCommands.class)
public class CommandProviders {

    @Provider
    public Class<? extends CommandAdapter> commandAdapter() {
        return HalpbotCommandAdapter.class;
    }
}
