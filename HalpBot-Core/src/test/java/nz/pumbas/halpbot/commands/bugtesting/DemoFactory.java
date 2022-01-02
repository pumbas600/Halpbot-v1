package nz.pumbas.halpbot.commands.bugtesting;

import org.dockbox.hartshorn.core.annotations.Factory;
import org.dockbox.hartshorn.core.annotations.stereotype.Service;

@Service
public interface DemoFactory extends GenericFactory<DemoInterface<?>>
{
    @Factory
    @Override
    DemoInterface<?> create(String name);
}
