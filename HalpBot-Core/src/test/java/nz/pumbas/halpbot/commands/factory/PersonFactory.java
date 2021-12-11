package nz.pumbas.halpbot.commands.factory;

import org.dockbox.hartshorn.core.annotations.Factory;
import org.dockbox.hartshorn.core.annotations.service.Service;

@Service
public interface PersonFactory
{
    @Factory
    Person create(String name, int age);

    default Person create(String name) {
        return this.create(name, -1);
    }
}
