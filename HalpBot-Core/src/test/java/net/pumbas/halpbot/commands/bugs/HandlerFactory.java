package net.pumbas.halpbot.commands.bugs;

import org.dockbox.hartshorn.component.Service;
import org.dockbox.hartshorn.component.factory.Factory;

@Service
public interface HandlerFactory {

    @Factory
    Handler create(String name);
}
