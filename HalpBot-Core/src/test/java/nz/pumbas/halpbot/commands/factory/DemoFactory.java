package nz.pumbas.halpbot.commands.factory;

import org.dockbox.hartshorn.core.annotations.Factory;
import org.dockbox.hartshorn.core.annotations.stereotype.Service;

@Service
public interface DemoFactory
{
    @Factory
    DemoServiceA serviceA();
}
