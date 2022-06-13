package net.pumbas.halpbot.commands;

import org.dockbox.hartshorn.component.Service;
import org.dockbox.hartshorn.component.processing.Provider;

@Service
public class CommandProviders {

    @Provider
    public Class<? extends CommandAdapter> commandAdapter() {
        return HalpbotCommandAdapter.class;
    }
}
