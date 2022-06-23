package net.pumbas.halpbot.commands.bugs;

import org.dockbox.hartshorn.component.Service;
import org.dockbox.hartshorn.component.processing.Provider;

@Service
public class HandlerProvider {

    @Provider
    public Class<? extends Handler> handler() {
        return HalpbotHandler.class;
    }
}
